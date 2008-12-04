/**
 *
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