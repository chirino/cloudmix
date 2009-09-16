/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.spi.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.AgentDetailsList;


/**
 * @version $Revision: 1.1 $
 */
@Path("/agents")
public class AgentsResource extends ResourceSupport {
    private static final transient Log LOG = LogFactory.getLog(AgentsResource.class);

    @Context
    private UriInfo uriInfo;
    @Inject
    private GridController controller;

    public void setController(GridController controller) {
        this.controller = controller;
    }

    public Collection<AgentDetails> getAgents() {
        return controller.getAllAgentDetails();
    }


    @POST
    @Consumes("application/xml")
    public Response post(AgentDetails agentDetails) throws URISyntaxException {
        String agentId = controller.addAgentDetails(agentDetails);
        LOG.info("Created agent: " + agentId + " from: " + agentDetails);
        
        URI uri = uriInfo.getAbsolutePathBuilder().path(agentId).build();
        return Response.created(uri).build();
    }

    @GET
    public AgentDetailsList getAgentDetailsList() {
        LOG.debug("getMachines() with controller: " + controller);
        return new AgentDetailsList(getAgents());
    }

    /**
     * Keeps the machine alive due to the ping
     */
    @Path("{agentId}")
    public AgentResource getAgentResource(@PathParam("agentId")String agentId) {
        return new AgentResource(controller, agentId);
    }

}
