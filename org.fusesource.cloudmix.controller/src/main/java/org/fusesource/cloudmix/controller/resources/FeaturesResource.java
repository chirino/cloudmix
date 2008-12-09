/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.FeatureDetailsList;

/**
 * @version $Revision: 1.1 $
 */
@Path("/features")
public class FeaturesResource extends ResourceSupport {
    private static final transient Log LOG = LogFactory.getLog(FeaturesResource.class);

    @Context
    private UriInfo uriInfo;
    private GridController controller;

    public void setController(GridController controller) {
        this.controller = controller;
    }

    @GET
    @Produces({"application/xml", "application/json" })
    public FeatureDetailsList getFeatures() {
        LOG.debug("getFeatures() with controller: " + controller);
        return new FeatureDetailsList(controller.getFeatureDetails());
    }

    @Path("{featureId}")
    public FeatureResource feature(@PathParam("featureId")String featureId) {
        return new FeatureResource(controller, featureId);
    }
}