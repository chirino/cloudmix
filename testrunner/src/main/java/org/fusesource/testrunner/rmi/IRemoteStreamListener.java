package org.fusesource.testrunner.rmi;

import org.fusesource.rmiviajms.Oneway;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author chirino
*/
public interface IRemoteStreamListener extends Remote {
    int FD_STD_IN = 0;
    int FD_STD_OUT = 1;
    int FD_STD_ERR = 2;

    public void open(int fd) throws RemoteException, IOException;

    @Oneway
    public void write(int fd, byte[] data) throws RemoteException;

    @Oneway
    public void close(int fd) throws RemoteException;
}