/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;

import java.util.Date;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;

/**
 * @version $Revision: 1.1 $
 */
public class AgentResource extends ResourceSupport {
    private final GridController controller;
    private final String agentId;

    public AgentResource(GridController controller, String agentId) {
        this.controller = controller;
        this.agentId = agentId;
    }

    @GET
    @Produces({"application/xml", "application/json" })
    public AgentDetails get() {
        return controller.getAgentDetails(agentId);
    }
    
    @PUT
    public void update(AgentDetails details) {
        controller.updateAgentDetails(agentId, details);
    }

    @DELETE
    public void delete() {
        controller.removeAgentDetails(agentId);
    }

    
    @GET @Path("history")
    @Produces({"application/xml", "application/json" })
    public Response history(@Context Request request) {
        ProvisioningHistory answer = getHistory();
        
        EntityTag etag = new EntityTag(answer.getDigest(), true);
        Response.ResponseBuilder rb = request.evaluatePreconditions(etag);
        if (rb != null) {
            return rb.build();
        }
        
        Date lastModified = answer.getLastModified();
        return Response.ok(answer, "application/xml").lastModified(lastModified).tag(etag).build();
    }
    
    public ProvisioningHistory getHistory() {
        return controller.getAgentHistory(agentId);
    }
}