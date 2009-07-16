/**
 * Allows communication between Java processes through InputStream and OutputStream
 * ProcessCommunicatior Objects should be used in pairs.
 * The object launching the process needs to create a TRProcessCommunicator Object to listen
 * in on the output stream and write to the input stream of the Process it has launched.
 * The Process that is launched needs to create a ProcesCommunicator Object to listen
 * in on its own input stream and write to its own output stream
 *
 *Communication Diagram:
 *
 * LauncherProcess                           LaunchedProcess
 *   writes to LaunchedProcess.in----\   /----writes to System.in
 *                                    \ /                                      \
 *                                     X
 *                                    / \
 *   read from LaunchedProcess.out-- /   \----reads from System.out
 *
 *  LauncherApp
 *   Launches Process
 *   Must create TRProcessCommunicator Object to communicate with the launched process
 *
 *  ProcessLaunchedApp
 *  Must create TRProcessCommunicator Objec to communicate with the app that launched it
 *
 *  Optionally, the TRAgent may expose a port for process object output.
 *
 *@author Luis Nicolaou (lnicolao@progress.com)
 *@since 01/31/01
 *
 */
package org.fusesource.testrunner;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class TRProcessCommunicator {
    private static boolean DEBUG = false;
    public static final String STREAM_INIT_STR = "Communication Started";

    //////////////////
    //ProcessCommunicatior memeber variables
    private TRLoaderObjectInputStream m_TRLObjInputStream = null; //input object stream
    private ObjectInputStream m_objInputStream = null;
    private ObjectOutputStream m_objOutputStream = null; //output object stream
    private BufferedInputStream processOutput = null; //output from process
    private PrintWriter m_printStream = null; //output data stream

    private boolean m_isLaunchedProcess = false; //True if the input stream is System.in then this TRProcessCommunicator is expecting to be talking to TestRunner
    private boolean m_objectStreamFlag = false; //True if the type of i/o streams are to be object streams. Otherwise they will be data streams.
    private BufferedWriter m_log = null; //writes exception to m_appName-ProcessCommmunicatorExceptionLog[SystemDate].log
    private String m_appName = null;

    private TRClassLoader m_classLoader = null; //So that we can dynamically load classes without having them in the primordial classpath

    private Socket m_portSocket = null; // optional socket for object output exposed by TRAgent
    private OutputStream m_portOutputStream = null; // optional output stream to port

    /**
     * Constructor
     * 
     *@param the
     *            input stream to use for reading of objects
     *@param the
     *            output stream to use for writing of objects
     * 
     *@exception throws exception if it can't create input or output streams
     **/
    public TRProcessCommunicator(InputStream input, OutputStream output, String appName, boolean objectStreamFlag) throws Exception {
        m_appName = appName;
        m_objectStreamFlag = objectStreamFlag;

        //set whether this process is the launched process
        if (input == System.in)
            m_isLaunchedProcess = true;

        //construct output and input stream for communication
        try {
            //Get the class loader (which will only be able to load classes from primordial classLoader:
            m_classLoader = new TRClassLoader("");

            //create output stream
            if (m_objectStreamFlag) {
                OutputStream objectOutput;
                if (m_isLaunchedProcess)
                    objectOutput = getOutputStream(); // check whether a port is exposed
                else
                    objectOutput = output;

                m_objOutputStream = new ObjectOutputStream(new BufferedOutputStream(objectOutput));
            } else {
                m_printStream = new PrintWriter(new BufferedOutputStream(output));
            }

            //if this is the process that was launched then it has to do a first write so that
            //the ProcessObject can create the inputstream on the other end
            if (m_isLaunchedProcess) {
                if (m_objectStreamFlag) {
                    m_objOutputStream.writeObject(STREAM_INIT_STR);
                    m_objOutputStream.flush();
                } else {
                    m_printStream.println(STREAM_INIT_STR);
                    m_printStream.flush();
                }
            }

            processOutput = new BufferedInputStream(input);

            //create input stream
            if (m_objectStreamFlag) {
                if (m_isLaunchedProcess) {
                    m_objInputStream = new ObjectInputStream(processOutput);
                } else {
                    m_TRLObjInputStream = new TRLoaderObjectInputStream(processOutput, m_classLoader);
                    m_TRLObjInputStream.setDebug(DEBUG);
                }
            }
        } catch (Exception ex) {
            //tries to display or log the exception and then throws the exception up
            handleException(ex, "TRProcessCommunicator.constructor:");
            throw ex;
        }

    }

    private void handleException(Exception ex, String prefix) throws Exception {
        log("" + new Date(System.currentTimeMillis()));
        log(m_appName + ":" + prefix + " " + ex.getMessage());

        //if you aren't a launchedProcess print exception
        if (!m_isLaunchedProcess) {
            ex.printStackTrace();
        } else //if you are a launchedProcess then log the exception to disk
        {
            try {
                StringWriter stackTrace = new StringWriter();
                ex.printStackTrace(new PrintWriter(stackTrace));
                m_log.write(stackTrace.toString());
                m_log.flush();

            } catch (Exception fatalex) {
            }
        }
    }

    /**
     * Prints message by either writing it to System.out or by writing it to the
     * output stream for display at the other end
     * 
     **/
    public void println(String message) throws Exception {
        if (!m_isLaunchedProcess) {
            System.out.println("" + message);
        } else {
            writeObject(new TRDisplayMsg(message));
        }
    }

    public void writeError(String msg, Throwable thrown) throws Exception {
        writeObject(new TRErrorMsg(msg, thrown));
    }

    /**
    *
    **/
    public synchronized void writeObject(Object obj) throws Exception {
        try {
            if (m_objectStreamFlag) {
                try {
                    m_objOutputStream.writeObject(obj);
                    m_objOutputStream.flush();
                } catch (Exception e) {
                    if (!m_isLaunchedProcess) {
                        System.out.println("ERROR: writing object into msg");
                        e.printStackTrace();
                    }
                }
            } else {
                m_printStream.println(obj.toString());
                m_printStream.flush();
            }
        } catch (Exception ex) {
            //tries to display or log the exception and then throws the exception up
            handleException(ex, "TRProcessCommunicator.writeObject: ");
            throw (ex);
        }

    }

    /**
    *
    **/
    public Object readObject() throws Exception {
        Object objIn = null;
        String line = "";
        try {
            if (m_objectStreamFlag) {
                if (m_isLaunchedProcess) {
                    while (m_objInputStream != null && (objIn = m_objInputStream.readObject()) != null) {
                        //break to return the object
                        break;
                    }
                } else {
                    try {
                        while (m_TRLObjInputStream != null && (objIn = m_TRLObjInputStream.recoverableReadObject()) != null) {
                            //if the object read is a String then print it:
                            if (objIn instanceof TRDisplayMsg) {
                                ((TRDisplayMsg) objIn).setSource("PROCESS");
                                //System.out.println(objIn);
                            }
                            break;
                        }
                    }
                    //Catch error with non object
                    catch (java.io.StreamCorruptedException sce) {
                        objIn = new TRErrorMsg("ERROR: non-serialized data detected in process' output stream.", sce);
                    }
                }
            } else {
                boolean newline = false;
                String newLineString = System.getProperty("line.separator", "\n");

                while (processOutput != null) {
                    long delay = 500;

                    int nextByte;
                    nextByte = processOutput.read();
                    if (nextByte == -1) {
                        throw new EOFException();
                    }
                    line += (char) nextByte;

                    //Check to see if we reached the end of a line:
                    if (newLineString.charAt(0) == nextByte) {
                        if (newLineString.length() > 1) {
                            nextByte = processOutput.read();
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
                    if (!newline && processOutput.available() == 0) {
                        Thread.sleep(delay);
                    }

                    //If this line isn't blank and there is nothing else to read
                    //even after the delay return what we have so far.
                    if (line != null && line != "" && (processOutput.available() == 0 || newline)) {
                        return line;
                    }
                }
            }

            if (!m_isLaunchedProcess) {
                if (DEBUG)
                    System.out.println("about to return objIn: " + objIn);
            }
            return objIn;
        }

        catch (EOFException eof) {
            if (line.length() != 0) {
                return line;
            }
            throw eof;
        } catch (Exception ex) {
            //tries to display or log the exception and then throws the exception up
            //handleException(ex, "TRProcessCommunicator.readObject: ");
            throw (ex);
        }
    }

    public void setClassLoader(TRClassLoader trcl) {
        //First Check if we are using a Loading object stream. If not
        //We don't need to set the classloader for the stream because
        //We'll only be reading primitive stream data from the data stream:
        if (m_TRLObjInputStream != null) {
            m_TRLObjInputStream.setClassLoader(trcl);
            return;
        }

        //If null is passed in set to use default classloader:
        if (trcl == null) {
            try {
                m_classLoader = new TRClassLoader("");
            } catch (Exception e) {
            } //No action keep old class loader:
        }

        if (!m_isLaunchedProcess && DEBUG) {
            System.out.println("Turning on TRClassLoader debug.");
            m_classLoader.setDebug(true);
        }
    }

    public void setDebug(boolean val) {
        DEBUG = val;
        if (m_classLoader != null) {
            m_classLoader.setDebug(val);
        }
    }

    public synchronized void close() throws Exception {
        if (m_classLoader != null) {
            m_classLoader.close();
            m_classLoader = null;
        }

        if (m_TRLObjInputStream != null) {
            if (DEBUG)
                log("TRProcessCommunicator: Closing TRLObjInputStream");
            m_TRLObjInputStream.close();
            m_TRLObjInputStream = null;
            if (DEBUG)
                log("DONE Closing TRLObjInputStream");
        }

        if (m_objOutputStream != null) {
            if (DEBUG)
                log("TRProcessCommunicator: Closing ObjectOutputStream");
            m_objOutputStream.close();
            m_objOutputStream = null;
        }

        if (processOutput != null) {
            if (DEBUG)
                log("TRProcessCommunicator: Closing process Input Stream");
            processOutput.close();
            processOutput = null;
        }

        if (m_printStream != null) {
            if (DEBUG)
                log("TRProcessCommunicator: Closing DataInputStream");
            m_printStream.close();
            m_printStream = null;
        }

        if (m_portOutputStream != null)
            try {
                m_portOutputStream.close();
            } catch (IOException e) {
            }

        if (m_portSocket != null)
            try {
                m_portSocket.close();
            } catch (IOException e) {
            }

        if (m_log != null) {
            m_log.flush();
            m_log.close();
            m_log = null;
        }
    }

    private OutputStream getOutputStream() {
        OutputStream ret = System.out; // default
        int port = -1;
        if (Integer.getInteger(TRAgent.PORT_PROPERTY) != null)
            port = Integer.getInteger(TRAgent.PORT_PROPERTY).intValue();
        if (port < 0) // no port specified in TRA.ini
            return ret;

        if (DEBUG)
            log("INFO: Detected port = " + port);

        int pid = -1;
        if (Integer.getInteger(TRAgent.PID_PROPERTY) != null)
            pid = Integer.getInteger(TRAgent.PID_PROPERTY).intValue();
        if (pid < 0) {
            log("ERROR: getOutputStream found port property but not pid");
            return ret;
        }

        if (DEBUG)
            log("INFO: Detected PID = " + pid);

        try {
            m_portSocket = new Socket("localhost", port);
            if (DEBUG)
                log("INFO: Successfully created socket to port = " + port);
        } catch (IOException ioe) {
            log("ERROR: creating socket to port " + port + " - " + ioe);
            return ret;
        }
        try {
            m_portOutputStream = m_portSocket.getOutputStream();
            ObjectOutputStream cout = new ObjectOutputStream(m_portOutputStream); // don't buffer!
            cout.writeObject(TRAgent.PID);
            cout.writeInt(pid);
            cout.flush();
            if (DEBUG)
                log("INFO: Successfully wrote PID info");
        } catch (IOException ioe) {
            log("ERROR: getting port output stream and writing PID info - " + ioe);
        }
        return m_portOutputStream;
    }

    private void log(String s) {
        if (!m_isLaunchedProcess)
            System.out.println(s);
        else {
            try {
                if (m_log == null) {
                    //create last resort exception written to disk option
                    m_log = new BufferedWriter(new FileWriter("ProcessCommmunicator-" + m_appName + "-Exception.log", true));
                }
                log(s);
                m_log.newLine();
                m_log.flush();
            } catch (IOException e) {
            }
        }
    }

}// end of class public class TRProcessCommunicator
