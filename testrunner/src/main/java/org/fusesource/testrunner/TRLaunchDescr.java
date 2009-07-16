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

import java.util.Vector;
import java.io.Serializable;

/**
 * A TRLaunchDescr is sent to a running TRAgent using a TRJMSCommunicator. The
 * TRLaunchDescr provides the information necessary for the TRAgent to launch
 * either a Java Program or a script. <br>
 * <br>
 * When a Java Program is launched the TRAgent uses the JVM, JVM args,
 * classpath, runName (main classname), and args that are specified. For JVM and
 * classpath tags can be provided instead of or in conjunction with paths. If
 * tags are specified the TRAgent checks its TRA.ini file to resolve the path
 * matching that tag. It is possible to specify part of the application
 * classpath as exposed. Using a <c>setEposedCP()</c> method will tell the
 * TRAgent that it should be ready to dynamically load and instantiate the
 * classes in the portion of the classpath. This is necessary if the either the
 * launched app or the launching app plans to pass application specific classes
 * through the agent. The classpath of the launched process will be made up of
 * both the exposed and regular classpath tags and paths. <br>
 * <br>
 * When a Script is launched the agent will simply try to run the String
 * specified by <c>setRunName<c/> plus the args specified by <c>setArgs</c>.
 * Objects written to the script should only be primitive those that could be
 * handled by a DataStream.
 * 
 * @author Colin MacNaughton (cmacnaug)
 * @see TRJMSCommunicator
 * @see TRProcessCommunicator
 * @see TRAgent
 */
public class TRLaunchDescr implements Serializable {
    //Serialization:
    //private static final long serialVersionUID =

    /**
     * 
     */
    private static final long serialVersionUID = 7438406308694653630L;

    private static boolean DEBUG = false;
    //Two types known programs and unknown:
    //Only implement known for now:

    //_TAG variables suggest that the TestRunnerAgent may know about
    //the variable in question

    //JVM:
    private String JVMTag = null; //A JVM known to TestRunnerAgents
    private String JVMPath = "";
    private String JVMArgs = "";
    private Vector JVMArgTags = null;

    //runName: (Full class name or path to a script to run);
    private String runName = "";
    //working directory for launched process
    private String workingDir = "";
    //environment parameters in the form key=value
    private String[] envParams = null;

    //CLASSPATH INFO:
    private Vector classPathTags = null; //Classpaths known to TestRunnerAgent (specified in TRA.ini file);
    private Vector classPaths = null; //Classpaths accessible by absolute or relative path to TestRunner's working directory:
    //Classpath to classes that may potentially be exposed to TestRunner
    //(In other words classes that may be passed through one of TestRunner's Streams)
    private Vector exposedCPTags = null;
    private Vector exposedCPs = null;

    //ARGUMENTS:
    private String args = "";

    //private static String [] processTypes = {"Java executable", "Script"};

    /**
     * Specifies that the process being launched is a java class with a main
     * method.
     */
    public static final int PROCESS_TYPE_JAVA_EXE = 0;

    /**
     * Specifies that the process being launched is a script.
     */
    public static final int PROCESS_TYPE_SCRIPT = 1;

    /**
     * Specifies that the process being launched is a java class with a main
     * method.
     */
    public static final int PROCESS_TYPE_BOOTSTRAP_JAVA_EXE = 2;

    /**
     * Specified that the process being launched will communicate using java
     * primitives.
     */
    public static final int DATA_OUTPUT = 0;

    /**
     * Specify that the process being launched will communicate by passing Java
     * serializable objects.
     */
    public static final int OBJECT_OUTPUT = 1;

    private int processType = -1;
    private int outputType = -1;

    /**
     * Constructor.
     */
    public TRLaunchDescr() {
        classPathTags = new Vector();
        classPaths = new Vector();
        exposedCPTags = new Vector();
        exposedCPs = new Vector();
        JVMArgTags = new Vector();
    }

    /**
     * The type of process to launch. Launching a script requires that only
     * <c>setRunName</c> and <c>setArgs</c> be called.
     * 
     * @param val
     *            <c>PROCESS_TYPE_JAVA_EXE</c> or <c>PROCESS_TYPE_SCRIPT</c>
     */
    //accessors
    public void setProcessType(int val) {
        processType = val;
    }

    /**
     * Sets whether to run the TRAgent in debug mode.
     * 
     * @param val
     */
    public void setDebug(boolean val) {
        DEBUG = val;
    }

    /**
     * Sets the type of output expected from the launched process. This dictates
     * how the process's output stream is read - either as DataStream or an
     * ObjectStream
     * 
     * @param val
     *            <c>DATA_OUTPUT</c> or <c>OBEJECT_OUTPUT</c>
     */
    public void setOutputType(int val) {
        outputType = val;
    }

    /**
     * If set the TRAgent will try to match this tag against those specified in
     * it TRA.ini. If there is a match the tag will be interrupretted as an
     * accessible path to a jvm's java executable
     * 
     * @param tag
     *            The JVM tag to match.
     */
    public void setJVMTag(String tag) {
        JVMTag = tag;
    }

    /**
     * If there is no JVMTag set then this path will be interpretted as the path
     * to a jvm's java executable.
     * 
     * @param path
     *            The full or relative path from the TRAgents working directory.
     */
    public void setJVMPath(String path) {
        JVMPath = path;
    }

    /**
     * This specifies the args (if any) to specify for the JVM such as memory
     * allocation
     * 
     * @param val
     *            The args as they would appear on the command line.
     */
    public void setJVMArgs(String val) {
        JVMArgs = val;
    }

    /**
     * Adds a tag for the TRAgent to try to match to one in its TRA.ini file and
     * interpret as arguments for a java process' jvm. Can be called more than
     * once to use several tags.
     * 
     * @param tag
     *            A tag to be added to the JVM Args.
     */
    public void addJVMArgTag(String tag) {
        JVMArgTags.addElement(tag);
    }

    /**
     * If a java application is launched this is interpretted as the classname
     * of the main class to be executed. Otherwise interpretted as the path to a
     * script accessible from the TRAgent's working directory
     * 
     * @param name
     *            The main class name or path to the script being run.
     */
    public void setRunName(String name) {
        runName = name;
    }

    /**
     * This specifies an optional list of environment parameters in the form
     * key=value If null, the process inherits the environment variables of the
     * TRAAgent.
     * 
     * @param name
     *            The main class name or path to the script being run.
     */
    public void setEnvParams(String[] params) {
        envParams = params;
    }

    /**
     * This specifies a working directory for the class or script being run. If
     * empty, TRAgent's working directory is used.
     * 
     * @param name
     *            The main class name or path to the script being run.
     */
    public void setWorkingDir(String name) {
        workingDir = name;
    }

    /**
     * Adds a tag for the TRAgent to try to match to one in its TRA.ini file and
     * interpret as a classpath String to append to the classpath of the
     * launched process. Can be called more than once to use several tags.
     * 
     * @param tag
     *            A tag to be added to the classpath.
     */
    public void addClassPathTag(String tag) {
        classPathTags.addElement(tag);
    }

    /**
     * The path to classes to be appended to the classpath generated for the
     * process being launched.
     * 
     * @param path
     *            The path.
     */
    public void addClassPath(String path) {
        classPaths.addElement(path);
    }

    /**
     * Same as <c>addClassPathTag</c> except the TRAgent will attempt to
     * dynamically load the classes pointed to so that it can instantiate them
     * as they are passed from launcher to process or process to launcher.
     * Because there is overhead associated with loading the classes this should
     * only be used when necessary.
     * 
     * @param tag
     *            The classpath tag.
     */
    public void addExposedCPTag(String tag) {
        exposedCPTags.addElement(tag);
    }

    /**
     * Same as <c>addClassPath</c>. See <c>addExposedCPTag()</c>.
     * 
     * @param path
     *            The classpath.
     */
    public void addExposedCP(String path) {
        exposedCPs.addElement(path);
    }

    /**
     * Arguments to be applied to the script or process that is being launched
     * 
     * @param val
     *            The arguments as they would appear on the command line of the
     *            launched process.
     */
    public void setArgs(String val) {
        args = val;
    }

    //GET ACCESSORS:
    public int getProcessType() {
        return processType;
    }

    public boolean getDebug() {
        return DEBUG;
    }

    public int getOutputType() {
        return outputType;
    }

    public String getJVMTag() {
        return JVMTag;
    }

    public String getJVMPath() {
        return JVMPath;
    }

    public String getJVMArgs() {
        return JVMArgs;
    }

    public Vector getJVMArgTags() {
        return JVMArgTags;
    }

    public Vector getClassPathTags() {
        return classPathTags;
    }

    public Vector getClassPaths() {
        return classPaths;
    }

    public String getRunName() {
        return runName;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public String[] getEnvParams() {
        return envParams;
    }

    public Vector getExposedCPTags() {
        return exposedCPTags;
    }

    public Vector getExposedCP() {
        return exposedCPs;
    }

    public String getArgs() {
        return args;
    }

    /*
     * protected void finalize() { processTypes = null; }
     */
}