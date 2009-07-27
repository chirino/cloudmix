package org.fusesource.testrunner.rmi;

import java.rmi.RemoteException;

/**
 * @author chirino
*/
public interface IProcess extends IStream {
    public boolean isRunning() throws RemoteException;

    public void kill() throws Exception;
}