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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.StringList;

/**
 * @version $Revision: 1.1 $
 */
public class FeatureResource extends ResourceSupport {
    private final GridController controller;
    private final String featureId;

    public FeatureResource(GridController controller, String featureId) {
        this.controller = controller;
        this.featureId = featureId;
    }

    @GET
    public FeatureDetails getFeatureDetails() {
        return controller.getFeatureDetails(featureId);
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
    @Produces({"application/xml", "application/json" })
    public StringList getAgents(@PathParam("featureId")String aFeatureId) {
        return new StringList(controller.getAgentsAssignedToFeature(aFeatureId));
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
    
}