package org.fusesource.testrunner.rmi;

import org.fusesource.testrunner.LocalProcessLauncher;
import org.fusesource.testrunner.LaunchDescription;
import org.fusesource.testrunner.LocalProcess;
import org.fusesource.testrunner.LocalProcessListener;
import org.fusesource.rmiviajms.JMSRemoteObject;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.Destination;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.io.File;
import java.util.Map;

/**
 * @author chirino
 */
public class RemoteProcessLauncher implements IRemoteProcessLauncher {

    private IRemoteProcessLauncher proxy;
    private final LocalProcessLauncher processLauncher = new LocalProcessLauncher() {
        protected LocalProcess createLocalProcess(LaunchDescription launchDescription, LocalProcessListener handler, int pid) throws RemoteException {
            return new RemotedProcess(this, launchDescription, handler, pid);
        }
    };
    private RemoteListenerMonitor monitor = new RemoteListenerMonitor(processLauncher);


    public void bind(String owner) throws Exception {
        processLauncher.bind(owner);
    }

    public void unbind(String owner) throws Exception {
        processLauncher.unbind(owner);
    }

    public IRemoteProcess launch(LaunchDescription launchDescription, IRemoteProcessListener listener) throws Exception {
        RemotedProcess lp = (RemotedProcess)processLauncher.launch(launchDescription, new RemoteProcessListener(listener));
        return lp.getProxy();
    }

    public void start() throws Exception {
        processLauncher.start();
        Destination destination = new ActiveMQQueue(processLauncher.getAgentId());
        proxy = (IRemoteProcessLauncher) JMSRemoteObject.exportObject(this, destination);
        monitor.start();
    }

    public void stop() throws NoSuchObjectException {
        JMSRemoteObject.unexportObject(proxy, false);
        processLauncher.stop();
        monitor.stop();
    }


    public String getAgentId() {
        return processLauncher.getAgentId();
    }

    public void setAgentId(String id) {
        processLauncher.setAgentId(id);
    }

    public File getDataDirectory() {
        return processLauncher.getDataDirectory();
    }

    public void setDataDirectory(File dataDirectory) {
        processLauncher.setDataDirectory(dataDirectory);
    }

    public Map<Integer, LocalProcess> getProcesses() {
        return processLauncher.getProcesses();
    }


    /*
     * public static void main()
     *
     * Defines the entry point into this app.
     */
    public static void main(String[] argv) {
        System.out.println("\n\n PROCESS LAUNCHER\n");

        String jv = System.getProperty("java.version").substring(0, 3);
        if (jv.compareTo("1.5") < 0) {
            System.err.println("The LocalProcessLauncher agent requires jdk 1.4 or higher to run, the current java version is " + System.getProperty("java.version"));
            System.exit(-1);
            return;
        }

        if (argv.length > 1) {
            System.err.println("Too many arguments.");
            System.exit(-1);
        }
        RemoteProcessLauncher agent = new RemoteProcessLauncher();
        //        agent.setPropFileName(argv[0]);
        try {
            agent.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}