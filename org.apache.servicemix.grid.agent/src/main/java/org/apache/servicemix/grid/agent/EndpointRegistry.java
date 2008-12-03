/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.grid.agent;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.servicemix.grid.common.dto.AgentDetails;

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
