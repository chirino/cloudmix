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

package org.fusesource.testrunner;

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
    private TRJMSCommunicator m_agentCom;
    private ITRScriptListener scriptListener;
    private String m_agentName;
    private Integer m_pid;
    private static int retValue = -1;
    private UserInputHandler m_userInputHandler;
    private static boolean m_startInputHandler = false;

    //Keeps track of the last line sent when this is run using main();
    private String m_lastRead = null;

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
            try {
                trlu = new TRScriptLauncher(argv[0], argv[1], argv[2], actualArgs);
                m_startInputHandler = true;
                trlu.run();
            } catch (Exception e) {
                System.out.println("ERROR: running script.");
                e.printStackTrace();
            }
            trlu.close();
        }
        System.out.println("Returning with exit value: " + retValue);
        System.exit(retValue);
    }

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
            m_agentCom = new TRJMSCommunicator(serverURL, //The test runner control server that the agent is listening on
                    System.getProperty("user.name") + System.currentTimeMillis(), //The name to identify us as a TestRunnerCommunicator
                    null, //The bind listener (null means we cannot be bound)
                    null); //A pointer to an asynchronous message handler (null means we will only read synchronous messages)

            //IMPORTANT: attempt to bind the agent to
            //work for us. If someone else is already using the agent
            //the bind will fail:
            System.out.print("Attempting to bind " + agentName + "...");
            if (!m_agentCom.bind(agentName, 60000)) {
                //Why?
                Object obj = m_agentCom.getMessage(60000);
                throw new Exception("ERROR: Unable to bind " + agentName + ". Reason: " + (obj == null ? "Unknown" : obj.toString()));
            }
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

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("Arguments:");
        System.out.println("   -ServerURL:    TestRunner Control Server");
        System.out.println("   -AgentName:    The name of the test runner agent that will run the script");
        System.out.println("   -ScriptPath:   Full UNC path to the script (path that will be accessible from");
        System.out.println("    the (agent launching the script)");
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
    public int run() throws Exception {
        if (m_agentCom == null)
            return -1;
        m_agentCom.sendMessage(m_agentName, m_launchDescr);
        m_agentCom.setSendDisplayObjs(true);
        Object msg;

        //Listen for LAUNCH_SUCCESS. TRAgent always sends TRMsg with either LAUNCH_SUCCESS or LAUNCH_FAILURE
        //after a process is started:
        while (true) {
            msg = m_agentCom.getMessage(60000);
            if (msg == null) {
                throw new Exception("Timed out waiting for script to start");
            } else {
                if (msg instanceof TRDisplayMsg) {
                    System.out.println(msg);
                    continue;
                } else if (!(msg instanceof TRMsg && ((TRMsg) msg).getMessage().equals(TRAgent.LAUNCH_SUCCESS))) {
                    throw new Exception("Error launching script: " + msg);
                }
            }
            break;
        }

        //Start a user input handler if we were launched by main():
        if (m_startInputHandler) {
            m_userInputHandler = new UserInputHandler();
        }

        m_pid = (Integer) m_agentCom.getProperties().get(TRAgent.PID);
        System.out.println("Launched Successfully [pid = " + m_pid + "]");
        while (true) {
            //If there is a script listener registered
            //Send the ouput to the listener. Otherwise dump to
            //System.out:
            msg = m_agentCom.getMessage(60000);

            //All entities communicating with TRAgent must be able to handle
            //a TRDisplayObj to deal with non-app related TestRunner messages:
            if (msg instanceof TRDisplayMsg) {
                System.out.println(msg.toString());
                continue;
            }

            if (msg != null) {
                //If the process is done kill it and wait for the exit value
                //to return.
                if (msg instanceof TRMsg) {
                    if (((TRMsg) msg).getMessage().equals(TRAgent.PROCESS_DONE)) {
                        m_agentCom.sendMessage(m_agentName, new TRLaunchKill(), TRAgent.PID, m_pid);
                        msg = null;
                        msg = m_agentCom.getMessage(60000);
                        if (msg != null && msg instanceof TRMsg && ((TRMsg) msg).getMessage().equals(TRAgent.KILL_SUCCESS)) {
                            retValue = ((Integer) m_agentCom.getProperties().get(TRAgent.EXIT_VALUE)).intValue();
                        }
                        close();
                        return retValue;
                    }
                    //The process might have been killed by our UserInputHandler
                    if (((TRMsg) msg).getMessage().equals(TRAgent.KILL_SUCCESS) || ((TRMsg) msg).getMessage().equals(TRAgent.KILL_FAILURE)) {
                        System.out.println("Process killed.");
                        retValue = ((Integer) m_agentCom.getProperties().get(TRAgent.EXIT_VALUE)).intValue();
                        close();
                        return retValue;
                    }
                }
                if (msg instanceof TRErrorMsg) {
                    msg = new String("ERROR: " + ((TRErrorMsg) msg).getMessage());
                }

                //If a java program has registered a script listener pass the script output to
                //it, otherwise just dump it to stdout:
                if (scriptListener != null) {
                    scriptListener.scriptOutputNotify(this, msg.toString());
                } else {
                    //Prevent from echoing what the user just typed in:
                    if (m_lastRead == null || !msg.toString().startsWith(m_lastRead)) {
                        System.out.print(msg);
                    } else {
                        m_lastRead = null;
                    }
                }
            }
        }
    }

    /**
     * As the script launcher allocates a fair number of resources it should be
     * closed when it no longer being used.
     */
    public synchronized void close() {
        if (m_agentCom != null) {
            try {
                System.out.print("Releasing " + m_agentName + "...");
                if (m_agentCom.releaseAll(30000)) {
                    System.out.println("DONE");
                } else {
                    System.out.println("ERROR");
                }
            } catch (Exception e) {
                System.out.println("ERROR: releasing " + m_agentName);
            }

            try {
                m_agentCom.close();
            } catch (Exception e) {
                System.out.println("ERROR: closing the communicator.");
            }
            m_agentCom = null;
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
        if (m_agentCom != null) {
            m_agentCom.sendMessage(m_agentName, utf, TRAgent.PID, m_pid);
        }
    }

    /**
     * Sets whether the TRScriptLauncher prints debug output to System.out
     * 
     * @param val
     *            The debugFlag
     */
    public void setDebug(boolean val) {
        //DEBUG = val;
        if (m_agentCom != null) {
            m_agentCom.setDebug(val);
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
                        m_agentCom.sendMessage(m_agentName, new TRLaunchKill(), TRAgent.PID, m_pid);
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
}