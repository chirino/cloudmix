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

/** TRScriptLauncher
 *
 *  A Utility to allow a user to remotely launch a script on a remote machine. This
 *  class basically fills in a launch description and sends it to an agent.
 *
 *  Author: Colin MacNaughton (cmacnaug@progress.com)
 *	Date: 02/01/01
 */

package org.fusesource.testrunner.rmi;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This is an executable class which, provided to allow users to conveniently
 * launch applications. It creates its own TRJMSCommunicator, binds to the
 * specified TRAgent through the Control Broker. It then generates a
 * TRLaunchDescr to run the specified commandLine with the specified arguments
 * 
 * @author Colin MacNaughton (cmacnaug@progress.com
 * @version 1.0
 * @since 1.0
 * @see TRAgent
 */
public class TRScriptLauncher {
    //private static boolean DEBUG = false;
    private TRLaunchDescr m_launchDescr;
    private TRClient m_client;
    private ITRScriptListener scriptListener;
    private String m_agentName;
    private UserInputHandler m_userInputHandler;
    private static boolean m_startInputHandler = false;

    //Keeps track of the last line sent when this is run using main();
    private String m_lastRead = null;
    TRProcessContext ctx;
    private boolean running;
    private int exitCode = -1;
    
    /**
     * Java programs that utilized TRScriptLauncher can register themselves to
     * be notified when a new line is printed to the scripts output stream.
     */
    public interface ITRScriptListener {

        public void handleScriptErrorOutput(TRScriptLauncher trsl, String output);

        /**
         * A notify for each new line of output generate by the script launched
         * by TRScriptLauncher
         * 
         * @param trsl
         *            The TRScriptLauncher the output originates from.
         * @param output
         *            The line of output from the process.
         */
        public void handleScriptOutput(TRScriptLauncher trsl, String output);
    }

    private final ProcessListener processListener = new ProcessListener() {
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ITRProcessListener#handleError(org.fusesource
         * .testrunner.TRProcessContext, java.lang.String, java.lang.Throwable)
         */
        public void handleError(TRProcessContext ctx, String message, Throwable thrown) {
            System.err.println("Error running script " + message + " -- killing");
            thrown.printStackTrace();
            kill();

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ITRProcessListener#handleInfo(org.fusesource
         * .testrunner.TRProcessContext, java.lang.String)
         */
        public void handleProcessInfo(TRProcessContext ctx, String info) {
            //Just print it:
            System.out.println(info);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ITRProcessListener#handleSystemErr(org.fusesource
         * .testrunner.TRProcessContext, java.lang.String)
         */
        public void handleSystemErr(TRProcessContext ctx, String err) {
            if (scriptListener != null) {
                scriptListener.handleScriptErrorOutput(TRScriptLauncher.this, err);
            } else {
                System.err.print(err);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ITRProcessListener#handleSystemOut(org.fusesource
         * .testrunner.TRProcessContext, java.lang.String)
         */
        public void handleSystemOut(TRProcessContext ctx, String output) {
            //Prevent from echoing what the user just typed in:
            if (m_lastRead == null || !output.startsWith(m_lastRead)) {
                if (scriptListener != null) {
                    scriptListener.handleScriptOutput(TRScriptLauncher.this, output);
                } else {
                    System.out.print(output);
                }
            } else {
                m_lastRead = null;
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ITRProcessListener#processDone(org.fusesource
         * .testrunner.TRProcessContext, int)
         */
        public void processDone(TRProcessContext ctx, int exitCode) {

            synchronized (TRScriptLauncher.this) {
                TRScriptLauncher.this.exitCode = exitCode;
                running = false;
                notifyAll();
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ITRAgentComHandler#handleMessage(java.lang.
         * Object, java.util.Hashtable, org.fusesource.testrunner.TRProcessContext)
         */
        public void handleMessage(TRProcessContext ctx, Object msg) {
            //Don't expect messages for launched scripts, call
            //error handler:
            handleError(ctx, "Unexpected Object Output: " + msg, new Exception("Unexpected Object Output"));
            return;
        }
    };
    
    /**
     * Constructor This contstructor does two things. First, it instantiates and
     * populates a a TRLaunchDescr object. A LaunchDescr object encapsulates the
     * information necessary for a TRAgent to launch a process or a script. This
     * class fills in a launch description to launch a script. Second, this
     * constructor starts a TRJMSCommunicator object. This object is used to
     * communicate with agents listening on a TestRunner Control broker. Once
     * The communicator is started the constructor attempts to bind the agent.
     * Binding the agent captures the agent for exclusive use by this
     * controller. When the JMSCommunicator is closed the
     * 
     * @param serverURL
     *            The control broker used to connect to the agent
     * @param agentName
     *            The agent name.
     * @param ScriptPath
     *            The path (relative to the TRAgent's working directory or
     *            absolute) to the script on the agent machine. This could be a
     *            batch file that runs a java app.
     * @param args
     *            Arguments to the script. (Optional)
     * @exception java.lang.Exception
     */
    public TRScriptLauncher(String serverURL, String agentName, String ScriptPath, String[] args) throws Exception {
        m_agentName = agentName;

        //Create a LaunchDescr (to be filled in):
        m_launchDescr = new TRLaunchDescr();

        //This will be a script:
        m_launchDescr.setProcessType(TRLaunchDescr.PROCESS_TYPE_SCRIPT);

        //Set the scriptName:
        m_launchDescr.setRunName(ScriptPath);

        //Set the args
        String argString = "";
        for (int i = 0; args != null && i < args.length; i++) {
            argString += args[i];
        }
        m_launchDescr.setArgs(argString);

        //Set the expected output type to DATA_OUTPUT (tells the agent
        //we want to communicate with a Data output stream:
        m_launchDescr.setOutputType(TRLaunchDescr.DATA_OUTPUT);

        //Initialize Communication with the agent
        try {
            //String [] agentsToControl = {agentName};
            m_client = new TRClient(new TRJMSCommunicator(serverURL, //The test runner control server that the agent is listening on
                    System.getProperty("user.name") + System.currentTimeMillis())); //The name to identify us as a TestRunnerCommunicator

            //IMPORTANT: attempt to bind the agent to
            //work for us. If someone else is already using the agent
            //the bind will fail:
            System.out.print("Attempting to bind " + agentName + "...");
            m_client.bindAgent(agentName);
            System.out.println("DONE: " + agentName + " bound.");
        } catch (Exception e) {
            close();
            System.out.println("ERROR: connecting to " + serverURL + " script will not run");
            throw e;
        }

        //This can also be called by another Java program that may register a
        //script listener to listen to the script's output. Set to null
        //if no listeners are registered.
        scriptListener = null;
    }

    

    /**
     * Once a TRScriptLauncher is instantiated calling its run() method sends
     * the LaunchDescr to start the script run the specified commandline on the
     * agent machine
     * 
     * @exception java.lang.Exception
     */
    /*
     * public void run()
     * 
     * returns: the script's exit value.
     * 
     * Sends the TRLaunchDescr to the agent. Then listens for output from the
     * script using TRJMSCommmunicator.getMessage(..). All output is assumed to
     * be Strings and is dumped to System.out. When a TRAgent.PROCESS_DONE
     * String comes through We know the process is done and can expect the
     * script's exit value to be returned next.
     */
    public int runScript() throws Exception {
        if (m_client == null)
            return -1;

        running = true;
        ctx = m_client.launchProcess(m_agentName, m_launchDescr, processListener);

        //Start a user input handler if we were launched by main():
        if (m_startInputHandler) {
            m_userInputHandler = new UserInputHandler();
        }

        System.out.println("Launched Successfully: " + ctx);

        synchronized (this) {
            while (running) {
                wait();
            }
            return exitCode;
        }
    }

    /**
     * As the script launcher allocates a fair number of resources it should be
     * closed when it no longer being used.
     */
    public synchronized void close() {
        
        if(running)
        {
            kill();
        }
        
        if (m_client != null) {
            try {
                System.out.print("Releasing " + m_agentName + "...");
                m_client.releaseAll();
                System.out.println("DONE");
            } catch (Exception e) {
                System.out.println("ERROR: releasing " + m_agentName);
            }

            try {
                m_client.close();
            } catch (Exception e) {
                System.out.println("ERROR: closing the communicator.");
            }
            m_client = null;
        }

        if (m_userInputHandler != null) {
            m_userInputHandler.stop();
            m_userInputHandler = null;
        }
    }

    /**
     * If the TRScriptLauncher is created by another Java Program, the this can
     * be called to register a Listener that will receive each line of output
     * from the script being run. If a listener is registered the script output
     * will not be output to System.out, otherwise it will.
     * 
     * @param listener
     *            The TRScriptListener that will handle the process' output.
     */
    /*
     * private void registerScriptListener(TRScriptListener listener)
     * 
     * Sets a script listen that will listen to the output.
     */
    public void registerScriptListener(ITRScriptListener listener) {
        scriptListener = listener;
    }

    public synchronized void writeUTF(String utf) throws Exception {
        if (m_client != null) {
            m_client.sendMessage(ctx, utf);
        }
    }

    private void kill() {
        try {
            exitCode = m_client.killProcess(ctx).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            synchronized (this) {
                running = false;
                notifyAll();
            }
        }
    }

    /*
     * UserInputHandler
     * 
     * Reads user input on System.in.
     */
    private class UserInputHandler implements Runnable {
        Thread m_thread;
        BufferedReader br;

        public UserInputHandler() {
            m_thread = new Thread(this);
            m_thread.start();
        }

        public void run() {
            //Listen for user input:
            String input = "";
            try {
                br = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    input = br.readLine();
                    if (input.equalsIgnoreCase("EXIT")) {
                        kill();
                        return;
                    }
                    m_lastRead = input;
                    if (!m_thread.isInterrupted() && input != null) {
                        writeUTF(input);
                    }
                }
            } catch (InterruptedException ie) {
                return;
            } catch (Exception e) {
                if (m_thread == null || m_thread.isInterrupted()) {
                    return;
                }
                System.out.println("Error reading user input.");
                e.printStackTrace();
            }
        }

        public void stop() {
            try {
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (m_thread != null) {
                m_thread.interrupt();
                m_thread = null;
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("Arguments:");
        System.out.println("   -ServerURL:    TestRunner Control Server");
        System.out.println("   -AgentName:    The name of the test runner agent that will run the script");
        System.out.println("   -ScriptPath:   Full UNC path to the script (path that will be accessible from");
        System.out.println("    the (agent launching the script)");
    }

    /*
     * public static void main(String [] argv)
     * 
     * Defines entry point to this app.
     * 
     * Arguments: -ServerURL: TestRunner Control Server -AgentName: The name of
     * the test runner agent that will run the script -ScriptPath: Full UNC path
     * to the script (path that will be accessible from the (agent launching the
     * script)
     */
    public static void main(String[] argv) {
        if (argv.length < 3) {
            printUsage();
            System.exit(-1);
        } else {
            String[] actualArgs = new String[argv.length - 3];
            for (int i = 3; i < argv.length; i++) {
                actualArgs[i - 3] = argv[i];
            }
            TRScriptLauncher trlu = null;
            int exitCode = -1;
            try {
                trlu = new TRScriptLauncher(argv[0], argv[1], argv[2], actualArgs);
                m_startInputHandler = true;
                exitCode = trlu.runScript();
            } catch (Exception e) {
                System.out.println("ERROR: running script.");
                e.printStackTrace();
            }
            trlu.close();

            System.out.println("Returning with exit value: " + exitCode);
            System.exit(exitCode);
        }

    }
}