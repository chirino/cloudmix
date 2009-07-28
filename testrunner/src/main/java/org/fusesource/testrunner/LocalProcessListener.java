package org.fusesource.testrunner;

/**
 * @author chirino
 */
public interface LocalProcessListener extends LocalStreamListener {

    public void onExit(int exitCode);

    public void onError(Throwable thrown);

    public void onInfoLogging(String message);

}
