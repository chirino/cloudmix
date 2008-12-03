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
package org.apache.servicemix.grid.controller.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.servicemix.grid.common.GridController;
import org.apache.servicemix.grid.common.dto.ProfileDetailsList;

@Path("/profiles")
public class ProfilesResource extends ResourceSupport {
    GridController controller;
    
    public void setController(GridController c) {
        controller = c;
    }
    
    @GET
    @Produces("application/xml")
    public ProfileDetailsList getProfiles() {
        return new ProfileDetailsList(controller.getProfileDetails());
    }
    
    @Path("{id}")
    public ProfileResource getProfile(@PathParam("id") String id) {
        return new ProfileResource(controller, id);
    }    
}
