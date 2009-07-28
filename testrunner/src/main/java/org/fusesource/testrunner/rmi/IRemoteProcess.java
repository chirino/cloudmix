package org.fusesource.testrunner.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.fusesource.testrunner.Process;

/**
 * @author chirino
*/
public interface IRemoteProcess extends Remote, Process {
    public boolean isRunning() throws RemoteException;

    public void kill() throws RemoteException;
}