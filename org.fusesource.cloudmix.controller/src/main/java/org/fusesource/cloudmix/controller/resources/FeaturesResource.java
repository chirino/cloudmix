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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetailsList;


/**
 * @version $Revision$
 */
@Path("/features")
public class FeaturesResource extends ResourceSupport {
    private static final transient Log LOG = LogFactory.getLog(FeaturesResource.class);

    @Inject
    private GridController controller;

    public void setController(GridController controller) {
        this.controller = controller;
    }

    public List<FeatureDetails> getFeatures() {
        return getFeaturesList().getFeatures();
    }

    @GET
    public FeatureDetailsList getFeaturesList() {
        LOG.debug("getFeatures() with controller: " + controller);
        return new FeatureDetailsList(controller.getFeatures());
    }

    @Path("{featureId}")
    public FeatureResource feature(@PathParam("featureId") String featureId) {
        return new FeatureResource(controller, featureId);
    }
}