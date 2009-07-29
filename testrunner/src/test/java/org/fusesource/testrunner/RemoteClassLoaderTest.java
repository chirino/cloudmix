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

import junit.framework.TestCase;
import org.fusesource.testrunner.rmi.RemoteProcessLauncher;
import org.fusesource.testrunner.rmi.RemoteLauncherClient;
import org.fusesource.testrunner.rmi.RemoteLoadingMain;
import org.fusesource.testrunner.rmi.RemoteClassLoader;
import static org.fusesource.testrunner.Expression.file;
import static org.fusesource.testrunner.Expression.path;
import org.fusesource.rmiviajms.JMSRemoteObject;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;

import java.io.IOException;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author chirino
 */
public class RemoteClassLoaderTest extends TestCase {
    
    BrokerService controlBroker;
    RemoteProcessLauncher agent;
    RemoteLauncherClient clientRemote;


    protected void setUp() throws Exception {
        controlBroker = new BrokerService();
        controlBroker.setBrokerName("RemoteLauncherBroker");
        controlBroker.setPersistent(false);
        controlBroker.addConnector("tcp://localhost:61616");
        controlBroker.start();

        //Set up a launch agent:
        agent = new RemoteProcessLauncher();
        agent.setDataDirectory(new File("target" + File.separator + "testrunner-data"));
        agent.start();

        clientRemote = new RemoteLauncherClient("client1");
        clientRemote.bindAgent(agent.getAgentId());

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
        JMSRemoteObject.resetSystem();
    }

    /**
     * Verify that we can setup a remote launch that does not contain
     * the Main app.
     *
     * @throws Exception
     */
    public void testClassNotFound() throws Exception {
        LaunchDescription ld = new LaunchDescription();
        ld.add("java");
        ld.add("-cp");
        setClassPath(ld);
        ld.add(DataInputTestApplication.class.getName());

        ExitProcessListener exitListener = new ExitProcessListener();
        Process process = clientRemote.launchProcess(agent.getAgentId(), ld, exitListener);
        exitListener.assertExitCode(1);
    }

    public void testInvalidSyntax() throws Exception {
        LaunchDescription ld = new LaunchDescription();
        ld.add("java");
        ld.add("-cp");
        setClassPath(ld);
        ld.add(RemoteLoadingMain.class.getName());
        ld.add(DataInputTestApplication.class.getName());

        ExitProcessListener exitListener = new ExitProcessListener();
        Process process = clientRemote.launchProcess(agent.getAgentId(), ld, exitListener);
        exitListener.assertExitCode(2);
        assertTrue(exitListener.getOutAsString().startsWith("Invalid Syntax:"));
    }

    public void testLoadRemoteClass() throws Exception {
        RemoteClassLoader.ClassLoaderServer server = new RemoteClassLoader.ClassLoaderServer(null, DataInputTestApplication.class.getClassLoader());
        JMSRemoteObject.exportObject(server, new ActiveMQQueue("CLASSLOADER"));

        LaunchDescription ld = new LaunchDescription();
        ld.add("java");
        ld.add("-cp");
        setClassPath(ld);
        ld.add(RemoteLoadingMain.class.getName());
        ld.add("--cache-dir");
        ld.add(file("./classloader-cache"));
        ld.add("--classloader-url");
        ld.add("CLASSLOADER");
        ld.add(DataInputTestApplication.class.getName());

        ExitProcessListener exitListener = new ExitProcessListener();
        Process process = clientRemote.launchProcess(agent.getAgentId(), ld, exitListener);
        process.write(Process.FD_STD_IN, "exit: 5\n".getBytes());

        try {
            exitListener.assertExitCode(5);
        } finally {
            System.out.println(exitListener.getOutAsString());
            System.out.println(exitListener.getErrAsString());
        }
    }

    private void setClassPath(LaunchDescription ld) throws IOException {
        File testClassesLocation = codeLocation(DataInputTestApplication.class);
        ArrayList<Expression.FileExpression> files = new ArrayList<Expression.FileExpression>();
        for (String file : System.getProperty("java.class.path").split(File.pathSeparator)) {
            File t = new File(file).getCanonicalFile();
            // We want to skip the test classes directory...
            if( !t.equals(testClassesLocation) ) {
                files.add(file(file));
            }
        }

        ld.add(path(files));
    }

    private static File codeLocation(Class<?> clazz) throws IOException {
        return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalFile();
    }

    private static class ExitProcessListener implements ProcessListener {

        private final CountDownLatch exitLatch = new CountDownLatch(1);
        private final AtomicInteger exitCode = new AtomicInteger(0);

        private ByteArrayOutputStream out = new ByteArrayOutputStream();
        private ByteArrayOutputStream err = new ByteArrayOutputStream();

        private ArrayList<String> infos = new ArrayList<String>();
        private ArrayList<Throwable> errors = new ArrayList<Throwable>();

        public void assertExitCode(int value) throws InterruptedException {
            assertTrue("timed out waiting for exit", exitLatch.await(10, TimeUnit.SECONDS));
            assertEquals(value, exitCode.get());
        }

        public void onProcessExit(int value) {
            this.exitCode.set(value);
            this.exitLatch.countDown();
        }

        synchronized public void onProcessError(Throwable thrown) {
            errors.add(thrown);
        }

        synchronized public void onProcessInfo(String message) {
            infos.add(message);
        }

        synchronized public void onProcessOutput(int fd, byte[] output) {
            try {
                if( fd == Process.FD_STD_OUT ) {
                    out.write(output);
                } else if( fd == Process.FD_STD_ERR ) {
                    err.write(output);
                }
            } catch (IOException e) {
            }
        }

        synchronized public String getOutAsString() {
            return new String(out.toByteArray());
        }
        synchronized public String getErrAsString() {
            return new String(err.toByteArray());
        }
    }
}