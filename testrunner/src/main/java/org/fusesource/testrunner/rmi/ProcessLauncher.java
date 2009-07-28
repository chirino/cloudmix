package org.fusesource.testrunner.rmi;

import org.apache.activemq.command.ActiveMQQueue;
import org.fusesource.rmiviajms.JMSRemoteObject;

import javax.jms.Destination;
import java.io.File;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chirino
 */
public class ProcessLauncher implements IProcessLauncher {
    public static final long CLEANUP_TIMEOUT = 60000;

    private String exclusiveOwner;
    private ProcessMonitor processMonitor;

    private String agentId; //The unique identifier for this agent (specified in ini file);
    private boolean started = false;
    private String dataDirectory = ".";

    //ProcessHandlers:
    private final Map<Integer, ProcessExecutor> processes = new HashMap<Integer, ProcessExecutor>();
    int pidCounter = 0;
    private Thread shutdownHook;
    private IProcessLauncher proxy;

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
        int pid = pidCounter++;
        ProcessExecutor rc = createProcessExecutor(launchDescription, handler, pid);
        processes.put(pid, rc);
        try {
            rc.start();
        } catch (Exception e) {
            processes.remove(pid);
        }
        return rc;
    }

    protected ProcessExecutor createProcessExecutor(LaunchDescription launchDescription, IProcessListener handler, int pid) throws RemoteException {
        return new RmiProcessExecutor(this, launchDescription, handler, pid);
        //return new LocalProcessExecutor(this, launchDescription, handler, pid);
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
        processMonitor = new ProcessMonitor(this);

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
            //e.printStackTrace();
        }

        started = false;

        for (ProcessExecutor process : processes.values()) {
            try {
                process.kill();
            } catch (RemoteException e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
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
        this.dataDirectory = dataDirectory;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    /**
     * @return This agent's id.
     */
    public String getAgentId() {
        return agentId;
    }

    public ProcessMonitor getProcessMonitor() {
        return processMonitor;
    }

    public Map<Integer, ProcessExecutor> getProcesses() {
        return processes;
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

    static final void recursiveDelete(String srcDir) throws IOException, Exception {
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

    public String toString() {
        return "ProcessLauncer-" + getAgentId();
    }

    /*
     * public static void main()
     * 
     * Defines the entry point into this app.
     */
    public static void main(String[] argv) {
        System.out.println("\n\n PROCESS LAUNCHER\n");

        String jv = System.getProperty("java.version").substring(0, 3);
        if (jv.compareTo("1.5") < 0) {
            System.err.println("The ProcessLauncher agent requires jdk 1.4 or higher to run, the current java version is " + System.getProperty("java.version"));
            System.exit(-1);
            return;
        }

        if (argv.length > 1) {
            System.err.println("Too many arguments.");
            System.exit(-1);
        }
        ProcessLauncher agent = new ProcessLauncher();
        //        agent.setPropFileName(argv[0]);
        try {
            agent.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}