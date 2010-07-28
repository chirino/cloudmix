/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.StringList;


/**
 * @version $Revision$
 */
public class FeatureResource extends ResourceSupport {
    private static final transient Log LOG = LogFactory.getLog(FeatureResource.class);

    private final GridController controller;
    private final String featureId;

    public FeatureResource(GridController controller, String featureId) {
        this.controller = controller;
        this.featureId = featureId;
    }

    @GET
    public FeatureDetails getFeatureDetails() {
        return controller.getFeature(featureId);
    }

    @PUT
    public void addFeatureDetails(FeatureDetails featureDetails) {
        featureDetails.setId(featureId);
        controller.addFeature(featureDetails);
    }

    @DELETE
    public void delete() {
        controller.removeFeature(featureId);
    }
    
    @GET @Path("agents")
    public StringList getAgentList() {
        return new StringList(controller.getAgentsAssignedToFeature(featureId));
    }

    @PUT @Path("agents/{id}")
    public void addAgent(@PathParam("id")String agentId) {
        // TODO need to add support for configuration overrides... if that API is going to be ever again...
        controller.addAgentToFeature(featureId, agentId, null);
    }

    @DELETE @Path("agents/{id}")
    public void removeAgent(@PathParam("id")String agentId) {
        controller.removeAgentFromFeature(featureId, agentId);
    }

    public List<AgentDetails> getAgents() {
        List<AgentDetails> answer = new ArrayList<AgentDetails>();
        List<String> agentIdList = controller.getAgentsAssignedToFeature(featureId);
        System.out.println(">>>>> found agents for feature id " + featureId + " are  " + agentIdList);
        for (String agentId : agentIdList) {
            AgentDetails agentDetails = controller.getAgentDetails(agentId);
            if (agentDetails != null) {
                answer.add(agentDetails);
            } else {
                LOG.warn("No agent details found for " + agentId + " when resolving feature " + featureId);
            }
        }
        System.out.println("Found: " + answer);
        return answer;
    }

    public String getFeatureId() {
        return featureId;
    }
}