/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.testrunner.rmi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Revision: 1.1 $
*/
public class LocalProcessExecutor implements ProcessExecutor {

    private final Object mutex = new Object();
    private final LaunchDescription ld;
    private final IProcessListener listener;
    private final int pid;

    Thread thread;
    Process process;
    ProcessOutputHandler errorHandler;
    ProcessOutputHandler outputHandler;
    private OutputStream os;

    AtomicBoolean running = new AtomicBoolean();
    private ProcessLauncher processLauncher;

    public LocalProcessExecutor(ProcessLauncher processLauncher, LaunchDescription ld, IProcessListener listener, int pid) throws RemoteException {
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
            workingDirectory = new File(processLauncher.getProcessMonitor().getTempDirectory() + File.separator + this.pid);
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
            errorHandler = new ProcessOutputHandler(process.getErrorStream(), "Process Error Handler for: " + pid, IStream.FD_STD_ERR);
            outputHandler = new ProcessOutputHandler(process.getInputStream(), "Process Output Handler for: " + pid, IStream.FD_STD_OUT);
            os = process.getOutputStream();

            running.set(true);

            errorHandler.start();
            outputHandler.start();
        }

    }

    public boolean isRunning() {
        synchronized (mutex) {
            return process != null;
        }
    }

    public void kill() {
        running.set(false);
        synchronized (mutex) {
            //Destroy the process:
            if (process != null) {
                try {
                    System.out.print("Killing process " + process + " [pid = " + pid + "]");
                    process.destroy();
                    process.waitFor();
                    int exitValue = process.exitValue();
                    listener.onExit(exitValue);

                    System.out.println("...DONE.");
                    process = null;
                } catch (Exception e) {
                    System.err.println("ERROR: destroying process.");
                    e.printStackTrace();
                }
            }
        }

        try {
            errorHandler.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            outputHandler.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void ping(long timeout) {
        if (listener != null) {
            //Check to see if the controller is still around for the process:
            try {
                listener.ping();
            } catch (Exception e) {
                try {
                    kill();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    public void open(int fd) throws RemoteException, IOException {
        if (fd != IStream.FD_STD_IN) {
            throw new IOException("Only IProcessLauncher.FD_STD_IN is supported");
        }
    }

    public void write(int fd, byte[] data) throws RemoteException {
        if (fd != IStream.FD_STD_IN) {
            return;
        }
        try {
            os.write(data);
            os.flush();
        } catch (IOException e) {
        }
    }

    public void close(int fd) throws RemoteException {
        if (fd != IStream.FD_STD_IN) {
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
        Thread m_thread;

        public ProcessOutputHandler(InputStream is, String name, int fd) {
            this.is = is;
            this.name = name;
            this.fd = fd;
        }

        public void start() {
            m_thread = new Thread(this, name);
            m_thread.start();
        }

        public void stop() throws InterruptedException {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (m_thread != null) {
                m_thread.interrupt();
                m_thread.join();
                m_thread = null;
            }
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
                        // TODO: we might want to local echo for easier debugging??
                        //                            if( fd == IStream.FD_STD_OUT ) {
                        //                                System.out.write(b);
                        //                            } else if( fd == IStream.FD_STD_ERR ) {
                        //                                System.err.write(b);
                        //                            }
                        listener.write(fd, b);
                    }

                }
            } catch (Exception e) {
                if (running.get()) {
                    System.out.println("ERROR: reading from process' output or  error stream.");
                    e.printStackTrace();
                }
            }
        }
    }

}//private class RemoteProcess
