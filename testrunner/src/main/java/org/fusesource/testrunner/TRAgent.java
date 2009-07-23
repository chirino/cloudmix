/*
 * Copyright (c) 1999, 2000, 2001 Sonic Software Corporation. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Progress Software Corporation.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Progress.
 *
 * PROGRESS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. PROGRESS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.fusesource.testrunner;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;
import java.util.Date;
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.lang.Process;

import org.fusesource.testrunner.TRCommunicator.TRComHandler;

/**
 * public class TRAgent
 * 
 * 
 * A TRAgent should be started on any machine intended to be used to remotely
 * launch applications. A TRA.ini file is expected in the working directory for
 * which the agent is launched. The TRA.ini file must set values for (at least)
 * the property TR_SERVER_URL, which specifies the url to connect to the Sonic
 * control broker that the agent will use to receive launch requests. The
 * TRA_NAME property defaults to the machine name. TRA_NAME may be specified in
 * the rare case in which two or more TRAgents are run on the same machine.<br>
 * <br>
 * TRA_PORT may be specified for launched processes to use as an object output
 * channel. This is desirable if the launched application writes to System.out,
 * but wishes to communicate with the TRAgent through an object (output)
 * channel.
 * 
 * Once started the TRAgent awaits for controllers to send it a TRLaunchDescr
 * object that contains the information necessary to launch either a Java
 * program or a script. Controllers must use a TRJMSCommunicator to communicate
 * with the agent and may wish to first 'bind' the TRAgent. Once the TRAgent is
 * bound other potential controllers will not be able to launch programs through
 * this agent and therefore will not interfere with the process being run for
 * the controller that bound the agent.<br>
 * <br>
 * 
 * TRAgent is capable of running multiple processes simultaneously with no limit
 * aside from the system resources upon which it is running. However the
 * controller that launched each spawned process or script must remain connected
 * to the TRAgent in order for its process to continue executing. If the TRAgent
 * detects that the controller is gone, any processes it is running will be
 * terminated. Similarly, if a controller has bound the TRAgent and disconnects
 * from the control broker by closing its TRJMSCommunicator, its exclusive bind
 * on the agent will be released the next time another controller requests a
 * bind from the TRAgent. <br>
 * <br>
 * 
 * Aside from TRA_NAME and TR_SERVER_URL, the TRA.ini file will contain tags
 * that tell the TRAgent the paths to programs 'known' to it. There are two
 * types of tags: CLASSPATH tags and JVM tags. See TRLaunchDescr for more
 * information on how the tags are used. Each time a process is launched the
 * TRA.ini file is reread so that new tags can be added during runtime.<br>
 * <br>
 * 
 * <i>A typical scenario using the TRAgent would be the following:</i><br>
 * <br>
 * 
 * <b>The control side:</b><br>
 * 1.)The TRAgent is started. It will connect to the Control broker using
 * TRA_NAME as its unique clientID<br>
 * 2.)A controller application connects to the control broker with its own
 * unique id using a TRJMSCommunicator.<br>
 * 3.)Using its TRJMSCommunicator the controller binds the TRAgent<br>
 * 4.)The controller app sends a TRLaunchDescr to the TRAgent<br>
 * 5.)The TRAgent uses a TRMsg to send a LAUNCH_SUCCESS or a TRErrorMsg to send
 * a LAUNCH_FAILURE back to the controller. If a LAUNCH_SUCCESS success is sent
 * back, the controller must examine the properties sent with the message to
 * obtain the launched process' PID.<br>
 * 6.)The TRAgent is now listening for output from the launched process and
 * sending it back to the controller the agent is also listening for message
 * from the controller and relaying them to the launched process. To indicate
 * that the message is intended for this process the controller must set a
 * property with the PID of the process.<br>
 * 7.)If at any point the TRAgent receives a KILL_DESCR (with PID set) from the
 * controller it will kill the process. And sends back a TRMsg with an Integer
 * EXIT_VALUE property and KILL_SUCCESS as its content, or a TRErrorMsg with
 * KILL_ERROR as its content.<br>
 * 8.)If the process finishes before a KILL_DESCR is sent the agent sends back a
 * TRMsg with PROCESS_DONE as its content<br>
 * . 9.)At any point the controller can use it TRJMSCommunicator to release the
 * bind to the agent which has no effect on the process running other than
 * allowing other controlling entities to send messages to the process or launch
 * other processes.<br>
 * <br>
 * 
 * <b>The process side:</b><br>
 * The process that is run can be virtually anything that could be run from a
 * normal command line. In fact TRScriptLauncher can be used to run anything
 * that can be run from the command line of the TRAgent's working directory.
 * Similarly a TRLaunchDescr can be used with only setRunName() filled in and
 * processType set to script to run just about anything. The java exe type is
 * added only as a convenience and is the prefered way of running Java
 * Executables. When a Java Executable is being run the output type can be
 * either Object or Data. The latter is used to read the output of regular
 * program that dumps string data to system.out. However develops may write
 * application intended for use with TestRunner. These applications may use a
 * TRProcessCommunicator to facilitate the passing of objects instead of string
 * data through it's standard input and output streams.<br>
 * NOTE: If Object output is specified TestRunner will not be able to handle
 * non-serialized object data passed through the process' output stream, i.e. if
 * the launched process makes a call to System.out.print() TRAgent will fail to
 * read the output if it is expecting object output.<br>
 * <br>
 * 
 * <b>Dynamic Class Loading:</b><br>
 * Classes are dynamically loaded when a TRLaunchDescr is received with
 * exposedClasses set. This is necessary so that TRAgent does not need to be
 * launched with the classes of every application that it runs in its own
 * classpath. Dynamically loaded foreign classes as they are passed through the
 * stream keeps TRAgent independant of the programs it is running.
 * 
 * <b>Control Broker Crash Recover</b><br>
 * If the agent loses its connection to the control broke it will attempt to
 * reconnect to it every 30 in the attempt to remain available for potential
 * controllers.
 * 
 * <b>Supported JVM: 1.3 or higher.<b>
 * 
 * @progress.com)
 * 
 * @author Colin MacNaughton (cmacnaug
 * @version 1.0
 * @since 1.0
 * @see TRJMSCommunicator
 * @see TRProcessCommunicator
 * @see TRLaunchDescr
 */

public class TRAgent {
    private static final boolean devDebug = false;
    private static boolean classDebug = Boolean.getBoolean("org.fuse.testrunner.TRAgent.debug");
    private static boolean packageDebug = Boolean.getBoolean("org.fuse.testrunner.debug");
    private static boolean globalDebug = Boolean.getBoolean("debug");
    private static boolean cmdDebug = devDebug || classDebug || packageDebug || globalDebug;
    private static boolean DEBUG = cmdDebug;

    static final String COMMAND_BIND = "BIND";
    static final String COMMAND_RELEASE = "RELEASE";
    static final String COMMAND_LAUNCH = "LAUNCH";
    static final String COMMAND_KILL = "KILL";
    static final String PROP_COMMAND_RESPONSE = "CMD_RESP";

    private static final long CLEANUP_TIMEOUT = 60000;

    /**
     * The key for the property returned to indicate a successfully launched
     * process' process id. The controller of the process can access this
     * property by calling getProperties on its TRJMSCommunicator after
     * receiving a TRMsg with LAUNCH_SUCCESS as its content or any message from
     * a launched process thereafter
     * 
     * @since 1.0
     */
    public static final String PID = "PID";

    //Sent in some TRMsgs so that the sender can correlate a response to a request:
    public static final String TR_REQ_TRACKING = "TR_REQ_TRACKING";

    // Property to hold PID to pass to launched process
    public static final String PID_PROPERTY = "org.fusesource.testrunner.tragent.pid";

    // Property to hold optional port to pass to launched process
    public static final String PORT_PROPERTY = "org.fusesource.testrunner.tragent.port";

    private static final long PORT_CONNECT_TIMEOUT = 30000; // 30 secs

    //Url of remote controller:
    private String controlUrl;

    //Handler for remote commands:
    private RemoteCommandHandler commandListener;

    //ProcessHandlers:
    private Hashtable m_procHandlers;

    //ProcessAcceptor: optional acceptor for a port for process object output
    private int m_port = -1;
    private ProcessAcceptor m_processAcceptor;

    //ProcessMonitor: Periodically tries to clean up abandoned processes:
    private ProcessMonitor m_processMonitor;

    //Used to generate unique process ids
    private int m_pidCount;

    //Properties file info
    private String m_propFileName;

    private Properties properties = new Properties();

    private String m_agentID; //The unique identifier for this agent (specified in ini file);

    private boolean started = false;
    private String m_dataDir = ".";

    private String exclusiveOwner;

    private Thread shutdownHook;

    /*
     * public TestRunnerControl
     * 
     * Constructor
     */
    public TRAgent() {
        m_pidCount = 0;
    }

    /**
     * Sets the name of the properties that holds configuration information for
     * this agent.
     * 
     * When set the prop file will be read each time a launch is requested to
     * allow updates to it on the fly.
     * 
     * @param propFile
     *            The name of the properties that holds configuration
     *            information for this agent.
     */
    public void setPropFileName(String propFile) {
        m_propFileName = propFile;
    }

    /**
     * Sets the connect url for the control broker to which this agent should
     * connect.
     * 
     * @param url
     *            The connect url for the control broker to which this agent
     *            should connect.
     */
    public void setControlUrl(String url) {
        controlUrl = url;
    }

    /**
     * Sets the name of the agent id. Once set it cannot be changed.
     * 
     * @param id
     *            the name of the agent id.
     */
    public void setAgentId(String id) {
        if (m_agentID == null && id != null) {
            m_agentID = id.trim().toUpperCase();
        }
    }

    /**
     * When java processes are launced and set to use Object input, this port is
     * set to listen for the launched processes object output.
     * 
     * @param port
     *            The port to listen on.
     */
    public void setProcessListenPort(int port) {
        m_port = port;
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
     * Sets the base directory where the agent puts it's data.
     * 
     * @param dataDirectory
     */
    public String getDataDirectory() {
        return m_dataDir;
    }

    /**
     * @return This agent's id.
     */
    public String getAgentId() {
        return m_agentID;
    }

    public void bind(String owner) throws Exception {
        if (exclusiveOwner == null) {
            exclusiveOwner = owner;
            m_processMonitor.checkForRogueProcesses(0);
            System.out.println("Now bound to: " + exclusiveOwner);
            return;
        } else if (!exclusiveOwner.equals(owner)) {
            throw new Exception("Bind failure, already bound: " + exclusiveOwner);
        } else {
            return;
        }
    }

    public void release(String owner) throws Exception {
        if (exclusiveOwner == null) {
            return;
        } else if (exclusiveOwner.equals(owner)) {
            System.out.println("Bind to " + exclusiveOwner + " released");
            exclusiveOwner = null;
            m_processMonitor.requestCleanup();
            return;
        } else {
            throw new Exception("Release failure, different owner: " + exclusiveOwner);
        }
    }

    public int kill(TRProcessContext ctx) throws Exception {

        ProcessHandler procHandler = (ProcessHandler) m_procHandlers.get(ctx.getPid());
        int exit;
        if (procHandler == null) {
            return 0;
        } else {
            try {
                exit = procHandler.kill(false);
            } finally {
                m_procHandlers.remove(new Integer(procHandler.m_pid));
            }
        }

        m_processMonitor.requestCleanup();

        return exit;
    }

    public TRProcessContext launch(TRLaunchDescr ld, ProcessListener listener) throws Exception {
        int pid = m_pidCount++;
        ProcessHandler procHandler = new ProcessHandler(listener, pid);
        try {
            procHandler.launch(ld);
            m_procHandlers.put(new Integer(pid), procHandler);
        } catch (Exception exception) {
            try {
                procHandler.kill(true);
            } catch (Exception e) {
            }
            throw exception;
        }
        return procHandler.getProcessContext();
    }

    public synchronized void start() throws Exception {
        if (started) {
            return;
        }

        started = true;

        readPropFile();

        if (m_agentID == null) {

            try {
                setAgentId(java.net.InetAddress.getLocalHost().getHostName());
            } catch (java.net.UnknownHostException uhe) {
                System.out.println("Error determining hostname. Edit " + m_propFileName + " to set TRA_NAME explicitly.");
                uhe.printStackTrace();
                setAgentId("UNDEFINED");
            }
        }

        shutdownHook = new Thread(getAgentId() + "-Shutdown") {
            public void run() {
                System.out.println("Executing Shutdown Hook for " + TRAgent.this);
                TRAgent.this.stop();

            }
        };

        Runtime.getRuntime().addShutdownHook(shutdownHook);

        m_procHandlers = new Hashtable();
        m_processMonitor = new ProcessMonitor();

        if (m_port >= 0) {
            try {
                m_processAcceptor = new ProcessAcceptor(m_port);
                System.out.println(m_agentID + ": Created acceptor for port " + m_port);
            } catch (IOException ioe) {
                System.out.println("ERROR: Creating acceptor for port " + m_port + " - " + ioe);
                ioe.printStackTrace();
                stop();
            }
        }

        if (controlUrl != null) {
            try {
                commandListener = new RemoteCommandHandler(controlUrl);
            } catch (Exception e) {
                stop();
                throw e;
            }
            commandListener.start();
        }
    }

    public synchronized void stop() {
        if (!started) {
            return;
        }

        if (Thread.currentThread() != shutdownHook) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }

        started = false;

        //Kill all processes:
        Iterator procHandlers = m_procHandlers.values().iterator();
        while (procHandlers.hasNext()) {
            ProcessHandler handler = (ProcessHandler) procHandlers.next();
            try {
                handler.kill(true);
            } catch (Exception e) {
            }
        }
        m_procHandlers.clear();

        //Stop the process monitor:
        m_processMonitor.shutdown();

        //Close the process acceptor:
        if (m_processAcceptor != null) {
            m_processAcceptor.close();
            m_processAcceptor = null;
        }

        if (commandListener != null) {
            commandListener.stop();
        }

    }

    private void readPropFile() {

        if (m_propFileName == null) {
            String defPropFile = m_dataDir + File.separator + "TRA.ini";

            if (new File(defPropFile).exists()) {
                m_propFileName = defPropFile;
            }
        }

        if (m_propFileName != null) {
            properties = new Properties();

            try {
                properties.load(new FileInputStream(m_propFileName));
            } catch (java.io.IOException ioe) {
                System.out.println("ERROR: reading " + m_propFileName + ".");
            }

            if (m_agentID != null) {
                setAgentId(properties.getProperty("TRA_NAME"));
            }

            // if a port is specified, create a server socket and acceptor thread
            m_port = -1;
            String portString = properties.getProperty("TRA_PORT");
            if (portString != null)
                try {
                    m_port = Integer.parseInt(portString);
                } catch (NumberFormatException e) {
                }

            controlUrl = properties.getProperty("TR_SERVER_URL", "");
        }
    }

    /*
     * private boolean recursiveFileCopy (String dir) recursively copies files
     * from one directory into another:
     */
    private static void recursiveFileCopy(String srcDir, String destDir) throws Exception {
        String srcFileName = "";
        String destFileName = "";
        String[] fileList = null;

        // Copy doc files to installation directory
        // List existing files
        fileList = (new File(srcDir)).list();

        //Check to ensure that the target directory exists:
        //If not create it 'cause we got some files to put in there.
        File destFileDir = new File(destDir);
        if (!destFileDir.exists()) {
            destFileDir.mkdir();
        }

        // Copy files from cd to installation directory
        for (int j = 0; j < fileList.length; j++) {
            //Format file names
            srcFileName = srcDir + File.separator + fileList[j];
            destFileName = destDir + File.separator + fileList[j];

            // Copy to CD file to destination
            File sF = new File(srcFileName);
            File dF = new File(destFileName);
            if (sF.isFile()) {
                copyFile(sF, dF);
            }
            // If the source is a directory then we copy its files:
            else if (sF.isDirectory()) {
                //Copy that directory recursively
                recursiveFileCopy(srcFileName, destFileName);
            }
        }
    }//private void recursiveFileCopy (String dir)

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
            // Copy files from cd to installation directory
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

    /**
     * @param currentFile
     * @param newFile
     */
    private static void copyFile(File currentFile, File newFile) throws Exception {
        if (DEBUG)
            System.out.println("Copying locally: " + newFile.getAbsolutePath());

        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }
        // Copy current file to new file
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(currentFile));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));

        byte buf[] = new byte[65536];
        int len;
        while ((len = in.read(buf)) != -1)
            out.write(buf, 0, len);

        // Close files
        in.close();
        out.close();
    }//public void copyFile (File currentFile, File newFile)

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("Arguments: iniFileName");
    }

    /**
     * private class ProcessHandler
     * 
     * Handles launching a process using Runtime.exec(). Utilizes a
     * ProcessCommunicator object to communicate with the launched process.
     * Output from the process is relayed to the entity that requested the
     * launch via TRAgent's TestRunnerJMSCommunicator object.
     * 
     */
    private class ProcessHandler implements Runnable {

        String m_currentProcessString;
        TRProcessCommunicator m_processCom;
        Thread m_thread;
        Object m_killLock;
        File m_tempDir = null;

        //The process:
        Process m_process;

        //Process output streams:
        ProcessOutputHandler m_errorHandler;
        ProcessOutputHandler m_outputHandler;

        private boolean DEBUG = TRAgent.DEBUG;

        int m_pid;
        int m_exitVal;

        final TRProcessContext ctx;
        ProcessListener listener;
        RemoteCommandHandler remoteController;
        String remoteControllerID;

        /**
         * Upon return the ProcessHandler can be started. The initialization
         * file is reloaded.
         * 
         * @param launcherID
         */
        public ProcessHandler(ProcessListener listener, int pid) {
            this.listener = listener;
            ctx = new TRProcessContext(getAgentId(), new Integer(pid));
            m_pid = pid;
            //Create a temporary directory in our current directory:
            //m_tempDirName = "." + File.separator + "temp" + File.separator + m_pid;
            m_tempDir = new File(m_processMonitor.m_tempDir + File.separator + m_pid);
            m_tempDir.mkdirs();

            readPropFile();

            m_killLock = new Object();
        }

        public void setRemoteContext(RemoteCommandHandler remoteController, String remoteControllerID) {
            this.remoteController = remoteController;
            this.remoteControllerID = remoteControllerID;
        }

        /**
         * @return
         */
        public TRProcessContext getProcessContext() {
            return ctx;
        }

        /**
         * Interprets the launch description and generates a command line to be
         * spawned. Creates a classloader to dynamically loadClasses.
         * LAUNCH_SUCCESS or LAUNCH_FAILURE is sent to the entity that requested
         * the launch.
         * 
         */
        public void launch(TRLaunchDescr ld) throws Exception {
            DEBUG = ld.getDebug() || DEBUG;

            String javaExe = "";
            String classPath = "";
            String exposedClassPath = "";
            //String classPathPrefix = "";
            String processName = "";
            String args = "";
            String workingDir = "";
            String[] envParams = null;

            //Get the launch Name
            processName = ld.getRunName();
            args = ld.getArgs();
            workingDir = ld.getWorkingDir();
            envParams = ld.getEnvParams();

            if (ld.getProcessType() == TRLaunchDescr.PROCESS_TYPE_JAVA_EXE || ld.getProcessType() == TRLaunchDescr.PROCESS_TYPE_BOOTSTRAP_JAVA_EXE) {
                //Look for matching tags:
                //JVM:
                String tag;
                if (ld.getJVMTag() != null) {
                    tag = ld.getJVMTag() + "_JAVAEXE";
                    if ((javaExe = getTag(tag)) == null) {
                        throw new Exception("JVM tag not found in ini file: " + tag);
                    }
                    //Check for a lib tag
                    tag = ld.getJVMTag() + "_JAVALIB";
                    String tagValue = null;
                    if ((tagValue = getTag(tag)) != null) {
                        classPath += tagValue + File.pathSeparator;
                    } else {
                        throw new Exception("JVM tag not found in ini file: " + tag);
                    }
                } else if (ld.getJVMPath() != null) {
                    javaExe += ld.getJVMPath();
                } else //Trying to run java app with no jvm -- ERROR:
                {
                    throw new Exception("No JVM specified. Java app not run.");
                }

                String jvmArgs = ld.getJVMArgs();
                if (m_port >= 0) {
                    String portArgs = "-D" + PORT_PROPERTY + "=" + m_port + " -D" + PID_PROPERTY + "=" + m_pid;
                    if (jvmArgs == null)
                        jvmArgs = portArgs;
                    else
                        jvmArgs += " " + portArgs;
                    if (DEBUG)
                        System.out.println("Passing JVM args " + jvmArgs + " to process with PID = " + m_pid);
                }

                javaExe += " " + generateJVMArgs(ld.getJVMArgTags(), jvmArgs);
                classPath += generateClassPath(ld.getClassPathTags(), ld.getClassPaths());
                exposedClassPath += generateClassPath(ld.getExposedCPTags(), ld.getExposedCP());
                classPath = " -cp \"" + exposedClassPath + classPath + "\" ";
            }

            boolean isObjectStream = (ld.getOutputType() == TRLaunchDescr.OBJECT_OUTPUT);

            if (ld.getProcessType() == TRLaunchDescr.PROCESS_TYPE_BOOTSTRAP_JAVA_EXE) {

                if (isObjectStream)
                    throw new Exception("PROCESS_TYPE_BOOTSTRAP_JAVA_EXE is incompatible with OBJECT_OUTPUT");

                int indx = processName.indexOf(" ");
                if (indx == -1) {
                    throw new Exception("runName for PROCESS_TYPE_BOOTSTRAP_JAVA_EXE is incorrect: " + processName);
                }
                String pName1 = processName.substring(0, indx);

                m_currentProcessString = javaExe + classPath + pName1 + " " + args;
                System.out.println("Bootstrap launch string: " + m_currentProcessString);
                TRLaunchHelper lh = new TRLaunchHelper(m_currentProcessString, ld);
                m_currentProcessString = lh.getTRLaunchString();
                System.out.println("New launch string: " + m_currentProcessString);
            } else
                m_currentProcessString = javaExe + classPath + processName + " " + args;

            //Generate the launch string
            System.out.println("Launching as: " + m_currentProcessString + " [pid = " + m_pid + "]" + ((workingDir != null && workingDir.length() > 0) ? " [workDir = " + workingDir + "]" : ""));
            listener.handleProcessInfo(ctx, "Launching as: " + m_currentProcessString + " [pid = " + m_pid + "]"
                    + ((workingDir != null && workingDir.length() > 0) ? " [workDir = " + workingDir + "]" : ""));

            //Launch:
            if (workingDir != null && workingDir.length() > 0)
                m_process = Runtime.getRuntime().exec(m_currentProcessString, envParams, new File(workingDir));
            else
                m_process = Runtime.getRuntime().exec(m_currentProcessString);
            if (m_process == null) {
                throw new Exception("Process launched failed (returned null).");
            }

            // create error handler
            m_errorHandler = new ProcessOutputHandler(m_process.getErrorStream(), "TR Process Error Handler for: " + m_pid, true);
            if(!isObjectStream)
            {
                m_outputHandler = new ProcessOutputHandler(m_process.getInputStream(), "TR Process Output Handler for: " + m_pid, false); // just handle output as data
            }
            else if(m_port < 0) // no port, or data output, use System.out as process input
            {
                //Create  a communicator for the process:
                m_processCom = new TRProcessCommunicator(m_process.getInputStream(), m_process.getOutputStream(), m_agentID, isObjectStream); //Create object stream?
            } else {
                m_outputHandler = new ProcessOutputHandler(m_process.getInputStream(), "TR Process Output Handler for: " + m_pid, false); // just handle output as data
                // get process input stream from port socket
                int retries = 0;
                long sleep = 500;
                int maxRetries = (int) (PORT_CONNECT_TIMEOUT / sleep);

                InputStream pis = null;
                System.out.println("Getting port input stream for PID = " + m_pid);
                while (retries < maxRetries) {
                    pis = m_processAcceptor.getProcessInputStream(m_pid);
                    if (pis != null)
                        break;
                    else {
                        retries++;
                        if (DEBUG)
                            System.out.println("Retry number = " + retries);
                        Thread.sleep(sleep);
                    }
                }
                if (pis == null)
                    throw new Exception("Launched process PID = " + m_pid + " did not create socket after " + (retries * sleep) + " millisecs");
                else
                    System.out.println("Got input stream for process PID = " + m_pid + " after " + (retries * sleep) + " millisecs");

                m_processCom = new TRProcessCommunicator(pis, // use input stream to port
                        m_process.getOutputStream(), m_agentID, isObjectStream); //Create object stream?
                if (DEBUG)
                    System.out.println("Successfully created process communicator for PID = " + m_pid);
            }

            //If the stream is OBJECT_OUTPUT then it will send a STREAM_INIT_STR when it starts.
            //Attempt to read the STREAM_INIT_STR from the launched process.
            //Also, set the classloader.
            if (isObjectStream) {
                
                Object startStr = m_processCom.readObject();
                if (startStr instanceof String && ((String) startStr).equals(TRProcessCommunicator.STREAM_INIT_STR)) {
                    return;
                } else {
                    throw new Exception("Stream init string not detected in process output stream.");
                }
            } else //DATA_OUTPUT
            {
                return;
            }
        }

        public void start() {
            if (m_processCom != null) {
                m_thread = new Thread(this, "TR Process Handler for pid " + m_pid);
                m_thread.start();
            }
        }

        /**
         * Generate a String of jvm args from tags and specified jvm args.
         * 
         */
        public String generateJVMArgs(Vector tags, String args) throws Exception {
            String ret = "";

            for (int i = 0; tags != null && i < tags.size(); i++) {
                String tag = ((String) tags.elementAt(i)) + "_JVM_ARGS";
                ret += getTag(tag) + " ";
            }

            if (args != null) {
                ret += " " + args;
            }

            return ret;
        }

        /**
         * Generates a classpath string from tags and paths contained in the
         * vectors.
         * 
         */
        private String generateClassPath(Vector tags, Vector paths) throws Exception {
            String ret = "";
            String tag;

            //Copy the straight classpath to a temporary directory for this process
            //These are copied because they may not be local, and we don't want to be
            //loading classes over the network at runtime.
            for (int i = 0; paths != null && i < paths.size(); i++) {
                ret += setUpLocalClassPath((String) paths.elementAt(i), false);
            }

            for (int i = 0; tags != null && i < tags.size(); i++) {
                tag = ((String) tags.elementAt(i)) + "_CLASSPATH";

                ret += getTag(tag) + File.pathSeparator;
            }
            return ret;
            //return "\"" + ret + "\"";
        }

        /**
         * Searches the TRA.ini file for a tag and returns its value. Each tag
         * in the ini file can have at most one wildcard (*) character in it. If
         * a tag passe in matches a wildcard tag in the ini file the tag value
         * is returned. with all instances of * replaced with the substring of
         * the tag passed in.
         * 
         * Ex. if "abcdef" were passed in and the ini file had the following tag
         * entry: ab*f=d:\dir*\filename
         * 
         * then this method would return d:\dircde\filename
         * 
         * returns null if no matches found.
         */
        private String getTag(String tag) throws Exception {
            if (tag != null) {
                tag = tag.trim();
            } else {
                throw new Exception("Tag (" + tag + ") not found in: " + m_propFileName);
            }

            if (properties.getProperty(tag) != null) {
                return properties.getProperty(tag).trim();
            } else //Check for a wildcard match:
            {
                Enumeration tags = properties.keys();
                while (tags.hasMoreElements()) {
                    String tagName = ((String) tags.nextElement()).trim();
                    int wildIndex = tagName.indexOf('*');
                    //boolean match = false;

                    if (DEBUG)
                        System.out.println("Matching " + tag + " against " + tagName);
                    //See if the tag from the file matchs the desired tag
                    if (wildCardMatch(tagName, tag)) {
                        if (DEBUG)
                            System.out.println("Matched.");
                        String replaceString = tag.substring(wildIndex, tag.length() - tagName.substring(wildIndex + 1).length());
                        String value = properties.getProperty(tagName).trim();
                        //Replace every occurence of the wildcard with replaceString
                        String ret = "";
                        int lastIndex = -1;
                        while (lastIndex < value.length()) {
                            int index = value.indexOf('*', lastIndex + 1);
                            if (index == -1)
                                break;
                            ret += value.substring(lastIndex + 1, index) + replaceString;
                            lastIndex = index;
                        }
                        ret += value.substring(lastIndex + 1);
                        return ret;
                    }
                }
            }
            throw new Exception("Tag (" + tag + ") not found in: " + m_propFileName);
        }

        /**
         * Does a case insenstive match from the file tag in the form Xx*yy with
         * a tag of the form xxzzYy counting as matching.
         * 
         */
        private boolean wildCardMatch(String fileTag, String matchTag) {
            if (DEBUG)
                System.out.println("In wild card match. File: " + fileTag + " match " + matchTag);
            int wildIndex = fileTag.indexOf('*');
            if (wildIndex < 0) {
                return fileTag.equalsIgnoreCase(matchTag);
            }

            String preFile = fileTag.substring(0, wildIndex);
            String postFile = fileTag.substring(wildIndex + 1);
            if (DEBUG)
                System.out.println("preFile: " + preFile + ", " + "postFile: " + postFile + ", ");
            //false if the tag to match won't have anything to replace the "*" with:
            if ((postFile.length() + preFile.length()) >= matchTag.length())
                return false;

            String preMatch = matchTag.substring(0, (wildIndex));
            String postMatch = matchTag.substring(matchTag.length() - postFile.length());
            if (DEBUG)
                System.out.println("preMatch: " + preMatch + ", " + "postMatch: " + postMatch);

            return (preMatch.equalsIgnoreCase(preFile) && postMatch.equalsIgnoreCase(postFile));
        }

        /**
         * Copies the classpaths pointed to in the launch description to the
         * specified directory and returns the classpath to the local classes
         * 
         */

        private String setUpLocalClassPath(String path, boolean copy) throws Exception {
            String classPath = "";

            StringTokenizer stok = new StringTokenizer(path, File.pathSeparator);

            if (!copy)
                return path;

            //TODO reenabled copying.
            while (stok.hasMoreElements()) {
                File file = new File(stok.nextToken());

                if (DEBUG)
                    System.out.println("Copying " + file.getAbsolutePath() + " locally");

                if (!file.exists()) {
                    throw new FileNotFoundException(file.getAbsolutePath());
                }
                //else if (file.)
                else if (file.isFile()) //If a file copy to dir/lib
                {
                    copyFile(file, new File(m_tempDir.getAbsolutePath() + File.separator + "lib" + File.separator + file.getName()));
                    classPath += m_tempDir.getAbsolutePath() + File.separator + "lib" + File.separator + file.getName() + File.pathSeparator;
                }
                //If a directory recursively copy to dir/classes
                else if (file.isDirectory()) {
                    String target = m_tempDir.getAbsolutePath() + File.separator + "classes" + File.separator + file.getName();
                    recursiveFileCopy(file.getAbsolutePath(), target);
                    classPath += target + File.pathSeparator;
                }
            }
            if (DEBUG)
                System.out.println("Local cp: " + classPath);
            return classPath;
        }

        /**
         * Reads from the launched process' output stream and relays the output
         * to the entity that launched the process. When the EOF is reached on
         * the output stream PROCESS_DONE is sent to the the launcher followed
         * by the process' exit value.
         */
        public void run() {
            System.out.println("Process running.");
            while (true) {

                if (DEBUG)
                    listener.handleProcessInfo(ctx, "Starting to listen to process output.");
                Object obj;
                try {
                    while (true) {
                        if (m_processCom != null) {
                            if ((obj = m_processCom.readObject()) != null) {
                                if (obj == null || m_thread.isInterrupted()) {
                                    return;
                                }
                                listener.handleMessage(ctx, obj);
                            }
                        } else {
                            return;
                        }
                    }
                } catch (java.io.EOFException eofe) {
                    //If the process ended before shutdown was called for this process then
                    //send a process done to the launcher otherwise we will have interrupted the
                    //thread during a shutdown:
                    if (DEBUG)
                        System.out.println("EOF read from process [ pid = " + m_pid + "]");

                    if (!m_thread.isInterrupted()) {

                        //We have shutdown unexpectedly wait for the process to finish before
                        //sending process done
                        if (DEBUG) {
                            System.out.println("Unexpected shutdown; waiting to join error handler [ pid = " + m_pid + "]");
                            try {
                                m_errorHandler.m_thread.join(15000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                Thread.currentThread().interrupt();
                            }
                        }

                        listener.processDone(ctx, m_process.exitValue());
                        System.out.println("PROCESS FINISHED.");

                    }
                    return;
                } catch (InterruptedIOException iioe) {
                    return;
                } catch (InterruptedException ie) {
                    return;
                } catch (Exception e) {
                    if (DEBUG)
                        System.out.println("Exception from process [ pid = " + m_pid + "] " + e.toString());
                    if (!m_thread.isInterrupted()) {
                        System.out.println("ERROR: running process: " + e.getMessage());
                        e.printStackTrace();
                        listener.handleError(ctx, "Error reading from process' output stream.", e);
                    }
                    return;
                }
            }//while true
        }//public void run()

        public synchronized boolean writeObject(Object obj) throws Exception {
            boolean didWrite = false;
            synchronized (m_killLock) {
                if (m_processCom != null && m_thread.isAlive()) {
                    m_processCom.writeObject(obj);
                    didWrite = true;
                }
            }
            return didWrite;
        }

        /**
         * private boolean isRunning()
         * 
         * Returns whether this object is current running a process.
         * 
         * 
         * boolean isRunning() { boolean isRunning = false; synchronized
         * (m_killLock) { if (m_thread != null && m_thread.isAlive() &&
         * m_processCom != null) { isRunning = true; } } return isRunning; }
         */

        /**
         * Terminate the currently running process and release the resources
         * allocated to it.
         * 
         * @throws Exception
         * 
         */
        public int kill(boolean internal) throws Exception {
            boolean killSuccessFlag = true;
            synchronized (m_killLock) {
                m_currentProcessString = null;

                Exception exception = null;

                //Destroy the process:
                //do not stop the process handler thread yet, as it will try to send a message
                // to the launcher when the process finishes
                if (m_process != null) {
                    try {
                        System.out.print("Killing process " + m_process + " [pid = " + m_pid + "]");
                        try {
                            m_process.destroy();
                            m_exitVal = m_process.exitValue();
                        } catch (java.lang.IllegalThreadStateException itse) {
                            //Thrown if the process hadn't exit yet:
                            m_process.waitFor();
                            m_exitVal = m_process.exitValue();
                        }

                        listener.processDone(ctx, m_exitVal);

                        System.out.println("...DONE.");
                        m_process = null;
                    } catch (Exception e) {
                        System.out.println("ERROR: destroying process.");
                        e.printStackTrace();
                        exception = e;
                        killSuccessFlag = false;
                    }
                }

                //Stop the errorListener:
                if (m_errorHandler != null) {
                    try {
                        if (DEBUG)
                            System.out.print("Closing error handler for" + m_process + " [pid = " + m_pid + "]");
                        m_errorHandler.close();
                        if (DEBUG)
                            System.out.println("...DONE.");
                        m_errorHandler = null;
                    } catch (Exception e) {
                        System.out.println("ERROR: closing error handler");
                        e.printStackTrace();
                        if (killSuccessFlag && !internal) {
                            exception = e;
                        }
                        killSuccessFlag = false;
                    }
                }

                //Stop the outputListener, if any:
                if (m_outputHandler != null) {
                    try {
                        if (DEBUG)
                            System.out.print("Closing output handler for" + m_process + " [pid = " + m_pid + "]");
                        m_outputHandler.close();
                        if (DEBUG)
                            System.out.println("...DONE.");
                        m_outputHandler = null;
                    } catch (Exception e) {
                        System.out.println("ERROR: closing output handler");
                        e.printStackTrace();
                        if (killSuccessFlag && !internal) {
                            exception = e;
                        }
                        killSuccessFlag = false;
                    }
                }

                //Stop the thread and wait for it to die
                if (m_thread != null && m_thread.isAlive()) {
                    if (DEBUG)
                        System.out.print("Stopping process handler for" + m_process + " [pid = " + m_pid + "]");

                    try {
                        m_thread.join(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    m_thread.interrupt();
                    try {
                        m_thread.join();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    ;
                    if (DEBUG)
                        System.out.println("...DONE.");
                }

                //Close the process communicator:
                if (m_processCom != null) {
                    try {
                        if (DEBUG)
                            System.out.print("Closing process communicator for process with pid = " + m_pid);
                        m_processCom.close();
                        if (DEBUG)
                            System.out.println("...DONE.");
                    } catch (java.io.IOException ie) {
                        System.out.println("WARNING: exception closing process communicator: " + ie.getMessage());
                    } catch (Exception e) {
                        System.out.println("ERROR: closing process communicator");
                        e.printStackTrace();
                        if (killSuccessFlag && !internal) {
                            exception = e;
                        }
                        killSuccessFlag = false;
                    }
                }

                // close port sockets and streams and clean up tables
                if (m_port >= 0)
                    m_processAcceptor.closeClient(m_pid);

                m_thread = null;
                if (exception != null) {
                    throw exception;
                }

                return m_exitVal;

            }//synchronized(m_shutDownLock)
        }//public void shutdown()

        // handle output or error data
        private class ProcessOutputHandler implements Runnable {
            BufferedInputStream outReader;
            long delay = 100;
            Thread m_thread;
            boolean isStdError;

            public ProcessOutputHandler(InputStream is, String name, boolean isStdError) {
                outReader = new BufferedInputStream(is);
                m_thread = new Thread(this, name);
                m_thread.start();
            }

            public void run() {
                String line = "";
                boolean newline = false;
                String newLineString = System.getProperty("line.separator", "\n");
                
                while (true) {
                    try {
                        
                        int nextByte;
                        nextByte = outReader.read();
                        if (nextByte == -1) {
                            throw new EOFException();
                        }
                        line += (char) nextByte;

                        //Check to see if we reached the end of a line:
                        if (newLineString.charAt(0) == nextByte) {
                            if (newLineString.length() > 1) {
                                nextByte = outReader.read();
                                line += (char) nextByte;
                            }
                            newline = true;
                        }

                        //If there is no output currently available wait a little longer
                        //in an attempt to avoid sending more messages than we need to.
                        //Just returning on newLines is insufficient because not all applications
                        //will desire to read lines delimited by linefeeds (e.g. an app that
                        //prompts the user.
                        //If it is a newLine don't wait, send it:
                        if (!newline && outReader.available() == 0) {
                            Thread.sleep(delay);
                        }

                        //If this line isn't blank and there is nothing else to read
                        //even after the delay return what we have so far.
                        if (line != null && line != "" && (outReader.available() == 0 || newline)) {
                            sendLine(line);
                            line = "";
                            newline = false;
                        }

                    } catch (EOFException eof) {
                        sendLine(line);
                        return;
                    } catch (java.io.IOException ioe) {
                        sendLine(line);
                        listener.handleError(ctx, "Error reading from process' output or error stream " + line, ioe);
                        ioe.printStackTrace();
                        return;
                    } catch (java.lang.InterruptedException ie) {
                        sendLine(line);
                        return;
                    } catch (Exception e) {
                        sendLine(line);
                        System.out.println("ERROR: reading from process' output or  error stream.");
                        e.printStackTrace();
                    }
                }
            }

            private final void sendLine(String line) {
                if (line == null || line.length() == 0) {
                    return;
                }

                try {
                    if (isStdError) {
                        listener.handleSystemErr(ctx, line);
                    } else {
                        listener.handleSystemOut(ctx, line);
                    }
                    if (DEBUG) {
                        System.out.println(line);
                    }
                } catch (Exception e) {
                    listener.handleError(ctx, "Error reading process output or error stream " + line, e);
                }
            }

            public void close() {
                m_thread.interrupt();
                try {
                    m_thread.join(delay * 2);
                } catch (java.lang.InterruptedException ie) {
                }
                ;

                m_thread = null;

                try {
                    outReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 
         */
        public void ping(long timeout) {
            if (remoteControllerID != null) {
                //Check to see if the controller is still around for the process:
                try {
                    //Don't ping controller if we are still bound to it or if timeout is 0 or less
                    if (exclusiveOwner == null || !exclusiveOwner.equals(remoteControllerID)) {
                        if (timeout <= 0) {
                            throw new Exception("Killing process [pid = " + m_pid + "] without pinging controller.");
                        }

                        if (!remoteController.ping(remoteControllerID, timeout)) {
                            throw new Exception("No response pinging controller for [pid = " + m_pid + "] -- killing.");
                        }
                    }
                } catch (Exception e) {
                    try {
                        TRAgent.this.kill(ctx);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }

        }

    }//private class ProcessHandler

    // Acceptor for optional port for launched processes' object input
    private class ProcessAcceptor implements Runnable {
        Thread m_thread;
        int m_port;
        ServerSocket m_serverSocket;
        Hashtable m_clientSockets; // Integer(pid) -> client socket
        Hashtable m_clientInputStreams; // Integer(pid) -> client InputStream

        public ProcessAcceptor(int port) throws IOException {
            m_port = port;
            m_serverSocket = new ServerSocket(port);
            m_clientSockets = new Hashtable();
            m_clientInputStreams = new Hashtable();
            m_thread = new Thread(this);
            m_thread.start();
        }

        public void run() {
            while (true) // accept loop
            {
                Socket clientSocket = null;
                InputStream clientInput = null;
                try {
                    if (m_thread.isInterrupted())
                        break;

                    clientSocket = m_serverSocket.accept();
                    if (DEBUG)
                        System.out.println("Accepted socket on port " + m_port);
                    clientInput = clientSocket.getInputStream();
                    ObjectInputStream cos = new ObjectInputStream(clientInput); // don't buffer as we don't want to read ahead

                    // get PID
                    Object pidKey = null;
                    pidKey = cos.readObject();
                    if (!((pidKey != null) && (pidKey instanceof String) && ((String) pidKey).equals(PID)))
                        throw new Exception("Illegal PID key");
                    int pid = cos.readInt();
                    if (DEBUG)
                        System.out.println("Successfully read PID = " + pid + " from client socket");
                    m_clientSockets.put(new Integer(pid), clientSocket);
                    m_clientInputStreams.put(new Integer(pid), clientInput);
                } catch (Exception e) {
                    System.out.println("ERROR: Unable to read PID info on port " + m_port + " - " + e);
                    try {
                        if (clientInput != null)
                            clientInput.close();
                        if (clientSocket != null)
                            clientSocket.close();
                    } catch (IOException ioe) {
                    }
                    continue;
                }
            }// end accept loop

            // close server, streams and sockets
            close();
        }

        /**
         * Get input stream for that pid
         */
        public InputStream getProcessInputStream(int pid) {
            return (InputStream) m_clientInputStreams.get(new Integer(pid));
        }

        // close server socket and all client sockets
        public void close() {
            Set clientInputStreams = m_clientInputStreams.entrySet();
            Iterator it = clientInputStreams.iterator();
            while (it.hasNext()) {
                InputStream is = (InputStream) it.next();
                try {
                    is.close();
                } catch (IOException ioe) {
                }
            }
            m_clientInputStreams.clear();

            Set clientSockets = m_clientSockets.entrySet();
            it = clientSockets.iterator();
            while (it.hasNext()) {
                Socket sock = (Socket) it.next();
                try {
                    sock.close();
                } catch (IOException ioe) {
                }
            }
            m_clientSockets.clear();

            try {
                m_serverSocket.close();
            } catch (IOException ioe) {
            }

        }

        // close a specific client socket and input stream
        public void closeClient(int pid) {
            InputStream is = (InputStream) m_clientInputStreams.get(new Integer(pid));
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                }
                m_clientInputStreams.remove(is);
            }

            Socket clientSocket = (Socket) m_clientSockets.get(new Integer(pid));
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException ioe) {
                }
                m_clientSockets.remove(clientSocket);
            }
        }
    }

    private class ProcessMonitor implements Runnable {
        Thread m_thread;
        private String m_tempDir;
        private boolean cleanupRequested = false;

        public ProcessMonitor() {
            m_tempDir = m_dataDir + File.separator + getAgentId() + File.separator + "temp";
            m_thread = new Thread(this);
            m_thread.start();

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
            //If we aren't running anything cleanup: temp files
            if (m_procHandlers == null || m_procHandlers.size() == 0) {
                File tempDir = new File(m_tempDir);
                String[] subDirs = tempDir != null ? tempDir.list() : null;

                System.out.println("*************Cleaning up temporary files*************");
                for (int i = 0; subDirs != null && i < subDirs.length; i++) {
                    try {
                        recursiveDelete(tempDir + File.separator + subDirs[i]);
                    } catch (Exception e) {
                        System.out.println("ERROR cleaning up temporary files:");
                        e.printStackTrace();
                    }
                }
            }
        }

        public void checkForRogueProcesses(long timeout) {
            if (m_procHandlers != null && m_procHandlers.size() > 0) {
                Enumeration procHandlerList = m_procHandlers.elements();
                while (procHandlerList.hasMoreElements()) {
                    ProcessHandler procHandler = (ProcessHandler) procHandlerList.nextElement();
                    procHandler.ping(timeout);
                }
            }
        }

        /**
         * Requests cleanup of temporary files
         */
        public synchronized void requestCleanup() {
            cleanupRequested = true;
            notify();
        }

    }//private class ProcessMonitor

    private class RemoteProcessListener implements ProcessListener {

        String launcherID;
        RemoteCommandHandler commandAcceptor;

        RemoteProcessListener(String launcherID, RemoteCommandHandler acceptor) {
            this.launcherID = launcherID;
            this.commandAcceptor = acceptor;
        }

        /* (non-Javadoc)
         * @see org.fusesource.testrunner.ProcessListener#handleError(org.fusesource.testrunner.TRProcessContext, java.lang.String, java.lang.Throwable)
         */
        public void handleError(TRProcessContext ctx, String message, Throwable thrown) {
            
            commandAcceptor.sendMessage(launcherID, new RMIRequest(RMIRequest.CLIENT_PROC_LISTENER, "handleError", new Object[]{ctx, message, thrown}));
        }

        /* (non-Javadoc)
         * @see org.fusesource.testrunner.ProcessListener#handleMessage(org.fusesource.testrunner.TRProcessContext, java.lang.Object)
         */
        public void handleMessage(TRProcessContext ctx, Object msg) {
            commandAcceptor.sendMessage(launcherID, new RMIRequest(RMIRequest.CLIENT_PROC_LISTENER, "handleMessage", new Object[]{ctx, msg}));
        }

        /* (non-Javadoc)
         * @see org.fusesource.testrunner.ProcessListener#handleProcessInfo(org.fusesource.testrunner.TRProcessContext, java.lang.String)
         */
        public void handleProcessInfo(TRProcessContext ctx, String info) {
            commandAcceptor.sendMessage(launcherID, new RMIRequest(RMIRequest.CLIENT_PROC_LISTENER, "handleProcessInfo", new Object[]{ctx, info}));
        }

        /* (non-Javadoc)
         * @see org.fusesource.testrunner.ProcessListener#handleSystemErr(org.fusesource.testrunner.TRProcessContext, java.lang.String)
         */
        public void handleSystemErr(TRProcessContext ctx, String err) {
            commandAcceptor.sendMessage(launcherID, new RMIRequest(RMIRequest.CLIENT_PROC_LISTENER, "handleSystemErr", new Object[]{ctx, err}));
            
        }

        /* (non-Javadoc)
         * @see org.fusesource.testrunner.ProcessListener#handleSystemOut(org.fusesource.testrunner.TRProcessContext, java.lang.String)
         */
        public void handleSystemOut(TRProcessContext ctx, String output) {
            commandAcceptor.sendMessage(launcherID, new RMIRequest(RMIRequest.CLIENT_PROC_LISTENER, "handleSystemOut", new Object[]{ctx, output}));
            
        }

        /* (non-Javadoc)
         * @see org.fusesource.testrunner.ProcessListener#processDone(org.fusesource.testrunner.TRProcessContext, int)
         */
        public void processDone(TRProcessContext ctx, int exitCode) {
            commandAcceptor.sendMessage(launcherID, new RMIRequest(RMIRequest.CLIENT_PROC_LISTENER, "processDone", new Object[]{ctx, new Integer(exitCode)}));
        }
    }

    private class RemoteCommandHandler implements TRComHandler, Runnable {
        TRCommunicator com;
        Thread thread;

        RemoteCommandHandler(String controlUrl) throws Exception {
            com = new TRJMSCommunicator(controlUrl, //TestRunner Server
                    m_agentID //clientID (null = not specified)
            ); //specifies that we will process asyncronous messages
            com.setTRComHandler(this);
            com.connect();
        }

        public boolean ping(String controllerID, long timeout) throws Exception {
            return com.ping(controllerID, timeout);
        }

        /**
         * @param launcherID
         * @param msg
         * @param props
         */
        public void sendMessage(String launcherID, TRMetaMessage msg) {
            try {
                com.sendMessage(msg, launcherID);
            } catch (Exception e) {
                System.err.println("Error sending message to controller: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        /**
         * @param launcherID
         * @param msg
         * @param props
         */
        public void sendMessage(String launcherID, Object msg, Hashtable props) {
            try {
                com.sendMessage(new TRMetaMessage(msg, props), launcherID);
            } catch (Exception e) {
                System.err.println("Error sending message to controller: " + e.getMessage());
                e.printStackTrace();
            }
        }

        public synchronized void start() {
            if (thread == null) {
                thread = new Thread(this, "TRAgent-" + getAgentId());
            }
            thread.setDaemon(false);
            thread.start();
        }

        public synchronized void stop() {
            //Close the control comm:
            try {
                com.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (thread != null) {
                //Stop our processing thread:
                thread.interrupt();
                try {
                    if (thread.isAlive()) {
                        wait();
                    }
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                //Start handling async Messages:
                while (started) {
                    try {
                        handleMessage(com.getMessage(1000));
                    } catch (InterruptedException e) {
                        System.out.println(getAgentId() + ": Stopping...");
                        break;
                    } catch (Exception e) {
                        if (e.getCause() instanceof InterruptedException) {
                            System.out.println(getAgentId() + ": Stopping...");
                            break;
                        }
                        System.out.println("ERROR: reading message.");
                        e.printStackTrace();
                    }
                }
            } finally {
                synchronized (this) {
                    notifyAll();
                }
            }
        }

        /**
         * Called when an asynchronous message is sent to the JMSCommunicator.
         * OR when we synchronously receive a message:
         * 
         * @param obj
         *            The message
         * @param source
         *            The ID of the entity that sent the message.
         * 
         */
        public synchronized void handleMessage(TRMetaMessage msg) {
            if (DEBUG)
                System.out.println(getAgentId() + ": Received async message");

            if (msg == null) {
                return;
            }

            if (DEBUG)
                System.out.println("Received Message");

            if (msg.isInternal()) {
                
                if(msg instanceof RMIRequest)
                {
                    Integer requestTracking = msg.getIntProperty(TR_REQ_TRACKING);
                    
                    //TODO refactor all of this to be more generic:
                    Hashtable responseProps = new Hashtable();
                    responseProps.put(TR_REQ_TRACKING, requestTracking);
                    
                    RMIRequest rmiMsg = (RMIRequest)msg;
                    if(rmiMsg.getTarget() == RMIRequest.AGENT)
                    {
                    
                        String method = rmiMsg.getMethod();
                        Object [] args = rmiMsg.getArgs();
    
                        responseProps.put(PROP_COMMAND_RESPONSE, method);
    
                        if (method.equals(COMMAND_LAUNCH)) {
                            System.out.println("Got a Launch Descr from " + msg.getSource() + " on " + new Date());
    
                            TRLaunchDescr trld = (TRLaunchDescr) args[0];
                            try {
                                RemoteProcessListener rpl = new RemoteProcessListener(msg.getSource(), this);
                                TRProcessContext ctx = launch(trld, rpl);
                                //Set the pid in the remote process listener:
                                ProcessHandler procHandler = (ProcessHandler) m_procHandlers.get(ctx.getPid());
                                procHandler.setRemoteContext(this, msg.getSource());
                                responseProps.put(PID, ctx.getPid());
                                sendMessage(msg.getSource(), ctx, responseProps);
                                //Start the handler after sending the response:
                                procHandler.start();
    
                            } catch (Exception e) {
                                sendMessage(msg.getSource(), new TRErrorMsg("Launch Failed", e), responseProps);
                            }
                            return;
                        }
    
                        //If the controller is requesting to kill the process:
                        if (method.equals(COMMAND_KILL)) {
    
                            TRProcessContext ctx = (TRProcessContext) args[0];
                            System.out.println("Got a Launch Kill from " + msg.getSource() + " for [" + ctx + "] on " + new Date());
    
                            responseProps.put(PID, ctx.getPid());
                            try {
                                int exitValue = kill(ctx);
                                sendMessage(msg.getSource(), new Integer(exitValue), responseProps);
                            } catch (Exception e) {
                                sendMessage(msg.getSource(), new TRErrorMsg("Kill Failed", e), responseProps);
                            }
    
                            return;
                        }
    
                        if (method.equals(COMMAND_BIND)) {
                            try {
                                bind(msg.getSource());
                                sendMessage(msg.getSource(), new Boolean(true), responseProps);
                            } catch (Exception e) {
                                sendMessage(msg.getSource(), new TRErrorMsg("Bind Failed", e), responseProps);
                            }
                            return;
                        }
    
                        if (method.equals(COMMAND_RELEASE)) {
                            try {
                                release(msg.getSource());
                                sendMessage(msg.getSource(), new Boolean(true), responseProps);
                            } catch (Exception e) {
                                sendMessage(msg.getSource(), new TRErrorMsg("Release Failed", e), responseProps);
                            }
                            return;
                        }
    
                        sendMessage(msg.getSource(), new TRErrorMsg("Unexpected Command: " + method, new Exception("Unexpected Command" + method)), responseProps);
    
                    }
                    else
                    {
                        sendMessage(msg.getSource(), new TRErrorMsg("Unexpected Request target: " + rmiMsg.getTarget(), new Exception("Unexpected Request target: " + rmiMsg.getTarget())), responseProps);
                    }
                }

                Object content = null;
                try {
                    content = msg.getContent();
                } catch (Exception e1) {
                    sendMessage(msg.getSource(), new TRErrorMsg("Corrupt Command", e1), null);
                }
                //Handle a broad cast message
                //Find the processes that belong to this agent and dispatch
                //the message to each process.
                if (content instanceof TRProcessBroadcastMetaMsg) {
                    TRProcessBroadcastMetaMsg metaMsg = (TRProcessBroadcastMetaMsg) content;
                    TRMetaMessage[] procMessages = metaMsg.getSubMessages(getAgentId());
                    for (int i = 0; i < procMessages.length; i++) {
                        handleMessage(procMessages[i]);
                    }

                    return;
                }

                sendMessage(msg.getSource(), new TRErrorMsg("Unexpected Command", new Exception("Unexpected Command" + msg)), null);
                return;
            }

            //If it isn't an internal request, must be for a process:
            try {
                if (msg.getProperties() != null && msg.getProperties().containsKey(PID)) {
                    ProcessHandler procHandler = (ProcessHandler) m_procHandlers.get(msg.getProperties().get(PID));
                    if (procHandler == null) {
                        System.out.println("Msg received with an invalid pid.");
                        throw new Exception("Unknown process id");
                    }
                    procHandler.writeObject(msg);
                } else {
                    throw new Exception("No process id specified.");
                }
            } catch (Exception e) {
                Hashtable responseProps = new Hashtable();
                responseProps.put(PID, msg.getIntProperty(PID));
                sendMessage(msg.getSource(), new TRErrorMsg("ERROR: writing to process", e), null);
            }
        }

        public String toString() {
            return getAgentId() + ":CommandHandler:" + com.toString();
        }
    }

    public String toString() {
        return "TRAgent-" + getAgentId();
    }

    /*
     * public static void main()
     * 
     * Defines the entry point into this app.
     */
    public static void main(String[] argv) {
        System.out.println("\n\n" + org.fusesource.testrunner.Version.getVersionString() + "\n");

        String jv = System.getProperty("java.version").substring(0, 3);
        if (jv.compareTo("1.4") < 0) {
            System.err.println("The TestRunner agent requires jdk 1.4 or higher to run, the current java version is " + System.getProperty("java.version"));
            System.exit(-1);
            return;
        }

        if (argv.length != 1) {
            printUsage();
            System.exit(-1);
        }
        TRAgent agent = new TRAgent();
        agent.setPropFileName(argv[0]);
        try {
            agent.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}//public class TRAgent