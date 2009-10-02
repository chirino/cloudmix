/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;

import org.fusesource.cloudmix.common.GridClients;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.controller.properties.PropertiesEvaluator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.Properties;
import java.util.Map;


public class PropertiesResource { // extends ResourceSupport {

    private ProfileResource profileResource;

    public PropertiesResource(ProfileResource profileResource) {
        this.profileResource = profileResource;
    }

    @GET
    @Produces("text/plain")
    public String getPropertiesText() {
        StringBuilder buffer = new StringBuilder();
        Properties properties = getProperties();
        for (Map.Entry entry : properties.entrySet()) {
            buffer.append(entry.getKey());
            buffer.append(" = ");
            Object value = entry.getValue();
            if (value instanceof Number) {
                buffer.append(value);
            }
            else {
                buffer.append('\"');
                buffer.append(value);
                buffer.append('\"');
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }

    public Properties getProperties() {
        ProfileDetails profile = profileResource.getProfileDetails();
        GridController controller = profileResource.getController();
        PropertiesEvaluator propertiesEvaluator = profileResource.getPropertiesEvaluator();

        List<FeatureDetails> features = GridClients.getFeatureDetails(controller, profile);
        return propertiesEvaluator.evaluateProperties(features);
    }

}