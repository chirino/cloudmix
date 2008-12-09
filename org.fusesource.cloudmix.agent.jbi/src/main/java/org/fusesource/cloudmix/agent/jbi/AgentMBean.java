/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
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
