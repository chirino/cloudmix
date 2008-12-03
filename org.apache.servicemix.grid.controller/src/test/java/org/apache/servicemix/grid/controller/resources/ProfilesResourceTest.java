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
package org.apache.servicemix.grid.controller.resources;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.GridController;
import org.easymock.EasyMock;

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
