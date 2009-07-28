/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.testrunner;

import org.fusesource.testrunner.LocalProcessLauncher;
import org.fusesource.testrunner.rmi.*;

import java.io.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Revision: 1.1 $
*/
public class LocalProcess implements LocalStreamListener {

    private final Object mutex = new Object();
    private final LaunchDescription ld;
    protected final LocalProcessListener listener;
    private final int pid;

    Thread thread;
    Process process;
    ProcessOutputHandler errorHandler;
    ProcessOutputHandler outputHandler;
    private OutputStream os;

    AtomicBoolean running = new AtomicBoolean();
    private LocalProcessLauncher processLauncher;

    public LocalProcess(LocalProcessLauncher processLauncher, LaunchDescription ld, LocalProcessListener listener, int pid) {
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

            String arg = expression.evaluate();
            cmd[i++] = arg;

            command_line.append('\'');
            command_line.append(arg);
            command_line.append('\'');
        }

        // Evaluate the enviorment...
        String[] env = null;
        if (ld.getEnviorment() != null) {
            env = new String[ld.getEnviorment().size()];
            i = 0;
            for (Map.Entry<String, Expression> entry : ld.getEnviorment().entrySet()) {
                env[i++] = entry.getKey() + "=" + entry.getValue().evaluate();
            }
        }

        File workingDirectory;
        if (ld.getWorkingDirectory() != null) {
            workingDirectory = new File(ld.getWorkingDirectory().evaluate());
        } else {
            workingDirectory = new File(processLauncher.getDataDirectory(), "pid-"+this.pid);
        }
        workingDirectory.mkdirs();

        //Generate the launch string
        String msg = "Launching as: " + command_line + " [pid = " + pid + "] [workDir = " + workingDirectory + "]";
        System.out.println(msg);
        listener.onInfoLogging(msg);

        //Launch:
        synchronized (mutex) {
            process = Runtime.getRuntime().exec(cmd, env, workingDirectory);
            if (process == null) {
                throw new Exception("Process launched failed (returned null).");
            }

            // create error handler
            running.set(true);
            errorHandler = new ProcessOutputHandler(process.getErrorStream(), "Process Error Handler for: " + pid, IRemoteStreamListener.FD_STD_ERR);
            outputHandler = new ProcessOutputHandler(process.getInputStream(), "Process Output Handler for: " + pid, IRemoteStreamListener.FD_STD_OUT);
            os = process.getOutputStream();

            thread = new Thread("Process Watcher for: " + pid) {
                @Override
                public void run() {
                    try {
                        process.waitFor();
                        int exitValue = process.exitValue();
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
        listener.onExit(exitValue);
    }

    public boolean isRunning() {
        synchronized (mutex) {
            return process != null;
        }
    }

    public void kill() {
        if ( running.compareAndSet(true, false) ) {
            try {
                System.out.print("Killing process " + process + " [pid = " + pid + "]");
                process.destroy();
                System.out.println("...DONE.");
            } catch (Exception e) {
                System.err.println("ERROR: destroying process.");
                e.printStackTrace();
            }
        }
    }

    public void open(int fd) throws IOException {
        if (fd != IRemoteStreamListener.FD_STD_IN) {
            throw new IOException("Only IRemoteProcessLauncher.FD_STD_IN is supported");
        }
    }

    public void write(int fd, byte[] data) throws IOException {
        if (fd != IRemoteStreamListener.FD_STD_IN) {
            return;
        }
        os.write(data);
        os.flush();
    }

    public void close(int fd) {
        if (fd != IRemoteStreamListener.FD_STD_IN) {
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
        private final String name;
        private final int fd;
        private final InputStream is;

        public ProcessOutputHandler(InputStream is, String name, int fd) {
            this.is = is;
            this.name = name;
            this.fd = fd;
            Thread m_thread = new Thread(this, name);
            m_thread.start();
        }


        public void run() {
            try {
                listener.open(fd);
            } catch (Throwable e) {
                e.printStackTrace();
                return;
            }

            try {
                byte buffer[] = new byte[1024 * 4];
                while (true) {

                    int count = is.read(buffer);
                    if (count > 0) {
                        byte b[] = new byte[count];
                        System.arraycopy(buffer, 0, b, 0, count);
                        listener.write(fd, b);
                    }

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
