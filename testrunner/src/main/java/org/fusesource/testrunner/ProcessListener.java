package org.fusesource.testrunner;

/**
 * @author chirino
 */
public interface ProcessListener {

    public void onProcessExit(int exitCode);

    public void onProcessError(Throwable thrown);

    public void onProcessInfo(String message);
    
    public void onProcessOutput(int fd, byte [] output);

}
