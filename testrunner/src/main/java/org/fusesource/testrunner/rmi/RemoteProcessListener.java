package org.fusesource.testrunner.rmi;

import org.fusesource.testrunner.ProcessListener;

import java.rmi.RemoteException;
import java.io.IOException;

/**
 * @author chirino
 */
class RemoteProcessListener implements ProcessListener {

    private final IRemoteProcessListener listener;

    public RemoteProcessListener(IRemoteProcessListener handler) {
        this.listener = handler;
    }

    public void onProcessExit(int exitCode) {

        try {
            listener.onExit(exitCode);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void onProcessError(Throwable thrown){
        try {
            listener.onError(thrown);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void onProcessInfo(String message) {
        try {
            listener.onInfoLogging(message);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void onProcessOutput(int fd, byte[] data)  {
        try {
            listener.onStreamOutput(fd, data);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public IRemoteProcessListener getListener() {
        return listener;
    }
}