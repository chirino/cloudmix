/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.fusesource.cloudmix.common.dto.AgentDetails;

public class EndpointRegistry {

    private static final transient Log LOG = LogFactory.getLog(EndpointRegistry.class);

    private RestGridClient gridClient;
    private InstallerAgent agent;

    /**
     * Add an endpoint to the agent details.
     *
     * @param id the endpoint key
     * @param ref the endpoint reference
     */
    public void addEndpoint(String id, W3CEndpointReference ref) {
        LOG.info("adding endpoint: " + id);
        try { 
            getAgentDetails().addEndpoint(id, ref);
            getClient().updateAgentDetails(getAgent().getAgentId(), 
                                           getAgentDetails());
        } catch(Throwable t) {
            LOG.warn("update agent details failed", t);
        }
    }
    
    /**
     * Remove an endpoint from the agent details.
     *
     * @param id the endpoint key
     * @return true if endpoint already exists
     */
    public boolean removeEndpoint(String id) {
        LOG.info("removing endpoint: " + id);
        boolean exists = false;
        try { 
            exists = getAgentDetails().removeEndpoint(id);
            if (exists) {
                getClient().updateAgentDetails(getAgent().getAgentId(),
                                               getAgentDetails());
            }
        } catch(Throwable t) {
            LOG.warn("update agent details failed", t);
        }
        return exists; 
    }

    public void setClient(RestGridClient gridClient) {
        this.gridClient = gridClient;
    }

    public RestGridClient getClient() {
        return gridClient;
    }

    public void setAgent(InstallerAgent agent) {
        this.agent = agent;
    }
    
    public InstallerAgent getAgent() {
        return agent;
    }

    /**
     * @return agent details
     */
    private final AgentDetails getAgentDetails() {
        return getAgent().getAgentDetails();
    }
}
