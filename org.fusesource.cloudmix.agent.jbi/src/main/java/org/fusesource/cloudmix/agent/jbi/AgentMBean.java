package org.fusesource.cloudmix.agent.jbi;

import java.net.URI;

public interface AgentMBean {

    String getProfile();
    
    String getHostName();
    
    String getOs();
    
    int getPid();
    
    String getAgentLink();
    
    String getContainerType();
    
    String getSupportPackageTypes();
    
    int getMaxFeatures();
    
    URI getRepositoryUri();
    
    long getPollingPeriod();
    
    long getInitialPollingDelay();
    
    String getCurrentFeatures();
        
}
