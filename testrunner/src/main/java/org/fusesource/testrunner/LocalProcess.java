/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.testrunner;

import org.fusesource.testrunner.ProcessLauncher;

import java.io.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Revision: 1.1 $
 */
public class LocalProcess implements Process {

    int FD_STD_IN = 0;
    int FD_STD_OUT = 1;
    int FD_STD_ERR = 2;

    private final Object mutex = new Object();
    private final LaunchDescription ld;
    protected final ProcessListener listener;
    private final int pid;

    Thread thread;
    java.lang.Process process;
    ProcessOutputHandler errorHandler;
    ProcessOutputHandler outputHandler;
    private OutputStream os;

    AtomicBoolean running = new AtomicBoolean();
    private ProcessLauncher processLauncher;

    public LocalProcess(ProcessLauncher processLauncher, LaunchDescription ld, ProcessListener listener, int pid) {
        this.processLauncher = processLauncher;
        this.ld = ld;
        this.listener = listener;
        this.pid = pid;
    }

    /**
     * Launches the process.
     */
    public void start() throws Exception {
        if (ld.getCommand().isEmpty()) {
            throw new Exception("LaunchDescription command empty.");
        }

        // Resolve resources (copy them locally:
        for (LaunchResource resource : ld.getResources()) {
            processLauncher.getResourceManager().locateResource(resource);
        }

        // Evaluate the command...
        String[] cmd = new String[ld.getCommand().size()];
        StringBuilder command_line = new StringBuilder();
        boolean first = true;
        int i = 0;
        for (Expression expression : ld.getCommand()) {
            if (!first) {
                command_line.append(" ");
            }
            first = false;

            String arg = expression.evaluate(processLauncher.getProperties());
            cmd[i++] = arg;

            command_line.append('\'');
            command_line.append(arg);
            command_line.append('\'');
        }

        // Evaluate the enviorment...
        String[] env = null;
        if (ld.getEnvironment() != null) {
            env = new String[ld.getEnvironment().size()];
            i = 0;
            for (Map.Entry<String, Expression> entry : ld.getEnvironment().entrySet()) {
                env[i++] = entry.getKey() + "=" + entry.getValue().evaluate();
            }
        }

        File workingDirectory;
        if (ld.getWorkingDirectory() != null) {
            workingDirectory = new File(ld.getWorkingDirectory().evaluate());
        } else {
            workingDirectory = new File(processLauncher.getDataDirectory(), "pid-" + this.pid);
        }
        workingDirectory.mkdirs();

        //Generate the launch string
        String msg = "Launching as: " + command_line + " [pid = " + pid + "] [workDir = " + workingDirectory + "]";
        System.out.println(msg);
        listener.onProcessInfo(msg);

        //Launch:
        synchronized (mutex) {
            process = Runtime.getRuntime().exec(cmd, env, workingDirectory);
            if (process == null) {
                throw new Exception("Process launched failed (returned null).");
            }

            // create error handler
            running.set(true);
            errorHandler = new ProcessOutputHandler(process.getErrorStream(), "Process Error Handler for: " + pid, FD_STD_ERR);
            outputHandler = new ProcessOutputHandler(process.getInputStream(), "Process Output Handler for: " + pid, FD_STD_OUT);
            os = process.getOutputStream();

            thread = new Thread("Process Watcher for: " + pid) {
                @Override
                public void run() {
                    try {
                        process.waitFor();
                        int exitValue = process.exitValue();
                        //Prior to sending exit join the output
                        //handler threads to make sure that all 
                        //data is sent:
                        errorHandler.join();
                        outputHandler.join();
                        onExit(exitValue);
                    } catch (InterruptedException e) {
                    }
                }
            };
            thread.start();
        }

    }

    protected void onExit(int exitValue) {
        running.set(false);
        listener.onProcessExit(exitValue);
    }

    public boolean isRunning() {
        synchronized (mutex) {
            return process != null;
        }
    }

    public void kill() throws Exception {
        if (running.compareAndSet(true, false)) {
            try {
                System.out.print("Killing process " + process + " [pid = " + pid + "]");
                process.destroy();
                thread.join();
                System.out.println("...DONE.");
            } catch (Exception e) {
                System.err.println("ERROR: destroying process " + process + " [pid = " + pid + "]");
                throw e;
            }
        }
    }

    public void open(int fd) throws IOException {
        if (fd != FD_STD_IN) {
            throw new IOException("Only IRemoteProcessLauncher.FD_STD_IN is supported");
        }
    }

    public void write(int fd, byte[] data) throws IOException {
        if (fd != FD_STD_IN) {
            return;
        }
        os.write(data);
        os.flush();
    }

    public void close(int fd) {
        if (fd != FD_STD_IN) {
            return;
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // handle output or error data
    private class ProcessOutputHandler implements Runnable {
        private final int fd;
        private final BufferedInputStream is;
        private static final int MAX_CHUNK_SIZE = 8 * 1024;
        Thread thread;

        public ProcessOutputHandler(InputStream is, String name, int fd) {
            this.is = new BufferedInputStream(is, MAX_CHUNK_SIZE);
            this.fd = fd;
            thread = new Thread(this, name);
            thread.start();
        }

        public void join() throws InterruptedException {
            thread.join();
        }

        public void run() {

            try {

                byte b = -1;
                while (true) {

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(MAX_CHUNK_SIZE);
                    b = (byte) is.read();
                    if (b == -1) {
                        throw new EOFException();
                    }

                    baos.write(b);

                    while (is.available() > 0 && baos.size() < MAX_CHUNK_SIZE) {
                        b = (byte) is.read();
                        if (b == -1) {
                            throw new EOFException();
                        }
                        baos.write(b);
                    }

                    listener.onProcessOutput(fd, baos.toByteArray());

                }
            } catch (EOFException expected) {
            } catch (Exception e) {
                if (running.get()) {
                    System.out.println("ERROR: reading from process' output or  error stream.");
                    e.printStackTrace();
                }
            }
        }
    }

}//private class ProcessServer
