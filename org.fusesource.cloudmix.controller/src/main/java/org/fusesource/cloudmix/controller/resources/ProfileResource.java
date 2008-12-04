/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;

import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.ProfileDetails;

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
