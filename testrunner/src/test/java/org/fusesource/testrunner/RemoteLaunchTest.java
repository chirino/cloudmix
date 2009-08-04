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
package org.fusesource.testrunner;

import static org.fusesource.testrunner.Expression.file;
import static org.fusesource.testrunner.Expression.path;
import static org.fusesource.testrunner.Expression.resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.activemq.broker.BrokerService;
import org.fusesource.rmiviajms.JMSRemoteObject;
import org.fusesource.testrunner.LaunchDescription;
import org.fusesource.testrunner.Process;
import org.fusesource.testrunner.ProcessListener;
import org.fusesource.testrunner.Expression.FileExpression;
import org.fusesource.testrunner.rmi.RemoteLauncherClient;
import org.fusesource.testrunner.rmi.RemoteProcessLauncher;

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
    RemoteProcessLauncher agent;
    RemoteLauncherClient clientRemote;
    ResourceManager commonResourceManager;

    protected void setUp() throws Exception {

        String dataDir = "target" + File.separator + "remote-launch-test-data";
        String commonRepo = new File(dataDir + File.separator + "common-repo").toURI().toString();

        controlBroker = new BrokerService();
        controlBroker.setBrokerName("RemoteLauncherBroker");
        controlBroker.setPersistent(false);
        controlBroker.addConnector("tcp://localhost:61616");
        controlBroker.start();
        
        //Set up a launch agent:
        agent = new RemoteProcessLauncher();
        agent.setDataDirectory(new File(dataDir + File.separator + "testrunner-data"));
        agent.setCommonResourceRepoUrl(commonRepo);
        agent.start();
        agent.purgeResourceRepository();

        clientRemote = new RemoteLauncherClient("client1");
        clientRemote.setBindTimeout(5000);
        clientRemote.setLaunchTimeout(10000);
        clientRemote.setKillTimeout(5000);
        clientRemote.bindAgent(agent.getAgentId());

        commonResourceManager = new ResourceManager();
        commonResourceManager.setCommonRepo(commonRepo, null);

    }

    protected void tearDown() throws Exception {
        System.out.println("Shutting down control com");
        clientRemote.close();
        System.out.println("Shutting down agent");
        try {
            agent.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Shutting down control broker");
        controlBroker.stop();
        controlBroker.waitUntilStopped();
        
        JMSRemoteObject.resetSystem();
        
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
        ld.add("org.fusesource.testrunner.DataInputTestApplication");

        DataOutputTester tester = new DataOutputTester();
        tester.test(clientRemote.launchProcess(agent.getAgentId(), ld, tester));

    }

    public void testResource() throws Exception {
        LaunchDescription ld = new LaunchDescription();
        ld.add("java");
        ld.add("-cp");

        ArrayList<FileExpression> files = new ArrayList<FileExpression>();
        for (String file : System.getProperty("java.class.path").split(File.pathSeparator)) {
            files.add(file(file));
        }

        ld.add(path(files));
        ld.add("org.fusesource.testrunner.DataInputTestApplication");

        LaunchResource resource = new LaunchResource();
        resource.setType(LaunchResource.FILE);
        resource.setRepoName("common");
        resource.setRepoPath("test/file.dat");
        byte[] data = new String("Test Data").getBytes();
        commonResourceManager.deployFile(resource, data);
        ld.addResource(resource);

        ld.add("-DataFile");
        ld.add(resource(resource));

        DataFileTester tester = new DataFileTester();
        tester.test(clientRemote.launchProcess(agent.getAgentId(), ld, tester), data);

    }

    public class DataFileTester implements ProcessListener {

        private final int TESTING = 1;
        private final int SUCCESS = 2;
        private final int FAIL = 3;

        private int state = TESTING;
        private Exception failure;

        private ByteArrayOutputStream output = new ByteArrayOutputStream();
        private byte[] expected;

        public synchronized void test(Process process, byte[] data) throws Exception {

            expected = data;
            try {
                process.write(Process.FD_STD_IN, "echo-data-file\n".getBytes());
                while (true) {
                    if (state == FAIL) {
                        throw failure;
                    } else if (state == SUCCESS) {
                        return;
                    }

                    wait(10000);
                    if (state == TESTING) {
                        failure = new Exception("Timed out");
                        state = FAIL;
                    }
                }

            } finally {
                process.kill();
            }
        }

        public synchronized void onProcessExit(int exitCode) {
            if (state == TESTING) {
                state = FAIL;
                notifyAll();
            }
        }

        public synchronized void onProcessError(Throwable thrown) {
            failure = new Exception("Unexpected process error", thrown);
            state = FAIL;
            notifyAll();
        }

        public void onProcessInfo(String message) {
            System.out.println("PROCESS INFO: " + message);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.fusesource.testrunner.ProcessListener#onProcessOutput(int,
         * byte[])
         */
        public synchronized void onProcessOutput(int fd, byte[] pOut) {
            if (fd == Process.FD_STD_ERR) {
                System.err.println(new String(pOut));
                failure = new Exception("Error: " + new String(pOut));
                state = FAIL;
                notifyAll();
                return;
            }
            try {
                output.write(pOut);
                if (output.size() < expected.length) {
                    return;
                }

                byte[] actual = output.toByteArray();

                if (!Arrays.equals(actual, expected)) {
                    failure = new Exception("Received data doesn't match: " + new String(actual) + new String(expected));
                    state = FAIL;
                } else {
                    state = SUCCESS;
                }
                notifyAll();

            } catch (IOException e) {
                failure = e;
                state = FAIL;
                notifyAll();
            }
        }

    }

    public class DataOutputTester implements ProcessListener {

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

        public synchronized void test(Process process) throws Exception {

            try {
                while (true) {

                    switch (state) {
                    case TEST_OUTPUT: {
                        System.out.println("Testing output");
                        process.write(Process.FD_STD_IN, new String("echo:" + EXPECTED_OUTPUT + "\n").getBytes());
                        break;
                    }
                    case TEST_ERROR: {
                        System.out.println("Testing error");
                        process.write(Process.FD_STD_IN, new String("error:" + EXPECTED_ERROR + "\n").getBytes());
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
                    if (oldState == state) {
                        throw new Exception("Timed out in state: " + state);
                    }
                }
            } finally {
                process.kill();
            }
        }

        synchronized public void onProcessOutput(int fd, byte[] data) {
            String output = new String(data);

            if (fd == Process.FD_STD_OUT) {
                System.out.print("STDOUT: " + output);
                if (state == TEST_OUTPUT && EXPECTED_OUTPUT.equals(output.trim())) {
                    state = TEST_ERROR;
                } else {
                    failure = new Exception("Unexpected system output: " + output);
                    state = FAIL;
                }
                notifyAll();
            } else if (fd == Process.FD_STD_ERR) {
                System.out.print("STDERR: " + output);
                if (state == TEST_ERROR && EXPECTED_ERROR.equals(output.trim())) {
                    state = SUCCESS;
                } else {
                    failure = new Exception("Unexpected system err: " + output);
                    state = FAIL;
                }
                notifyAll();
            }
        }

        public synchronized void onProcessExit(int exitCode) {
            if (state < SUCCESS) {
                failure = new Exception("Premature process exit");
                state = FAIL;
                notifyAll();
            }
        }

        public synchronized void onProcessError(Throwable thrown) {
            failure = new Exception("Unexpected process error", thrown);
            state = FAIL;
            notifyAll();
        }

        public void onProcessInfo(String message) {
            System.out.println("PROCESS INFO: " + message);
        }
    }

}