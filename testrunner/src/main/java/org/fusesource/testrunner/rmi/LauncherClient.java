package org.fusesource.testrunner.rmi;

import org.fusesource.rmiviajms.JMSRemoteObject;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.Destination;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.rmi.RemoteException;

/**
 * @author chirino
 */
public class LauncherClient {

    private long killTimeout;
    private long launchTimeout;
    private long bindTimeout = 1000*30;
    private AtomicBoolean closed = new AtomicBoolean();
    private final String name;

    private final HashMap<String, IProcessLauncher> boundAgents = new HashMap<String, IProcessLauncher>();

    public LauncherClient(String name) {
        this.name = name;
    }


    public void bindAgent(String agentName) throws Exception {
        checkNotClosed();

        agentName = agentName.toUpperCase();
        if (boundAgents.containsKey(agentName)) {
            return;
        }

        IProcessLauncher agent = getAgent(agentName);
        agent.bind(name);
    }

    private IProcessLauncher getAgent(String agentName) throws RemoteException {
        Destination destination = new ActiveMQQueue(agentName);
        IProcessLauncher agent = (IProcessLauncher) JMSRemoteObject.toProxy(destination, IProcessLauncher.class, null);
        return agent;
    }

    public void releaseAgent(String agentName) throws Exception {
        checkNotClosed();
        agentName = agentName.toUpperCase();
        IProcessLauncher agent = boundAgents.remove(agentName);
        if (agent!=null) {
            agent.unbind(name);
        }
    }

    public void releaseAll() throws Exception {
        checkNotClosed();

        ArrayList<String> failed = new ArrayList<String>();
        for (Map.Entry<String, IProcessLauncher> entry : boundAgents.entrySet()) {
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
//            listener.onTRException("Error releasing agents.", e);
        }
        closed.set(true);
    }

    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("closed");
        }
    }


    public IProcess launchProcess(String agentName, LaunchDescription trld, IProcessListener handler) throws Exception {
        checkNotClosed();
        agentName = agentName.toUpperCase();

        IProcessLauncher agent = boundAgents.get(agentName);
        if( agent == null ) {
            agent = getAgent(agentName);
        }

        return agent.launch(trld, handler);
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

    public void println(IProcess process, String line) throws RemoteException {
        byte [] data = (line+"\n").getBytes();
        process.write(IStream.FD_STD_IN, data);
    }
}