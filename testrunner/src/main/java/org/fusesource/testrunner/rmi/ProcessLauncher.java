package org.fusesource.testrunner.rmi;

import org.fusesource.rmiviajms.JMSRemoteObject;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.Destination;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

/**
 * @author chirino
 */
public class ProcessLauncher implements IProcessLauncher {
    private static final long CLEANUP_TIMEOUT = 60000;

    private String exclusiveOwner;
    private ProcessMonitor processMonitor;

    private String agentId; //The unique identifier for this agent (specified in ini file);
    private boolean started = false;
    private String m_dataDir = ".";

    //ProcessHandlers:
    private final HashMap<Integer, RemoteProcess> processes = new HashMap<Integer, RemoteProcess>();
    int pid_counter=0;
    private Thread shutdownHook;
    private IProcessLauncher proxy;

    /**
     * private class ProcessHandler
     * <p/>
     * Handles launching a process using Runtime.exec(). Utilizes a
     * ProcessCommunicator object to communicate with the launched process.
     * Output from the process is relayed to the entity that requested the
     * launch via TRAgent's TestRunnerJMSCommunicator object.
     */
    public class RemoteProcess extends JMSRemoteObject implements IProcess {

        private final Object mutex = new Object();
        private final LaunchDescription ld;
        private final IProcessListener listener;
        private final int pid;

        Thread thread;
        Process m_process;
        ProcessOutputHandler m_errorHandler;
        ProcessOutputHandler m_outputHandler;
        private OutputStream os;

        AtomicBoolean running = new AtomicBoolean();

        /**
         * Upon return the ProcessHandler can be started. The initialization
         * file is reloaded.
         */
        public RemoteProcess(LaunchDescription ld, IProcessListener listener, int pid) throws RemoteException {
            this.ld = ld;
            this.listener = listener;
            this.pid = pid;
        }


        /**
         * Interprets the launch description and generates a command line to be
         * spawned. Creates a classloader to dynamically loadClasses.
         * LAUNCH_SUCCESS or LAUNCH_FAILURE is sent to the entity that requested
         * the launch.
         */
        public void start() throws Exception {
            if( ld.getCommand().isEmpty() ) {
                throw new Exception("LaunchDescription command empty.");
            }


            // Evaluate the command...
            String [] cmd = new String[ld.getCommand().size()];
            StringBuilder command_line = new StringBuilder();
            boolean first = true;
            int i=0;
            for (Expression expression : ld.getCommand()) {
                if( !first ) {
                    command_line.append(" ");
                }
                first=false;

                String arg = expression.evaluate();
                cmd[i++] = arg;

                command_line.append('\'');
                command_line.append(arg);
                command_line.append('\'');
            }

            // Evaluate the enviorment...
            String[] env = null;
            if( ld.getEnviorment()!=null ) {
                env = new String[ld.getEnviorment().size()];
                i=0;
                for (Map.Entry<String, Expression> entry : ld.getEnviorment().entrySet()) {
                    env[i++] = entry.getKey()+"="+entry.getValue().evaluate();
                }
            }

            File workingDirectory;
            if( ld.getWorkingDirectory()!=null ) {
                workingDirectory = new File(ld.getWorkingDirectory().evaluate());
            } else {
                workingDirectory = new File(processMonitor.m_tempDir + File.separator + this.pid);
            }
            workingDirectory.mkdirs();

            //Generate the launch string
            String msg = "Launching as: " + command_line + " [pid = " + pid + "] [workDir = " + workingDirectory + "]";
            System.out.println(msg);
            listener.onInfoLogging(msg);

            //Launch:
            synchronized (mutex) {
                m_process = Runtime.getRuntime().exec(cmd, env, workingDirectory);
                if (m_process == null) {
                    throw new Exception("Process launched failed (returned null).");
                }

                // create error handler
                m_errorHandler = new ProcessOutputHandler(m_process.getErrorStream(), "Process Error Handler for: " + pid, IStream.FD_STD_ERR);
                m_outputHandler = new ProcessOutputHandler(m_process.getInputStream(), "Process Output Handler for: " + pid, IStream.FD_STD_OUT);
                os = m_process.getOutputStream();


                running.set(true);

                m_errorHandler.start();
                m_outputHandler.start();
            }
            
        }

        public boolean isRunning() {
            synchronized (mutex) {
                return m_process!=null;
            }
        }

        public void kill() {
            running.set(false);
            synchronized (mutex) {
                //Destroy the process:
                if (m_process != null) {
                    try {
                        System.out.print("Killing process " + m_process + " [pid = " + pid + "]");
                        m_process.destroy();
                        m_process.waitFor();
                        int exitValue = m_process.exitValue();
                        listener.onExit(exitValue);

                        System.out.println("...DONE.");
                        m_process = null;
                    } catch (Exception e) {
                        System.out.println("ERROR: destroying process.");
                        e.printStackTrace();
                    }
                }
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
            if( fd!= IStream.FD_STD_IN ) {
                throw new IOException("Only IProcessLauncher.FD_STD_IN is supported");
            }
        }

        public void write(int fd, byte[] data) throws RemoteException {
            if( fd!= IStream.FD_STD_IN ) {
                return;
            }
            try {
                os.write(data);
                os.flush();
            } catch (IOException e) {
            }
        }

        public void close(int fd) throws RemoteException {
            if( fd!= IStream.FD_STD_IN ) {
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

            public void start () {
                m_thread = new Thread(this, name);
                m_thread.start();
            }

            public void stop() throws InterruptedException {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                m_thread.interrupt();
                m_thread.join();
                m_thread = null;
            }

            public void run() {
                try {
                    listener.open(fd);
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }

                try {
                    byte buffer[] = new byte[1024*4];
                    while (true) {

                        int count = is.read(buffer);
                        if( count > 0 ) {
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
                    if( running.get() ) {
                        System.out.println("ERROR: reading from process' output or  error stream.");
                        e.printStackTrace();
                    }
                }
            }
        }


    }//private class RemoteProcess


    private class ProcessMonitor implements Runnable {
        Thread m_thread;
        private String m_tempDir;
        private boolean cleanupRequested = false;

        public ProcessMonitor() {
            m_tempDir = m_dataDir + File.separator + getAgentId() + File.separator + "temp";
            m_thread = new Thread(this, getAgentId() + "-Process Monitor");
            m_thread.start();
        }

        public void run() {
            while (true) {
                synchronized (this) {
                    try {
                        wait(CLEANUP_TIMEOUT);
                    } catch (java.lang.InterruptedException ie) {
                        cleanupRequested = true;
                        return;
                    } finally {
                        checkForRogueProcesses(15000);
                        if (cleanupRequested) {
                            cleanUpTempFiles();
                            cleanupRequested = false;
                        }
                    }
                }

            }
        }

        public void shutdown() {
            m_thread.interrupt();
            try {
                m_thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void cleanUpTempFiles() {
            //If we aren't running anything cleanup: temp parts
            if (processes == null || processes.size() == 0) {
                File tempDir = new File(m_tempDir);
                String[] subDirs = tempDir != null ? tempDir.list() : null;

                System.out.println("*************Cleaning up temporary parts*************");
                for (int i = 0; subDirs != null && i < subDirs.length; i++) {
                    try {
                        recursiveDelete(tempDir + File.separator + subDirs[i]);
                    } catch (Exception e) {
                        System.out.println("ERROR cleaning up temporary parts:");
                        e.printStackTrace();
                    }
                }
            }
        }

        public void checkForRogueProcesses(long timeout) {
            for (RemoteProcess remoteProcess : processes.values()) {
                remoteProcess.ping(timeout);
            }
        }

        /**
         * Requests cleanup of temporary files
         */
        public synchronized void requestCleanup() {
            cleanupRequested = true;
            notify();
        }

    }


    synchronized public void bind(String owner) throws Exception {
        if (exclusiveOwner == null) {
            exclusiveOwner = owner;
            processMonitor.checkForRogueProcesses(0);
            System.out.println("Now bound to: " + exclusiveOwner);
            return;
        } else if (!exclusiveOwner.equals(owner)) {
            throw new Exception("Bind failure, already bound: " + exclusiveOwner);
        } else {
            return;
        }
    }

    synchronized public void unbind(String owner) throws Exception {
        if (exclusiveOwner == null) {
            return;
        } else if (exclusiveOwner.equals(owner)) {
            System.out.println("Bind to " + exclusiveOwner + " released");
            exclusiveOwner = null;
            processMonitor.requestCleanup();
            return;
        } else {
            throw new Exception("Release failure, different owner: " + exclusiveOwner);
        }
    }


    synchronized public IProcess launch(LaunchDescription launchDescription, IProcessListener handler) throws Exception {
        int pid = pid_counter++;
        RemoteProcess rc = new RemoteProcess(launchDescription, handler, pid);
        processes.put(pid, rc);
        try {
            rc.start();
        } catch (Exception e) {
            processes.remove(pid);
        }
        return rc;
    }

    public synchronized void start() throws Exception {
        if (started) {
            return;
        }

        started = true;
        if (agentId == null) {

            try {
                setAgentId(java.net.InetAddress.getLocalHost().getHostName());
            } catch (java.net.UnknownHostException uhe) {
                System.out.println("Error determining hostname.");
                uhe.printStackTrace();
                setAgentId("UNDEFINED");
            }
        }

        shutdownHook = new Thread(getAgentId() + "-Shutdown") {
            public void run() {
                System.out.println("Executing Shutdown Hook for " + ProcessLauncher.this);
                ProcessLauncher.this.stop();
            }
        };

        Runtime.getRuntime().addShutdownHook(shutdownHook);
        processMonitor = new ProcessMonitor();

        Destination destination = new ActiveMQQueue(agentId);
        proxy = (IProcessLauncher) JMSRemoteObject.exportObject(this, destination);
    }

    public synchronized void stop() {
        if (!started) {
            return;
        }

        if (Thread.currentThread() != shutdownHook) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }

        try {
            JMSRemoteObject.unexportObject(proxy, false);
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }

        started = false;


        for (RemoteProcess process : processes.values()) {
            process.kill();
        }
        processes.clear();

        //Stop the process monitor:
        processMonitor.shutdown();
    }


    /**
     * Sets the name of the agent id. Once set it cannot be changed.
     *
     * @param id
     *            the name of the agent id.
     */
    public void setAgentId(String id) {
        if (agentId == null && id != null) {
            agentId = id.trim().toUpperCase();
        }
    }
    
    /**
     * Sets the base directory where the agent puts it's data.
     *
     * @param dataDirectory
     */
    public void setDataDirectory(String dataDirectory) {
        m_dataDir = dataDirectory;
    }

    /**
     */
    public String getDataDirectory() {
        return m_dataDir;
    }

    /**
     * @return This agent's id.
     */
    public String getAgentId() {
        return agentId;
    }

    private static void checkDirectoryDepth(String path, String message, int minDepth) throws Exception {
        int depth = 0;
        int index = -1;
        if (path.startsWith(File.separator + File.separator)) {
            depth -= 2;
        } else if (path.startsWith(File.separator)) {
            depth--;
        }

        while (true) {
            index = path.indexOf(File.separator, index + 1);
            if (index == -1) {
                break;
            } else {
                depth++;
            }
        }

        if (minDepth > depth)
            throw new Exception(message);
    }

    private static final void recursiveDelete(String srcDir) throws IOException, Exception {
        //String srcFileName = "";
        String[] fileList = null;

        //Just delete and return if a file is specified:
        File srcFile = new File(srcDir);

        //Check to make sure that we aren't deleting a root or first level directory:
        checkDirectoryDepth(srcFile.getAbsolutePath(), "Directory depth is too shallow to risk recursive delete for path: " + srcFile.getAbsolutePath()
                + " directory depth should be at least 2 levels deep.", 2);

        if (!srcFile.exists()) {
            return;
        } else if (srcFile.isFile()) {
            int retries = 0;
            while (!srcFile.delete()) {
                if (retries > 20) {
                    throw new IOException("ERROR: Unable to delete file: " + srcFile.getAbsolutePath());
                }
                retries++;
            }
        } else {
            fileList = srcFile.list();
            // Copy parts from cd to installation directory
            for (int j = 0; j < fileList.length; j++) {
                //Format file names
                recursiveDelete(srcDir + File.separator + fileList[j]);
            }
            //Finally once all leaves are deleted delete this node:
            int retries = 0;

            while (!srcFile.delete()) {
                if (retries > 20) {
                    throw new IOException("ERROR: Unable to delete directory. Not empty?");
                }
                retries++;
            }
        }
    }//private void recursiveDelete(String dir)

}