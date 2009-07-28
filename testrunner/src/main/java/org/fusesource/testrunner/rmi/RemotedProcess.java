package org.fusesource.testrunner.rmi;

import org.fusesource.testrunner.LocalProcess;
import org.fusesource.testrunner.LocalProcessLauncher;
import org.fusesource.testrunner.LaunchDescription;
import org.fusesource.testrunner.LocalProcessListener;
import org.fusesource.rmiviajms.JMSRemoteObject;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.io.IOException;

/**
 * @author chirino
*/
class RemotedProcess extends LocalProcess {


    public class RemoteProcess implements IRemoteProcess {
        public boolean isRunning() throws RemoteException {
                return RemotedProcess.this.isRunning();
            }

        public void kill() throws RemoteException {
            RemotedProcess.this.kill();
        }

        public void open(int fd) throws IOException {
            RemotedProcess.this.open(fd);
        }

        public void close(int fd) throws RemoteException {
            RemotedProcess.this.close(fd);
        }

        public void write(int fd, byte[] data) throws RemoteException {
                try {
                    RemotedProcess.this.write(fd, data);
                } catch (IOException e) {
                }
            }
    }

    final private IRemoteProcess server = new RemoteProcess();
    IRemoteProcess proxy;

    public void ping(long timeout) {
        if (listener != null) {
            //Check to see if the client that started the process is still around.
            try {
                RemoteProcessListener rpl = (RemoteProcessListener)listener;
                IRemoteProcessListener l = rpl.getListener();
                l.ping();
            } catch (Exception e) {
                try {
                    kill();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    public RemotedProcess(LocalProcessLauncher launcher, LaunchDescription launchDescription, LocalProcessListener handler, int pid) {
        super(launcher, launchDescription, handler, pid);
    }

    @Override
    public void start() throws Exception {
        super.start();
        proxy = (IRemoteProcess) JMSRemoteObject.exportObject(server);
    }

    @Override
    protected void onExit(int exitValue) {
        try {
            JMSRemoteObject.unexportObject(proxy, true);
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }
        super.onExit(exitValue);
    }

    public IRemoteProcess getProxy() {
        return proxy;
    }

}