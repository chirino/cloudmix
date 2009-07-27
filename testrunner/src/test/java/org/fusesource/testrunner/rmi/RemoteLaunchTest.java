/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.testrunner.rmi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.rmi.RemoteException;

import org.apache.activemq.broker.BrokerService;
import static org.fusesource.testrunner.rmi.Expression.*;
import org.fusesource.rmiviajms.JMSRemoteObject;

import junit.framework.TestCase;

/**
 * RemoteLaunchTest
 * <p>
 * Description:
 * </p>
 *
 * @author cmacnaug
 * @version 1.0
 */
public class RemoteLaunchTest extends TestCase {

    BrokerService controlBroker;
    ProcessLauncher agent;
    LauncherClient client;

    protected void setUp() throws Exception {
        controlBroker = new BrokerService();
        controlBroker.setBrokerName("TRControlBroker");
        controlBroker.setPersistent(false);
        controlBroker.addConnector("tcp://localhost:61616");
        controlBroker.start();

        //Set up a launch agent:
        agent = new ProcessLauncher();
        agent.setDataDirectory("target" + File.separator + "testrunner-data");
        agent.start();

        client = new LauncherClient("client1");
        client.setBindTimeout(5000);
        client.setLaunchTimeout(10000);
        client.setKillTimeout(5000);
        client.bindAgent(agent.getAgentId());

    }
    

    protected void tearDown() throws Exception {
        System.out.println("Shutting down control com");
        client.close();
        System.out.println("Shutting down agent");
        try {
            agent.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Shutting down control broker");
        controlBroker.stop();
    }

    public void testDataOutput() throws Exception {
        LaunchDescription ld = new LaunchDescription();
        ld.add("java");
        ld.add("-cp");

        ArrayList<FileExpression> files = new ArrayList<FileExpression>();
        for (String file : System.getProperty("java.class.path").split(File.pathSeparator)) {
            files.add(file(file));
        }

        ld.add(path(files));
        ld.add("org.fuse.testrunner.DataInputTestApplication");


        DataOutputTester tester = new DataOutputTester();
        tester.test(client.launchProcess(agent.getAgentId(), ld, tester));

    }

    public class DataOutputTester extends JMSRemoteObject implements IProcessListener {

        private final int TEST_OUTPUT = 0;
        private final int TEST_ERROR = 1;
        private final int SUCCESS = 2;
        private final int FAIL = 3;

        private static final String EXPECTED_OUTPUT = "test output";
        private static final String EXPECTED_ERROR = "test error";

        private int state = TEST_OUTPUT;

        private Exception failure;

        public DataOutputTester() throws RemoteException {
        }

        public synchronized void test(IProcess process) throws Exception {

            while (true) {

                switch (state) {
                case TEST_OUTPUT: {
                    System.out.println("Testing output");
                    client.println(process, "echo:" + EXPECTED_OUTPUT);
                    break;
                }
                case TEST_ERROR: {
                    System.out.println("Testing error");
                    client.println(process, "error:" + EXPECTED_ERROR);
                    break;
                }
                case SUCCESS: {
                    if (failure != null) {
                        throw failure;
                    }
                    return;
                }
                case FAIL:
                default: {
                    if (failure == null) {
                        failure = new Exception();
                    }
                    throw failure;
                }
                }

                int oldState = state;
                wait(10000);
                if(oldState == state)
                {
                    throw new Exception("Timed out in state: " + state);
                }
            }
        }


        synchronized public void write(int fd, byte[] data) throws RemoteException {
            String output = new String(data);
            System.out.print(output);

            if (fd == IStream.FD_STD_OUT ) {
                if (state == TEST_OUTPUT && EXPECTED_OUTPUT.equals(output.trim())) {
                    state = TEST_ERROR;
                } else {
                    failure = new Exception("Unexpected system output: " + output);
                    state = FAIL;
                }
                notifyAll();
            } else if (fd == IStream.FD_STD_ERR ) {
                if (state == TEST_ERROR && EXPECTED_ERROR.equals(output.trim())) {
                    state = SUCCESS;
                } else {
                    failure = new Exception("Unexpected system err: " + output);
                    state = FAIL;
                }
                notifyAll();
            }
        }

        public void onExit(int exitCode) {
        }

        public void onError(Throwable thrown) {
        }

        public void onInfoLogging(String message) {
        }

        public void ping() {
        }

        public void open(int fd) throws RemoteException, IOException {
        }

        public void close(int fd) throws RemoteException {
        }
    }

}