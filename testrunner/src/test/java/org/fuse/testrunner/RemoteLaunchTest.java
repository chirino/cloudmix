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
package org.fuse.testrunner;

import java.io.File;

import org.apache.activemq.broker.BrokerService;
import org.fusesource.testrunner.ProcessListener;
import org.fusesource.testrunner.TRAgent;
import org.fusesource.testrunner.TRClient;
import org.fusesource.testrunner.TRJMSCommunicator;
import org.fusesource.testrunner.TRLaunchDescr;
import org.fusesource.testrunner.TRProcessContext;
import org.fusesource.testrunner.TRClient.TRClientListener;

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
public class RemoteLaunchTest extends TestCase implements TRClientListener {
    static {
        try {
            System.setProperty(TRJMSCommunicator.FACTORY_PROP, "org.apache.activemq.ActiveMQConnectionFactory");
        } catch (Exception e) {}
    }
    BrokerService controlBroker;
    TRAgent agent;
    TRClient client;

    protected void setUp() throws Exception {
        controlBroker = new BrokerService();
        controlBroker.setBrokerName("TRControlBroker");
        controlBroker.setPersistent(false);
        controlBroker.addConnector("tcp://localhost:6000");
        controlBroker.start();

        //Set up a launch agent:
        agent = new TRAgent();
        agent.setDataDirectory("target" + File.separator + "testrunner-data");
        agent.setControlUrl("tcp://localhost:6000");
        agent.start();

        //Set up a communicator to talk to the agent:
        TRJMSCommunicator jmsComm = new TRJMSCommunicator("tcp://localhost:6000", System.getProperty("user.name") + System.currentTimeMillis());

        client = new TRClient(jmsComm);
        client.setBindTimeout(5000);
        client.setLaunchTimeout(10000);
        client.setKillTimeout(5000);
        client.setListener(this);
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
        TRLaunchDescr ld = new TRLaunchDescr();
        ld.setProcessType(TRLaunchDescr.PROCESS_TYPE_JAVA_EXE);
        ld.setOutputType(TRLaunchDescr.DATA_OUTPUT);
        ld.setJVMPath("java");
        ld.setRunName("org.fuse.testrunner.DataInputTestApplication");
        ld.addClassPath(System.getProperty("java.class.path"));
        ld.setWorkingDir(System.getProperty("user.dir") + File.separator + "target");

        DataOutputTester tester = new DataOutputTester();
        tester.test(client.launchProcess(agent.getAgentId(), ld, tester));

    }

    public void testObjectOutput() throws Exception {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.testrunner.TRClient.LaunchClientListener#onTRException(java
     * .lang.String, java.lang.Throwable)
     */
    public void onTRException(String reason, Throwable thrown) {
        System.err.println(reason);
        thrown.printStackTrace();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.testrunner.TRClient.LaunchClientListener#onTRInfo(java.lang
     * .String)
     */
    public void onTRInfo(String msg) {
        System.out.println(msg);
    }

    private class DataOutputTester implements ProcessListener {
        private final int TEST_OUTPUT = 0;
        private final int TEST_ERROR = 1;
        private final int SUCCESS = 2;
        private final int FAIL = 3;

        private static final String EXPECTED_OUTPUT = "test output";
        private static final String EXPECTED_ERROR = "test error";

        private int state = TEST_OUTPUT;

        private Exception failure;

        public synchronized void test(TRProcessContext ctx) throws Exception {
            while (true) {

                switch (state) {
                case TEST_OUTPUT: {
                    System.out.println("Testing output");
                    client.sendMessage(ctx, "echo:" + EXPECTED_OUTPUT);
                    break;
                }
                case TEST_ERROR: {
                    System.out.println("Testing error");
                    client.sendMessage(ctx, "echo:" + EXPECTED_ERROR);
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

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ProcessListener#handleError(org.fusesource
         * .testrunner.TRProcessContext, java.lang.String, java.lang.Throwable)
         */
        public synchronized void handleError(TRProcessContext ctx, String message, Throwable thrown) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ProcessListener#handleMessage(org.fusesource
         * .testrunner.TRProcessContext, java.lang.Object)
         */
        public synchronized void handleMessage(TRProcessContext ctx, Object msg) {
            failure = new Exception("Unexpected object output: " + msg);
            state = FAIL;
            notifyAll();
        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.fusesource.testrunner.ProcessListener#handleProcessInfo(org.
         * fusesource.testrunner.TRProcessContext, java.lang.String)
         */
        public synchronized void handleProcessInfo(TRProcessContext ctx, String info) {
            System.out.println(ctx + ": " + info);

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ProcessListener#handleSystemErr(org.fusesource
         * .testrunner.TRProcessContext, java.lang.String)
         */
        public synchronized void handleSystemErr(TRProcessContext ctx, String err) {
            System.err.print(err);
            if (state == TEST_ERROR && EXPECTED_OUTPUT.equals(err.trim())) {
                state = SUCCESS;
            } else {
                failure = new Exception("Unexpected system err: " + err);
                state = FAIL;
            }

            notifyAll();

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ProcessListener#handleSystemOut(org.fusesource
         * .testrunner.TRProcessContext, java.lang.String)
         */
        public synchronized void handleSystemOut(TRProcessContext ctx, String output) {
            System.out.print(output);
            if (state == TEST_OUTPUT && EXPECTED_OUTPUT.equals(output.trim())) {
                state = TEST_ERROR;
            } else {
                failure = new Exception("Unexpected system output: " + output);
                state = FAIL;
            }

            notifyAll();

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ProcessListener#processDone(org.fusesource
         * .testrunner.TRProcessContext, int)
         */
        public synchronized void processDone(TRProcessContext ctx, int exitCode) {
            // TODO Auto-generated method stub

        }

    }

}
