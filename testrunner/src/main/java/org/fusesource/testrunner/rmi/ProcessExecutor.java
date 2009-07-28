/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.testrunner.rmi;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * @version $Revision: 1.1 $
 */
public interface ProcessExecutor extends IProcess {
    void start() throws Exception;

    boolean isRunning() throws RemoteException;

    void kill() throws RemoteException;

    void ping(long timeout) throws RemoteException;

    void open(int fd) throws RemoteException, IOException;

    void write(int fd, byte[] data) throws RemoteException;

    void close(int fd) throws RemoteException;
}
