package org.fusesource.testrunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.File;
import java.io.IOException;

/**
 * @author chirino
 */
public class ProcessLauncher {
    public static final long CLEANUP_TIMEOUT = 60000;
    public static final String LOCAL_REPO_PROP = "org.fusesource.testrunner.localRepoDir";

    private String exclusiveOwner;

    private String agentId; //The unique identifier for this agent (specified in ini file);
    private boolean started = false;
    private File dataDirectory = new File(".");
    private File localRepoDirectory;

    //ProcessHandlers:
    private final Map<Integer, LocalProcess> processes = new HashMap<Integer, LocalProcess>();
    int pidCounter = 0;
    private Thread shutdownHook;

    private ResourceManager resourceManager;

    private String commonResourceRepoUrl;

    private Properties properties = System.getProperties();

    synchronized public void bind(String owner) throws Exception {
        if (exclusiveOwner == null) {
            exclusiveOwner = owner;
            System.out.println("Now bound to: " + exclusiveOwner);
        } else if (!exclusiveOwner.equals(owner)) {
            throw new Exception("Bind failure, already bound: " + exclusiveOwner);
        }
    }

    synchronized public void unbind(String owner) throws Exception {
        if (exclusiveOwner == null) {
        } else if (exclusiveOwner.equals(owner)) {
            System.out.println("Bind to " + exclusiveOwner + " released");
            exclusiveOwner = null;
        } else {
            throw new Exception("Release failure, different owner: " + exclusiveOwner);
        }
    }

    synchronized public Process launch(LaunchDescription launchDescription, ProcessListener handler) throws Exception {
        int pid = pidCounter++;
        LocalProcess rc = createLocalProcess(launchDescription, handler, pid);
        processes.put(pid, rc);
        try {
            rc.start();
        } catch (Exception e) {
            processes.remove(pid);
        }
        return rc;
    }

    protected LocalProcess createLocalProcess(LaunchDescription launchDescription, ProcessListener handler, int pid) throws Exception {
        return new LocalProcess(this, launchDescription, handler, pid);
    }

    public synchronized void start() throws Exception {
        if (started) {
            return;
        }

        if (localRepoDirectory == null) {
            localRepoDirectory = new File(dataDirectory, "local-repo");
            properties.setProperty(LOCAL_REPO_PROP, localRepoDirectory.getCanonicalPath());
        }

        if (resourceManager == null) {
            resourceManager = new ResourceManager();
            resourceManager.setLocalRepoDir(localRepoDirectory);
            if (commonResourceRepoUrl != null) {
                resourceManager.setCommonRepo(commonResourceRepoUrl, null);
            }
        }

        started = true;
        if (agentId == null) {

            try {
                setAgentId(java.net.InetAddress.getLocalHost().getHostName());
            } catch (java.net.UnknownHostException uhe) {
                System.out.println("Error determining hostname.");
                uhe.printStackTrace();
                setAgentId("UNDEFINED");
            }
        }

        shutdownHook = new Thread(getAgentId() + "-Shutdown") {
            public void run() {
                System.out.println("Executing Shutdown Hook for " + ProcessLauncher.this);
                ProcessLauncher.this.stop();
            }
        };

        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public synchronized void stop() {
        if (!started) {
            return;
        }

        if (Thread.currentThread() != shutdownHook) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        }

        started = false;

        for (LocalProcess process : processes.values()) {
            try {
                process.kill();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        processes.clear();

        if (resourceManager != null) {
            try {
                resourceManager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    /**
     * Clears the launchers local resource cache.
     * 
     * @throws IOException
     *             If there is an error purging the cache.
     */
    public void purgeResourceRepository() throws IOException {
        if (resourceManager != null) {
            resourceManager.purgeLocalRepo();
        }
    }

    /**
     * Sets the name of the agent id. Once set it cannot be changed.
     * 
     * @param id
     *            the name of the agent id.
     */
    public void setAgentId(String id) {
        if (agentId == null && id != null) {
            agentId = id.trim().toUpperCase();
        }
    }

    /**
     * Sets the url of common resources accessible to this agent. This can be
     * used to pull down things like jvm images from a central location.
     * 
     * @param url
     */
    public void setCommonResourceRepoUrl(String url) {
        commonResourceRepoUrl = url;
    }

    /**
     * Gets the url of common resources accessible to this agent.
     * 
     * @return
     */
    public String getCommonResourceRepoUrl() {
        return commonResourceRepoUrl;
    }

    /**
     * Sets the base directory where the agent puts it's data.
     * 
     * @param dataDirectory
     */
    public void setDataDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public File getDataDirectory() {
        return dataDirectory;
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * @return This agent's id.
     */
    public String getAgentId() {
        return agentId;
    }

    public Map<Integer, LocalProcess> getProcesses() {
        return processes;
    }

    public String toString() {
        return "ProcessLauncer-" + getAgentId();
    }
}