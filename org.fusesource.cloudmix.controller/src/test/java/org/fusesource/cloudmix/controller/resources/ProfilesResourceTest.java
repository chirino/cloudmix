/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;


import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.fusesource.cloudmix.common.GridController;

public class ProfilesResourceTest extends TestCase {
    public void testControllerAccessor() {
        ProfilesResource pr = new ProfilesResource();
        GridController gc = EasyMock.createNiceMock(GridController.class);
        assertNotSame("Precondition failed", pr.getController(), gc);
        pr.setController(gc);
        assertSame(gc, pr.getController());
    }
    
    public void testGetProfile() {
        ProfilesResource pr = new ProfilesResource();
        GridController gc = EasyMock.createNiceMock(GridController.class);
        pr.setController(gc);
        
        ProfileResource res = pr.getProfile("123");
        assertEquals("123", res.getProfileId());
        assertSame(gc, res.getController());
    }

}
