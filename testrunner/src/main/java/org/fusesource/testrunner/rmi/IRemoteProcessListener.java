package org.fusesource.testrunner.rmi;

import org.fusesource.rmiviajms.Oneway;

import java.rmi.RemoteException;

/**
 * @author chirino
*/
public interface IRemoteProcessListener extends IRemoteStreamListener {
    @Oneway
    public void onExit(int exitCode) throws RemoteException;

    @Oneway
    public void onError(Throwable thrown) throws RemoteException;

    @Oneway
    public void onInfoLogging(String message) throws RemoteException;

    public void ping() throws RemoteException;
}