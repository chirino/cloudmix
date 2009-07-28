package org.fusesource.testrunner.rmi;

import java.rmi.RemoteException;

/**
 * @author chirino
*/
public interface IRemoteProcess extends IRemoteStreamListener {
    public boolean isRunning() throws RemoteException;

    public void kill() throws Exception;
}