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

//import org.nsclient4j.NSClient4j;

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

public class TRAgent implements ITRBindListener, ITRAsyncMessageHandler {
    private static final boolean devDebug = false;
    private static boolean classDebug = Boolean.getBoolean("org.fuse.testrunner.TRAgent.debug");
    private static boolean packageDebug = Boolean.getBoolean("org.fuse.testrunner.debug");
    private static boolean globalDebug = Boolean.getBoolean("debug");
    private static boolean cmdDebug = devDebug || classDebug || packageDebug || globalDebug;
    private static boolean DEBUG = cmdDebug;

    private static final long CLEANUP_TIMEOUT = 60000;

    /**
     * Sent in a TRMsg back to the entity that sent the LaunchDescr that started
     * the process to indicate that the process was launched successfully.
     */
    public static final String LAUNCH_SUCCESS = "Launch Successful";

    /**
     * Sent back in a TRErrorMsg if the process was not successfully launched
     * 
     * @see LAUNCH_SUCCESS
     */
    public static final String LAUNCH_FAILURE = "Launch Failure";

    /**
     * Sent back to the entity that sent the LaunchDescr that started the
     * process to indicate that the process has finished executing. This will
     * always be followed by another message containing the process' exit value.
     */
    public static final String PROCESS_DONE = "Process Finished";

    /**
     * Sent in a TRMsg back to the entity that sent a KillDescr to indicate that
     * the process has been Successfully killed.
     * 
     */
    public static final String KILL_SUCCESS = "Kill Success";

    /**
     * Sent in a TRErrorMsg to the entity that sent a KillDescr if the agent
     * fails to kill the specified process.
     */
    public static final String KILL_FAILURE = "Kill Failure";

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

    // Property to hold PID to pass to launched process
    public static final String PID_PROPERTY = "org.fuse.testrunner.TRAgent.pid";

    //Sent in some TRMsgs so that the sender can correlate a response to a request:
    public static final String TR_REQ_TRACKING = "TR_REQ_TRACKING";

    /**
     * The key to the Integer property set to indicate the exit value of a
     * killed process.
     * 
     * @see KILL_SUCCESS
     */
    public static final String EXIT_VALUE = "Exit value";

    // Property to hold optional port to pass to launched process
    public static final String PORT_PROPERTY = "org.fuse.testrunner.TRAgent.port";

    private static final long PORT_CONNECT_TIMEOUT = 30000; // 30 secs

    //Commmunication Object:
    private TRJMSCommunicator m_controlCom;

    //ProcessHandlers:
    private Hashtable m_procHandlers;

    //ProcessAcceptor: optional acceptor for a port for process object output
    private int m_port;
    private ProcessAcceptor m_processAcceptor;

    //ProcessMonitor: Periodically tries to clean up abandoned processes:
    private ProcessMonitor m_processMonitor;

    //Temp file cleanup thread.
    private TempFileCleanupThread m_tempFileCleanupThread;

    //Used to generate unique process ids
    private int m_pidCount;

    //Properties file info
    private String m_propFileName;

    //Stores properties from the ini file:
    private Properties m_iniFileProps;

    private String m_currentMaster = null;

    private String m_agentID; //The unique identifier for this agent (specified in ini file);

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
        /*
         * System.out.println("\n\nSonicMQ v" +
         * progress.message.zclient.Version.MAJOR_VERSION + "."
         * progress.message.zclient.Version.MINOR_VERSION + " build " +
         * progress.message.zclient.Version.BUILD_NUMBER;
         */
        if (argv.length != 1) {
            printUsage();
            System.exit(-1);
        }
        new TRAgent(argv[0]);

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

    /*
     * public TestRunnerControl
     * 
     * Constructor
     */
    public TRAgent(String iniFileName) {
        String serverURL;
        m_pidCount = 0;
        m_tempFileCleanupThread = new TempFileCleanupThread();
        m_procHandlers = new Hashtable();
        m_processMonitor = new ProcessMonitor();

        //load the INI File:
        //FYI (The file is reloaded by each process handler so that the
        //ini file can be changed without restarting the agent.
        m_propFileName = iniFileName;
        m_iniFileProps = new Properties();

        try {
            m_iniFileProps.load(new FileInputStream(m_propFileName));
        } catch (java.io.IOException ioe) {
            System.out.println("ERROR: reading " + m_propFileName + ".");
        }

        try {
            m_agentID = m_iniFileProps.getProperty("TRA_NAME", java.net.InetAddress.getLocalHost().getHostName()).trim();
        } catch (java.net.UnknownHostException uhe) {
            System.out.println("Error determining hostname. Edit " + m_propFileName + " to set TRA_NAME explicitly.");
            uhe.printStackTrace();
        }

        // if a port is specified, create a server socket and acceptor thread
        m_port = -1;
        String portString = m_iniFileProps.getProperty("TRA_PORT");
        if (portString != null)
            try {
                m_port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
            }
        if (m_port > 0)
            try {
                m_processAcceptor = new ProcessAcceptor(m_port);
                System.out.println(m_agentID + ": Created acceptor for port " + m_port);
            } catch (IOException ioe) {
                System.out.println("ERROR: Creating acceptor for port " + m_port + " - " + ioe);
                ioe.printStackTrace();
                close();
            }

        serverURL = m_iniFileProps.getProperty("TR_SERVER_URL", "");

        try {
            m_controlCom = new TRJMSCommunicator(serverURL, //TestRunner Server
                    m_agentID, //clientID (null = not specified)
                    (ITRBindListener) this, //specifiecs that this Communicator is bindable
                    (ITRAsyncMessageHandler) this); //specifies that we will process asyncronous messages
        } catch (javax.jms.JMSSecurityException jmsse) {
            System.out.println("ERROR: Security error connecting to server: " + serverURL);
            jmsse.printStackTrace();
            close();
        } catch (javax.jms.JMSException jmse) {
            System.out.println("ERROR: connecting to server: " + serverURL);
            jmse.printStackTrace();
            close();
        } catch (Exception e) {
            System.out.println("ERROR: connecting to server: " + serverURL);
            e.printStackTrace();
            close();
        }

        //Start handling async Messages:
        while (true) {
            try {
                handleMessage(m_controlCom.getMessage(60000));
            } catch (Exception e) {
                System.out.println("ERROR: reading message.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Called when this agent's TRJMSCommunicator accepts a bind release
     * request.
     */
    public void bindReleaseNotify(String controllerID) {
        System.out.println("Bind to " + controllerID + " released");
        m_currentMaster = null;
        synchronized (m_tempFileCleanupThread) {
            m_tempFileCleanupThread.notify();
        }
    }

    /**
     * Called when this agent's TRJMSCommunicator accepts a bind request.
     * 
     * @param controllerID
     *            The id of the controller that send the bind request.
     */
    public void bindNotify(String controllerID) {
        //With such a short time out this will kill all processes be run by other controllers.
        //Which is fine because when someone binds the agent they are asking for
        //exclusive use of this agent
        m_currentMaster = controllerID;
        m_processMonitor.checkForRogueProcesses(0);
        System.out.println("Now bound to: " + controllerID);
    }

    /**
     * Called when an asynchronous message is sent to the JMSCommunicator. OR
     * when we synchronously receive a message:
     * 
     * @param obj
     *            The message
     * @param source
     *            The ID of the entity that sent the message.
     * 
     */
    public synchronized void handleMessage(Object msg) {
        if (DEBUG)
            System.out.println(m_agentID + ": Received async message");

        try {
            if (msg == null) {
                return;
            }
            if (DEBUG)
                System.out.println("Received Message");

            if (msg instanceof TRLaunchDescr) {
                System.out.println("Got a Launch Descr from " + m_controlCom.getSource() + " on " + new Date());
                m_pidCount++;
                Integer launchTracking = (Integer) m_controlCom.getProperties().get(TR_REQ_TRACKING);
                ProcessHandler procHandler = new ProcessHandler(m_controlCom.getSource(), m_pidCount, launchTracking);
                try {
                    procHandler.start((TRLaunchDescr) msg);
                    m_procHandlers.put(new Integer(m_pidCount), procHandler);
                } catch (Throwable thrown) {
                    thrown.printStackTrace();
                    Hashtable replyProps = new Hashtable();
                    replyProps.put(PID, new Integer(procHandler.m_pid));
                    if (launchTracking != null) {
                        replyProps.put(TR_REQ_TRACKING, launchTracking);
                    }
                    //Send success message to process launcher:
                    m_controlCom.sendMessage(m_controlCom.getSource(), new TRErrorMsg(LAUNCH_FAILURE, thrown), replyProps);
                    procHandler.kill(true, null);
                    return;
                }
                return;
            }

            //If the controller is requesting to kill the process:
            if (msg instanceof TRLaunchKill) {
                handleKill(m_controlCom.getSource(), (Integer) m_controlCom.getProperties().get(PID), (Integer) m_controlCom.getProperties().get(TR_REQ_TRACKING));
                return;
            }

            //Handle a broad cast message
            //Find the processes that belong to this agent and dispatch
            //the message to each process.
            if (msg instanceof TRComHubBroadcastMetaMsg) {
                TRComHubBroadcastMetaMsg metaMsg = (TRComHubBroadcastMetaMsg) msg;
                TRProcessContext[] recipProcs = metaMsg.getRecips();
                Object subMsg = metaMsg.getMessage();

                for (int i = 0; i < recipProcs.length; i++) {
                    if (recipProcs[i].getAgentID().equalsIgnoreCase(m_agentID)) {
                        if (subMsg instanceof TRLaunchKill) {
                            handleKill(m_controlCom.getSource(), recipProcs[i].getPid(), null);
                            continue;
                        }

                        ProcessHandler procHandler = (ProcessHandler) m_procHandlers.get(recipProcs[i].getPid());
                        if (procHandler == null) {
                            System.out.println("Broadcast msg received with an invalid pid: (" + recipProcs[i] + ")");
                            throw new Exception("Unknown process id");
                        }
                        try {
                            procHandler.writeObject(subMsg);
                        } catch (Throwable thrown) {
                            m_controlCom.sendMessage(m_controlCom.getSource(), new TRErrorMsg("ERROR: writing to process: " + recipProcs[i], thrown));
                        }
                    }
                }
                return;
            }

            //If it isn't a launch or kill try to send the message to a running process:
            try {
                if (m_controlCom.getProperties() != null && m_controlCom.getProperties().containsKey(PID)) {
                    ProcessHandler procHandler = (ProcessHandler) m_procHandlers.get(m_controlCom.getProperties().get(PID));
                    if (procHandler == null) {
                        System.out.println("Msg received with an invalid pid.");
                        throw new Exception("Unknown process id");
                    }
                    procHandler.writeObject(msg);
                } else {
                    throw new Exception("No process id specified.");
                }
            } catch (Exception e) {
                m_controlCom.sendMessage(m_controlCom.getSource(), new TRErrorMsg("ERROR: writing to process", e));
            }
        } catch (Exception e) {
            System.out.println("Error sending control message");
            e.printStackTrace();
        }
    }

    private final void handleKill(String source, Integer pid, Integer reqTracking) {
        System.out.println("Got a Launch Kill from " + source + " for [pid " + pid + "] on " + new Date());

        ProcessHandler procHandler = null;
        if (pid != null) {
            procHandler = (ProcessHandler) m_procHandlers.get(pid);
        }

        if (procHandler == null) {
            try {
                if (reqTracking == null) {
                    m_controlCom.sendMessage(source, new TRMsg(KILL_SUCCESS), EXIT_VALUE, new Integer(0));
                } else {
                    Hashtable props = new Hashtable();
                    props.put(EXIT_VALUE, new Integer(0));
                    props.put(TR_REQ_TRACKING, reqTracking);

                    m_controlCom.sendMessage(source, new TRMsg(KILL_SUCCESS), props);
                }
            } catch (Exception e) {
                System.out.println("Error sending control message");
                e.printStackTrace();
            }
            return;
        } else {
            procHandler.kill(false, reqTracking);
            m_procHandlers.remove(new Integer(procHandler.m_pid));
        }

        //Cleanup:
        synchronized (m_tempFileCleanupThread) {
            m_tempFileCleanupThread.notifyAll();
        }
        System.runFinalization();
        System.gc();
        Runtime.getRuntime().freeMemory();
    }

    private void close() {
        System.exit(-1);
    }

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

        Process m_process;
        String m_currentProcessString;
        TRProcessCommunicator m_processCom;
        Thread m_thread;
        Object m_killLock;
        Object m_sendLock;
        String m_launcherID;
        File m_tempDir = null;
        TRClassLoader m_procClassLoader;
        ProcessOutputHandler m_errorHandler;
        ProcessOutputHandler m_outputHandler;

        private boolean DEBUG = TRAgent.DEBUG;

        int m_pid;
        int m_exitVal;
        Integer m_launchTracking = null;

        /**
         * Upon return the ProcessHandler can be started. The initialization
         * file is reloaded.
         * 
         * @param launcherID
         */
        public ProcessHandler(String launcherID, int pid, Integer launchTracking) {
            m_launcherID = launcherID;
            m_pid = pid;
            m_launchTracking = launchTracking;
            //Create a temporary directory in our current directory:
            //m_tempDirName = "." + File.separator + "temp" + File.separator + m_pid;
            m_tempDir = new File("." + File.separator + "temp" + File.separator + m_pid);
            m_tempDir.mkdirs();

            //Load the properties file:
            try {
                m_iniFileProps.clear();
                m_iniFileProps.load(new FileInputStream(m_propFileName));
            } catch (java.io.IOException ioe) {
                sendMessageToLauncher(new TRErrorMsg("ERROR: reading " + m_propFileName + ".", ioe));
                System.out.println("ERROR: reading " + m_propFileName + ".");
                ioe.printStackTrace();
            }
            m_killLock = new Object();
            m_sendLock = new Object();
            m_thread = new Thread(this, "TR Process Handler for pid " + m_pid);
        }

        /**
         * Interprets the launch description and generates a command line to be
         * spawned. Creates a classloader to dynamically loadClasses.
         * LAUNCH_SUCCESS or LAUNCH_FAILURE is sent to the entity that requested
         * the launch.
         * 
         */
        public void start(TRLaunchDescr ld) throws Throwable {
            DEBUG = ld.getDebug() || DEBUG;
            m_controlCom.setDebug(DEBUG);

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
                if (m_port > 0) {
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
                classPath = " -cp " + exposedClassPath + classPath + " ";
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

            //If the process is launched with object streams, create a class loader to dynamically load classes
            //in the launched classpath.
            //This is necessary in case the controller or launched process try to send
            //objects between each other that we do not know about.
            if (isObjectStream) {
                if (DEBUG)
                    System.out.println("TRAgent: creating TRClassLoader with " + exposedClassPath + " for PID = " + m_pid);
                m_procClassLoader = new TRClassLoader(exposedClassPath);
                m_controlCom.setDebug(DEBUG);
                if (DEBUG)
                    m_procClassLoader.listLoaded(System.out);
                m_controlCom.setClassLoader(m_procClassLoader, m_pid);
            }

            //Generate the launch string
            System.out.println("Launching as: " + m_currentProcessString + " [pid = " + m_pid + "]" + ((workingDir != null && workingDir.length() > 0) ? " [workDir = " + workingDir + "]" : ""));
            m_controlCom.sendMessage(m_launcherID, new TRDisplayMsg("Launching as: " + m_currentProcessString + " [pid = " + m_pid + "]"
                    + ((workingDir != null && workingDir.length() > 0) ? " [workDir = " + workingDir + "]" : "")), PID, new Integer(m_pid));

            //Launch:
            if (workingDir != null && workingDir.length() > 0)
                m_process = Runtime.getRuntime().exec(m_currentProcessString, envParams, new File(workingDir));
            else
                m_process = Runtime.getRuntime().exec(m_currentProcessString);
            if (m_process == null) {
                throw new Exception("Process launched failed (returned null).");
            }

            // create error handler
            m_errorHandler = new ProcessOutputHandler(m_process.getErrorStream(), "TR Process Error Handler for: " + m_pid);

            // create communication channel for the process
            if (m_port < 0 || !isObjectStream) // no port, or data output, use System.out as process input
            {
                //Create  a communicator for the process:
                m_processCom = new TRProcessCommunicator(m_process.getInputStream(), m_process.getOutputStream(), m_agentID, isObjectStream); //Create object stream?
            } else {
                m_outputHandler = new ProcessOutputHandler(m_process.getInputStream(), "TR Process Output Handler for: " + m_pid); // just handle output as data
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
                m_processCom.setClassLoader(m_procClassLoader);
                if (DEBUG)
                    System.out.println("TRAgent: set " + m_procClassLoader + " on process com for pid = " + m_pid);
                m_processCom.setDebug(DEBUG || devDebug);

                Object startStr = m_processCom.readObject();
                if (startStr instanceof String && ((String) startStr).equals(TRProcessCommunicator.STREAM_INIT_STR)) {
                    Hashtable replyProps = new Hashtable();
                    replyProps.put(PID, new Integer(m_pid));
                    if (m_launchTracking != null) {
                        replyProps.put(TR_REQ_TRACKING, m_launchTracking);
                    }
                    //Send success message to process launcher:
                    m_controlCom.sendMessage(m_launcherID, new TRMsg(LAUNCH_SUCCESS), replyProps);
                    //And start the thread to start to listen for input
                    //from the process:
                    m_thread.start();
                    return;
                } else {
                    throw new Exception("Stream init string not detected in process output stream.");
                }
            } else //DATA_OUTPUT
            {
                Hashtable replyProps = new Hashtable();
                replyProps.put(PID, new Integer(m_pid));
                if (m_launchTracking != null) {
                    replyProps.put(TR_REQ_TRACKING, m_launchTracking);
                }
                //Send success message to process launcher:
                m_controlCom.sendMessage(m_launcherID, new TRMsg(LAUNCH_SUCCESS), replyProps);
                //And start the thread to start to listen for input
                //from the process:
                m_thread.start();
                return;
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
                ret += setUpLocalClassPath((String) paths.elementAt(i));
            }

            for (int i = 0; tags != null && i < tags.size(); i++) {
                tag = ((String) tags.elementAt(i)) + "_CLASSPATH";

                ret += getTag(tag) + File.pathSeparator;
            }
            return ret;
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

            if (m_iniFileProps.getProperty(tag) != null) {
                return m_iniFileProps.getProperty(tag).trim();
            } else //Check for a wildcard match:
            {
                Enumeration tags = m_iniFileProps.keys();
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
                        String value = m_iniFileProps.getProperty(tagName).trim();
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

        private String setUpLocalClassPath(String path) throws Exception {
            String classPath = "";

            StringTokenizer stok = new StringTokenizer(path, File.pathSeparator);

            while (stok.hasMoreElements()) {
                File file = new File(stok.nextToken());

                if (DEBUG)
                    System.out.println("Copying " + file.getAbsolutePath() + " locally");

                if (!file.exists()) {
                    throw new FileNotFoundException(file.getAbsolutePath());
                } else if (file.isFile()) //If a file copy to dir/lib
                {
                    copyFile(file, new File(m_tempDir.getAbsolutePath() + File.separator + "lib" + File.separator + file.getName()));
                    classPath += m_tempDir.getAbsolutePath() + File.separator + "lib" + File.separator + file.getName() + File.pathSeparator;
                }
                //If a directory recursively copy to dir/classes
                else if (file.isDirectory()) {
                    recursiveFileCopy(file.getAbsolutePath(), m_tempDir.getAbsolutePath() + File.separator + "classes");
                    classPath += m_tempDir.getAbsolutePath() + File.separator + "classes" + File.pathSeparator;
                }
                //If a directory recursively copy to dir/classes
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
                    sendMessageToLauncher(new TRDisplayMsg("Starting to listen to process output."));
                Object obj;
                try {
                    while (true) {
                        if (m_processCom != null) {
                            if ((obj = m_processCom.readObject()) != null) {
                                if (obj == null || m_thread.isInterrupted()) {
                                    return;
                                }
                                sendMessageToLauncher(obj);
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

                        sendMessageToLauncher(new TRMsg(PROCESS_DONE));
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
                        sendMessageToLauncher(new TRErrorMsg("Error reading from process' output stream.", e));
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
         * Sends a message to the entity that launched this process:
         */
        private void sendMessageToLauncher(Object obj) {
            sendMessageToLauncher(obj, null, null);
        }

        private void sendMessageToLauncher(Object obj, String propName, Object value) {
            if (DEBUG || devDebug)
                System.out.println("TRAgent.sendMesageToLaucher obj: " + obj);

            Hashtable props = new Hashtable();
            if (propName != null) {
                props.put(propName, value);
            }
            sendMessageToLauncher(obj, props);
        }

        private void sendMessageToLauncher(Object obj, Hashtable props) {
            props.put(PID, new Integer(m_pid));

            synchronized (m_sendLock) {
                try {
                    if (!Thread.interrupted()) {
                        m_controlCom.sendMessage(m_launcherID, obj, props);
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: sending to controller");
                    e.printStackTrace();
                }
            }
        }

        public void finalize() throws Throwable {
            if (DEBUG)
                System.out.println("Running Finalization on " + this);
            super.finalize();
            if (DEBUG)
                System.out.println("Finished Running Finalization");
        }

        /**
         * Terminate the currently running process and release the resources
         * allocated to it.
         * 
         */
        public void kill(boolean internal, Integer reqTracking) {
            boolean killSuccessFlag = true;
            synchronized (m_killLock) {
                m_currentProcessString = null;

                Hashtable replyProps = null;

                if (!internal) {
                    replyProps = new Hashtable();
                    if (reqTracking != null) {
                        replyProps.put(TR_REQ_TRACKING, reqTracking);
                    }
                    replyProps.put(EXIT_VALUE, new Integer(-1));
                }

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

                        replyProps.put(EXIT_VALUE, new Integer(m_exitVal));
                        System.out.println("...DONE.");
                        m_process = null;
                    } catch (Exception e) {
                        System.out.println("ERROR: destroying process.");
                        e.printStackTrace();
                        if (killSuccessFlag && !internal) {
                            sendMessageToLauncher(new TRErrorMsg(KILL_FAILURE, e), replyProps);
                        }
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
                            sendMessageToLauncher(new TRErrorMsg(KILL_FAILURE, e), replyProps);
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
                            sendMessageToLauncher(new TRErrorMsg(KILL_FAILURE, e), replyProps);
                        }
                        killSuccessFlag = false;
                    }
                }

                //Stop the thread and wait for it to die
                if (m_thread != null) {
                    if (DEBUG)
                        System.out.print("Stopping process handler for" + m_process + " [pid = " + m_pid + "]");
                    m_thread.interrupt();
                    try {
                        m_thread.join();
                    } catch (InterruptedException ie) {
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
                        //TODO: IO errors other than "The pipe is being closed" may be serious.
                    } catch (Exception e) {
                        System.out.println("ERROR: closing process communicator");
                        e.printStackTrace();
                        if (killSuccessFlag && !internal) {
                            sendMessageToLauncher(new TRErrorMsg(KILL_FAILURE, e), replyProps);
                        }
                        killSuccessFlag = false;
                    }
                }

                //Unload classloader:
                if (m_procClassLoader != null) {
                    try {
                        m_controlCom.removeClassLoader(m_procClassLoader, m_pid);
                        m_procClassLoader.close();
                        m_procClassLoader = null;
                    } catch (Exception e) {
                        System.out.println("ERROR: closing classloader for pid = " + m_pid);
                        e.printStackTrace();
                        if (killSuccessFlag && !internal) {
                            sendMessageToLauncher(new TRErrorMsg(KILL_FAILURE, e), replyProps);
                        }
                        killSuccessFlag = false;
                    }
                }

                // close port sockets and streams and clean up tables
                if (m_port >= 0)
                    m_processAcceptor.closeClient(m_pid);

                m_thread = null;
                if (killSuccessFlag && !internal) {
                    sendMessageToLauncher(new TRMsg(KILL_SUCCESS), replyProps);
                }

            }//synchronized(m_shutDownLock)
        }//public void shutdown()

        // handle output or error data
        private class ProcessOutputHandler implements Runnable {
            BufferedInputStream outReader;
            long delay = 3000;
            Thread m_thread;

            public ProcessOutputHandler(InputStream is, String name) {
                outReader = new BufferedInputStream(is);
                m_thread = new Thread(this, name);
                m_thread.start();
            }

            public void run() {
                String line = "";
                while (true) {
                    try {
                        //Wait for available data:
                        if (outReader.available() == 0) {
                            Thread.sleep(delay);
                        }

                        if (outReader.available() > 0) {
                            int nextByte = outReader.read();

                            if (nextByte == -1) {
                                throw new EOFException();
                            }
                            line += (char) nextByte;
                        }

                        //If this line isn't blank and there is nothing else to read
                        //even after the delay return what we have so far. Also limit the
                        //line length to 16384
                        if (outReader.available() == 0 || line.length() > 16384) {
                            sendLine(line);
                            line = "";
                        }
                    } catch (EOFException eof) {
                        sendLine(line);
                        return;
                    } catch (java.io.IOException ioe) {
                        sendLine(line);
                        sendMessageToLauncher(new TRErrorMsg("Error reading from process' output or error stream " + line, ioe));
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
                    sendMessageToLauncher(new TRDisplayMsg(line));
                    if (DEBUG) {
                        System.out.println(line);
                    }
                } catch (Exception e) {
                    sendMessageToLauncher(new TRErrorMsg("Error reading process output or error stream " + line, e));
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

        // get the port this acceptor is listening on
        /*
         * public int getPort() { return m_port; }
         */

        /*
         * private class ProcessOutputListener implements Runnable { Socket
         * m_socket; ObjectInputStream m_processOutput; public
         * ProcessOutputListener(Socket socket) { m_socket = socket;
         * m_processOutput = new ObjectInputStream(socket.getInputStream()); }
         * public void run() { while (true) { Object obj =
         * m_processOutput.readObject(); // TODO: where does it go?? } } }// end
         * ProcessOuputListener
         */
    }// end ProcessAcceptor

    private class ProcessMonitor implements Runnable {
        Thread m_thread;

        public ProcessMonitor() {
            m_thread = new Thread(this);
            m_thread.start();
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(CLEANUP_TIMEOUT);
                } catch (java.lang.InterruptedException ie) {
                    break;
                }
                if (Thread.interrupted()) {
                    break;
                }

                checkForRogueProcesses(15000);

                System.runFinalization();
                System.gc();
                Runtime.getRuntime().freeMemory();
            }
        }

        public void checkForRogueProcesses(long timeOut) {
            if (m_procHandlers != null && m_procHandlers.size() > 0) {
                Enumeration procHandlerList = m_procHandlers.elements();
                while (procHandlerList.hasMoreElements()) {
                    ProcessHandler procHandler = (ProcessHandler) procHandlerList.nextElement();
                    //Check to see if the controller is still around for the process:
                    try {
                        //Don't ping controller if we are still bound to it or if timeout is 0 or less
                        if (m_currentMaster == null || !m_currentMaster.equals(procHandler.m_launcherID)) {
                            if (timeOut <= 0) {
                                throw new Exception("Killing process [pid = " + procHandler.m_pid + "] without pinging controller.");
                            }

                            if (m_controlCom == null || !m_controlCom.ping(procHandler.m_launcherID, timeOut)) {
                                throw new Exception("No response pinging controller for [pid = " + procHandler.m_pid + "] -- killing.");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        procHandler.kill(true, null);
                        m_procHandlers.remove(new Integer(procHandler.m_pid));
                        synchronized (m_tempFileCleanupThread) {
                            m_tempFileCleanupThread.notifyAll();
                        }
                    }

                }//while(procHandlerList.hasMoreElements())
            }//if(m_procHandlers != null && m_procHandlers.size() > 0)
        }//public void checkForRogueProcesses()
    }//private class ProcessMonitor

    //CWM: I decoupled temporary file deletion from the kill process
    //because if an error was encountered in killing the process
    //and it still had a hold on some files we may not be able to delete
    //them which could lead to a whole directory tree being left around.
    //The TempFileCleanupThread just waits until no processes are running
    //to delete the files.
    private class TempFileCleanupThread implements Runnable {
        Thread m_thread = null;
        private String m_tempDir;

        TempFileCleanupThread() {
            m_tempDir = "." + File.separator + "temp";
            m_thread = new Thread(this);
            m_thread.start();
        }

        public void run() {
            while (true) {
                cleanup();
            }
        }

        public synchronized void cleanup() {
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
            try {
                wait();
            } catch (java.lang.InterruptedException ie) {
            }
        }
    }

    /*
     * private class SysStatsCollector implements Runnable { final Thread
     * m_thread;
     * 
     * SysStatsCollector() { m_thread = new Thread(this, "SysStatsCollector");
     * m_thread.start(); }
     * 
     * public void run() { //NSClient4j statsCollector = new NSClient4j(); } }
     */

}//public class TRAgent