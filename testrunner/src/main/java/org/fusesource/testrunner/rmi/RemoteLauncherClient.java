package org.fusesource.testrunner.rmi;

import org.apache.activemq.command.ActiveMQQueue;
import org.fusesource.rmiviajms.JMSRemoteObject;
import org.fusesource.testrunner.LaunchDescription;

import javax.jms.Destination;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chirino
 */
public class RemoteLauncherClient {

    private long killTimeout;
    private long launchTimeout;
    private long bindTimeout = 1000*30;
    private AtomicBoolean closed = new AtomicBoolean();
    private final String name;

    private final HashMap<String, IRemoteProcessLauncher> boundAgents = new HashMap<String, IRemoteProcessLauncher>();

    public RemoteLauncherClient(String name) {
        this.name = name;
    }


    public void bindAgent(String agentName) throws Exception {
        checkNotClosed();

        agentName = agentName.toUpperCase();
        if (boundAgents.containsKey(agentName)) {
            return;
        }

        IRemoteProcessLauncher agent = getAgent(agentName);
        agent.bind(name);
    }

    private IRemoteProcessLauncher getAgent(String agentName) throws RemoteException {
        Destination destination = new ActiveMQQueue(agentName);
        IRemoteProcessLauncher agent = JMSRemoteObject.toProxy(destination, IRemoteProcessLauncher.class, null);
        return agent;
    }

    public void releaseAgent(String agentName) throws Exception {
        checkNotClosed();
        agentName = agentName.toUpperCase();
        IRemoteProcessLauncher agent = boundAgents.remove(agentName);
        if (agent!=null) {
            agent.unbind(name);
        }
    }

    public void releaseAll() throws Exception {
        checkNotClosed();

        ArrayList<String> failed = new ArrayList<String>();
        for (Map.Entry<String, IRemoteProcessLauncher> entry : boundAgents.entrySet()) {
            try {
                entry.getValue().unbind(name);
            } catch (Exception ignore) {
                failed.add(entry.getKey());
            }
        }
        if (!failed.isEmpty()) {
            throw new Exception("Failed to release: " + failed);
        }
    }

    public void close() {
        try {
            releaseAll();
        } catch (Exception e) {
            e.printStackTrace();
//            listener.onTRException("Error releasing agents.", e);
        }
        closed.set(true);
    }

    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("closed");
        }
    }


    public IRemoteProcess launchProcess(String agentName, LaunchDescription trld, IRemoteProcessListener handler) throws Exception {
        checkNotClosed();
        agentName = agentName.toUpperCase();

        IRemoteProcessLauncher agent = boundAgents.get(agentName);
        if( agent == null ) {
            agent = getAgent(agentName);
        }
        
        return agent.launch(trld, (IRemoteProcessListener) JMSRemoteObject.exportObject(handler));
    }

    public long getBindTimeout() {
        return bindTimeout;
    }

    public void setBindTimeout(long bindTimeout) {
        this.bindTimeout = bindTimeout;
    }

    public long getLaunchTimeout() {
        return launchTimeout;
    }

    public void setLaunchTimeout(long launchTimeout) {
        this.launchTimeout = launchTimeout;
    }

    public long getKillTimeout() {
        return killTimeout;
    }

    public void setKillTimeout(long killTimeout) {
        this.killTimeout = killTimeout;
    }

    public void println(IRemoteProcess remoteProcess, String line) throws RemoteException {
        byte [] data = (line+"\n").getBytes();
        remoteProcess.write(IRemoteStreamListener.FD_STD_IN, data);
    }
}