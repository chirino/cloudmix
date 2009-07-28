/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.testrunner.rmi;

import org.fusesource.rmiviajms.JMSRemoteObject;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * An implementation of {@link org.fusesource.testrunner.rmi.ProcessExecutor} which exposes itself over
 * RMI over JMS
 *
 * @version $Revision: 1.1 $
*/
public class RmiProcessExecutor extends JMSRemoteObject implements ProcessExecutor {
    private ProcessExecutor delegate;


    public RmiProcessExecutor(ProcessExecutor delegate) throws RemoteException {
        this.delegate = delegate;
    }

    public RmiProcessExecutor(ProcessLauncher processLauncher, LaunchDescription ld, IProcessListener listener, int pid) throws RemoteException {
        this(new LocalProcessExecutor(processLauncher, ld, listener, pid));
    }

    public void close(int fd) throws RemoteException {
        delegate.close(fd);
    }

    public boolean isRunning() throws RemoteException {
        return delegate.isRunning();
    }

    public void kill() throws RemoteException {
        delegate.kill();
    }

    public void open(int fd) throws RemoteException, IOException {
        delegate.open(fd);
    }

    public void ping(long timeout) throws RemoteException {
        delegate.ping(timeout);
    }

    public void start() throws Exception {
        delegate.start();
    }

    public void write(int fd, byte[] data) throws RemoteException {
        delegate.write(fd, data);
    }
}