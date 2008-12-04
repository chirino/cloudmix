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