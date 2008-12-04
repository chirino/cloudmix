/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.resources;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.GridController;
import org.easymock.EasyMock;
import org.fusesource.cloudmix.controller.resources.ProfilesResource;
import org.fusesource.cloudmix.controller.resources.ProfileResource;

public class ProfilesResourceTest extends TestCase {
    public void testAnnotations() throws Exception {
        Class<ProfilesResource> cls = ProfilesResource.class;
        Path ann = cls.getAnnotation(Path.class);
        assertEquals("/profiles", ann.value());
        
        Method getProfiles = cls.getDeclaredMethod("getProfiles");
        assertNotNull(getProfiles.getAnnotation(GET.class));
        assertEquals("application/xml", getProfiles.getAnnotation(Produces.class).value()[0]);
        
        Method getProfile = cls.getDeclaredMethod("getProfile", String.class);
        assertEquals("{id}", getProfile.getAnnotation(Path.class).value());
        Annotation[][] parameterAnnotations = getProfile.getParameterAnnotations();
        PathParam pp = (PathParam) parameterAnnotations[0][0];
        assertEquals("id", pp.value());
    }
    
    public void testControllerAccessor() {
        ProfilesResource pr = new ProfilesResource();
        GridController gc = EasyMock.createNiceMock(GridController.class);
        assertNotSame("Precondition failed", pr.controller, gc);
        pr.setController(gc);
        assertSame(gc, pr.controller);
    }
    
    public void testGetProfile() {
        ProfilesResource pr = new ProfilesResource();
        GridController gc = EasyMock.createNiceMock(GridController.class);
        pr.setController(gc);
        
        ProfileResource res = pr.getProfile("123");
        assertEquals("123", res.profileId);
        assertSame(gc, res.controller);
    }

}
