/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_XML;



import com.sun.jersey.api.representation.Form;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.controller.properties.PropertiesEvaluator;


public class ProfileResource extends ResourceSupport {
    private static final transient Log LOG = LogFactory.getLog(ProfileResource.class);

    private final GridController controller;
    private final PropertiesEvaluator propertiesEvaluator;
    private final String profileId;

    public ProfileResource(GridController controller, PropertiesEvaluator propertiesEvaluator, String id) {
        this.controller = controller;
        this.propertiesEvaluator = propertiesEvaluator;
        this.profileId = id;
    }

    @Path("status")
    public ProfileStatusResource getStatus() {
        return new ProfileStatusResource(this);
    }

    @Path("properties")
    public PropertiesResource getProperties() {
        return new PropertiesResource(this);
    }

    @GET
    public ProfileDetails getProfileDetails() {
        return controller.getProfile(profileId);
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

    @POST
    @Consumes({
            TEXT_PLAIN, TEXT_HTML, TEXT_XML, APPLICATION_XML
    })
    public void post(@Context UriInfo uriInfo, @Context HttpHeaders headers, String body) {
        if (body != null && body.equalsIgnoreCase("kill")) {
            delete();
        } else {
            LOG.warn("Unknown status '" + body + "' sent to profileId " + profileId);
        }
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response post(@Context UriInfo uriInfo, @Context HttpHeaders headers, Form formData)
        throws URISyntaxException {
        LOG.info("<<<<<< received form: " + formData);

        String value = formData.getFirst("kill");
        System.out.println("Values: " + value);

        if (value != null) {
            post(uriInfo, headers, "kill");
        }
        return Response.seeOther(new URI("/profiles")).build();
    }

    public GridController getController() {
        return controller;
    }

    public PropertiesEvaluator getPropertiesEvaluator() {
        return propertiesEvaluator;
    }

    public String getProfileId() {
        return profileId;
    }
}
