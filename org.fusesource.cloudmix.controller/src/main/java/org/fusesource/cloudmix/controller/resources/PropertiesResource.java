/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;

import com.sun.jersey.spi.inject.Inject;
import org.fusesource.cloudmix.common.GridClients;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.controller.properties.PropertiesEvaluator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;
import java.util.Properties;


@Path("/properties")
public class PropertiesResource extends ResourceSupport {
    @Inject
    private GridController controller;

    @Inject
    private PropertiesEvaluator propertiesEvaluator;

    private ProfileResource profileResource;

    public PropertiesResource(ProfileResource profileResource) {
        this.profileResource = profileResource;
    }

    @GET
    public Properties getProperties() {
        ProfileDetails profile = profileResource.getProfileDetails();
        List<FeatureDetails> features = GridClients.getFeatureDetails(controller, profile);
        return propertiesEvaluator.evaluateProperties(features);
    }

}