/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.agent.GridControllerClient;
import org.easymock.EasyMock;

public class GridControllerClientTest extends TestCase {
    public void testAddNoProfiles() throws Exception {
        GridControllerClient gcc = new GridControllerClient();
        GridClient gc = EasyMock.createMock(GridClient.class);
        // none of these as we dont expect these calls: gc.addProfile(xxx);
        EasyMock.replay(gc);
        
        gcc.setClient(gc);
        assertSame(gc, gcc.getClient());
        
        gcc.addProfiles();
        EasyMock.verify(gc);
    }

    public void testAddProfiles() throws Exception {
        ProfileDetails pd1 = new ProfileDetails("TESTING");
        ProfileDetails pd2 = new ProfileDetails("production");
        
        GridControllerClient gcc = new GridControllerClient();
        GridClient gc = EasyMock.createMock(GridClient.class);
        gc.addProfile(pd1);
        gc.addProfile(pd2);
        EasyMock.replay(gc);
                
        gcc.setClient(gc);
        gcc.addProfiles(pd1, pd2);
        EasyMock.verify(gc);        
    }
}
