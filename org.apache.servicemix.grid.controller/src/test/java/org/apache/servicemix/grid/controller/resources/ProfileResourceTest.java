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

import java.lang.reflect.Method;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.easymock.EasyMock;

public class ProfileResourceTest extends TestCase {
    public void testAnnotations() throws Exception {
        Class<ProfileResource> cls = ProfileResource.class;
       
        Method getProfileDetails = cls.getDeclaredMethod("getProfileDetails");
        assertNotNull(getProfileDetails.getAnnotation(GET.class));
        
        Method addProfileDetails = cls.getDeclaredMethod("addProfileDetails", ProfileDetails.class);
        assertNotNull(addProfileDetails.getAnnotation(PUT.class));
        
        Method delete = cls.getDeclaredMethod("delete");
        assertNotNull(delete.getAnnotation(DELETE.class));
    }
    
    public void testGetProfileDetails() throws Exception {
        GridController gc = EasyMock.createMock(GridController.class);
        EasyMock.expect(gc.getProfileDetails("aaa")).andReturn(new ProfileDetails("aaa"));
        EasyMock.replay(gc);
        
        ProfileResource pr = new ProfileResource(gc, "aaa");
        
        assertEquals("aaa", pr.profileId);
        assertSame(gc, pr.controller);
        
        ProfileDetails pd = pr.getProfileDetails();
        assertEquals("aaa", pd.getId());
        
        EasyMock.verify(gc);
    }
    
    public void testAddProfileDetails() throws Exception {
        ProfileDetails pd = new ProfileDetails();
        GridController gc = EasyMock.createMock(GridController.class);
        gc.addProfile(pd);
        EasyMock.replay(gc);
        
        ProfileResource pr = new ProfileResource(gc, "123");
        pr.addProfileDetails(pd);
        assertEquals("123", pd.getId());
        
        EasyMock.verify(gc);
    }
    
    public void testDelete() throws Exception {
        GridController gc = EasyMock.createMock(GridController.class);
        gc.removeProfile("123");
        EasyMock.replay(gc);
        
        ProfileResource pr = new ProfileResource(gc, "123");
        pr.delete();
        
        EasyMock.verify(gc);         
    }
}
