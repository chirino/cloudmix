package org.fusesource.testrunner;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Adapted from Tide BasicProcessWrapper
 */
public class BasicProcessWrapper {
    private java.lang.Process _process;
    private ProcessStdOutThread _out;
    private ProcessStdErrorThread _error;
    private String _commandLine;
    private String[] _env = new String[100];
    private int _envCount = 0;
    private String _directory = null;
    private ArrayList _listeners = new ArrayList();
    private int _exitStatus = -1;

    public static final IProcessOutputListener CONSOLE = (new IProcessOutputListener() {
        public void processOutput(String s) {
            System.out.println(s);
        }
    });

    /**
     * Return the exit status for the wrapped process. -1 if process was not yet
     * run or created.
     * 
     * @return
     */
    public int getExitStatus() {
        return _exitStatus;
    }

    /**
     * Return the command line associated with the underlying process.
     * 
     * @return
     */
    public String getCommandLine() {
        return _commandLine;
    }

    /**
     * Set the command line that gets invoked.
     * 
     * @param s
     */
    public void setCommandLine(String s) {
        this._commandLine = s;
    }

    /**
     * Set an environment variable for the process
     * 
     * @param s
     */
    public void setEnv(String s) {
        this._env[_envCount++] = s;
    }

    /**
     * Set an environment variable for the process
     * 
     * @param s
     */
    public void setEnv(String[] s) {
        this._env = s;
    }

    /**
     * Set the initial working directory for the process
     * 
     * @param s
     */
    public void setDirectory(String s) {
        this._directory = s;
    }

    /**
     * Return a collection of output listeners.
     * 
     * @return
     */
    public ArrayList getListeners() {
        return _listeners;
    }

    /**
     * Return a handle to the underlying process.
     * 
     * @return
     */
    public Process getProcess() {
        return _process;
    }

    /**
     * Add a listener to handle output of any kind (std out and std error) from
     * this process.
     * 
     * @param outputListener
     */
    public void addListener(IProcessOutputListener outputListener) {
        _listeners.add(outputListener);
    }

    /**
     * Launch the process with this method. Will block till the process is
     * complete and will then close down streams associated with the underlying
     * process.
     * 
     * @throws IOException
     */
    public void launch() throws IOException {
        String[] env = null;
        if (_envCount > 0) {
            env = _env;
        }

        File dir = null;
        if (_directory != null) {
            dir = new File(_directory);
        }

        if (_directory != null)
            _process = Runtime.getRuntime().exec(getCommandLine(), env, dir);
        else
            _process = Runtime.getRuntime().exec(getCommandLine());

        _out = new ProcessStdOutThread(_process);
        _error = new ProcessStdErrorThread(_process);
        _out.start();
        _error.start();

        try {
            _process.waitFor();
        } catch (InterruptedException e) {
            System.out.println("Process being interrupted...closing down streams and destroying process");
        } finally {
            if (_process != null) {
                _exitStatus = _process.exitValue();
                // Clear out and close the streams...
                _process.getOutputStream().close();
                _process.destroy();
            }
        }
    }

    /**
     * This is a sample usage of this class.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.exit(0);
            }

            BasicProcessWrapper pw = new BasicProcessWrapper();
            pw.addListener(BasicProcessWrapper.CONSOLE);
            String cli = args[0] + " ";
            for (int i = 1; i < args.length; i++) {
                cli += args[i];
                if (i == args.length) {
                    break;
                } else {
                    cli += " ";
                }
            }

            pw.setCommandLine(cli);
            pw.launch();
            System.out.println("Exit status of " + cli + " = " + pw.getExitStatus());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Extend this class to handle different streams (standard out, and standard
     * error)
     * 
     */
    abstract class ProcessOutputThread extends Thread {
        protected java.lang.Process _process;
        protected BufferedReader _reader;
        protected InputStreamReader _isreader;

        ProcessOutputThread(java.lang.Process p) {
            super();
            _process = p;
        }

        protected abstract BufferedReader getInputReader();

        protected abstract void shutdown() throws IOException;

        public Process getProcess() {
            return _process;
        }

        public void run() {
            String result = null;
            Iterator listeners;
            IProcessOutputListener listener;

            try {
                while (true) {
                    result = _reader.readLine();

                    if (result == null) {
                        break;
                    }

                    listeners = getListeners().iterator();
                    while (listeners.hasNext()) {
                        listener = (IProcessOutputListener) listeners.next();
                        listener.processOutput(result);
                    }
                }
            } catch (IOException e) {
                // Ignore IO errors...stream may have been closed by someone else
            } finally {
                try {
                    shutdown();
                } catch (IOException e) {
                    // Ignore io errors here
                }
            }
        }
    }

    /**
     * Used to process standard output from the process.
     * 
     */
    private class ProcessStdOutThread extends ProcessOutputThread {
        Process process;

        public ProcessStdOutThread(java.lang.Process p) {
            super(p);
            process = p;
            _reader = getInputReader();
        }

        protected BufferedReader getInputReader() {
            _isreader = new InputStreamReader(process.getInputStream());
            return new BufferedReader(_isreader);
        }

        protected void shutdown() throws IOException {
            _reader.close();
            _isreader.close();
        }
    }

    /**
     * Used to process standard error from the process.
     * 
     */
    private class ProcessStdErrorThread extends ProcessOutputThread {
        Process process;

        public ProcessStdErrorThread(java.lang.Process p) {
            super(p);
            process = p;
            _reader = getInputReader();
        }

        protected BufferedReader getInputReader() {
            _isreader = new InputStreamReader(process.getErrorStream());
            return new BufferedReader(_isreader);
        }

        protected void shutdown() throws IOException {
            _reader.close();
            _isreader.close();
        }
    }
}
