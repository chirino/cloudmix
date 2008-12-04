/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.ProfileDetailsList;

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
