/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.sun.jersey.spi.inject.Inject;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetailsList;
import org.fusesource.cloudmix.controller.properties.PropertiesEvaluator;



@Path("/profiles")
public class ProfilesResource extends ResourceSupport {
    @Inject
    private GridController controller;

    @Inject
    private PropertiesEvaluator propertiesEvaluator;

    public void setController(GridController c) {
        controller = c;
    }

    public List<ProfileDetails> getProfiles() {
        return getProfileList().getProfiles();
    }

    @GET
    public ProfileDetailsList getProfileList() {
        return new ProfileDetailsList(controller.getProfiles());
    }

    @Path("{id}")
    public ProfileResource getProfile(@PathParam("id") String id) {
        return new ProfileResource(controller, propertiesEvaluator, id);
    }

    public GridController getController() {
        return controller;
    }

    public PropertiesEvaluator getPropertiesEvaluator() {
        return propertiesEvaluator;
    }
}
