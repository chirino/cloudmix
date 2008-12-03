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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;

import org.apache.servicemix.grid.common.GridController;
import org.apache.servicemix.grid.common.dto.ProfileDetails;

public class ProfileResource extends ResourceSupport {
    final GridController controller;
    final String profileId;

    public ProfileResource(GridController ctrl, String id) {
        controller = ctrl;
        profileId = id;
    }
    
    @GET
    public ProfileDetails getProfileDetails() {
        return controller.getProfileDetails(profileId);
    }
    
    @PUT
    public void addProfileDetails(ProfileDetails profileDetails) {
        profileDetails.setId(profileId);
        controller.addProfile(profileDetails);
    }
    
    @DELETE
    public void delete() {
        controller.removeProfile(profileId);
    }
}
