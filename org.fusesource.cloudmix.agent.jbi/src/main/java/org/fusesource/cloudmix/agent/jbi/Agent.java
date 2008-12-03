package org.fusesource.cloudmix.agent.jbi;

import java.net.URI;
import java.net.URISyntaxException;

import org.fusesource.cloudmix.agent.AgentPoller;
import org.fusesource.cloudmix.agent.RestGridClient;

public class Agent implements AgentMBean {

    private JBIInstallerAgent agent;
    private RestGridClient gridClient;
    private AgentPoller poller;

    public String getProfile() {
        return agent.getProfile();
    }

    public Agent(JBIInstallerAgent agent, RestGridClient gridClient, AgentPoller poller) {
        this.agent = agent;
        this.gridClient = gridClient;
        this.poller = poller;
    }

    public String getAgentLink() {
        return agent.getAgentLink();
    }

    public String getContainerType() {
        return agent.getContainerType();
    }

    public String getCurrentFeatures() {
        // TODO: add list of bundles for each feature.
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String feature : agent.getAgentDetails().getCurrentFeatures()) {
            if (first) {
                sb.append(feature);
                first = false;
            } else {
                sb.append(", ").append(feature);
            }
        }
        return sb.toString();
    }

    public String getHostName() {
        return agent.getHostName();
    }

    public long getInitialPollingDelay() {
        return poller.getInitialPollingDelay();
    }

    public int getMaxFeatures() {
        return agent.getMaxFeatures();
    }

    public String getOs() {
        return agent.getAgentDetails().getOs();
    }

    public int getPid() {
        return agent.getAgentDetails().getPid();
    }

    public long getPollingPeriod() {
        return poller.getPollingPeriod();
    }

    
    public URI getRepositoryUri() {
        try {
            return gridClient.getRootUri();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public String getSupportPackageTypes() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String t : agent.getSupportPackageTypes()) {
            if (first) {
                sb.append(t);
                first = false;
            } else {
                sb.append(", ").append(t);
            }
        }
        return sb.toString();
    }

}
