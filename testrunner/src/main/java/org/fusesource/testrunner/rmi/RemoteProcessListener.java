package org.fusesource.testrunner.rmi;

import org.fusesource.testrunner.LocalProcessListener;

import java.rmi.RemoteException;
import java.io.IOException;

/**
 * @author chirino
*/
class RemoteProcessListener implements LocalProcessListener {

    private final IRemoteProcessListener listener;

    public RemoteProcessListener(IRemoteProcessListener handler) {
        this.listener = handler;
    }

    public void onExit(int exitCode) {
        try {
            listener.onExit(exitCode);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void onError(Throwable thrown) {
        try {
            listener.onError(thrown);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void onInfoLogging(String message) {
        try {
            listener.onInfoLogging(message);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void open(int fd) throws IOException {
        listener.open(fd);
    }

    public void write(int fd, byte[] data) throws IOException {
        listener.write(fd, data);
    }

    public void close(int fd) throws IOException {
        listener.close(fd);
    }

    public IRemoteProcessListener getListener() {
        return listener;
    }
}