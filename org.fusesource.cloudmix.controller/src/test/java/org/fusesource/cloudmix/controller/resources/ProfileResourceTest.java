/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.resources;

import java.lang.reflect.Method;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.easymock.EasyMock;
import org.fusesource.cloudmix.controller.resources.ProfileResource;

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
