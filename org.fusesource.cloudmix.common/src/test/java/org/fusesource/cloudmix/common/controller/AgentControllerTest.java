/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.controller;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;

public class AgentControllerTest extends TestCase {

    public void testProfileController() {
        AgentDetails details = new AgentDetails();
        
        AgentController pc = new AgentController(null, details);
        assertSame(details, pc.getDetails());
    }
    
    public void testIsOwned() {
        MockGridController cl = new MockGridController();
        AgentController ac = new AgentController(cl, new AgentDetails());
        
        assertFalse(ac.isLockedByOwningFeature());
        
        FeatureDetails fd = new FeatureDetails();
        fd.setMaximumInstances("1");
        fd.setId("f1");
        fd.setOwnsMachine(false);
        cl.addFeature(fd);
        
        ac.getFeatures().add("f1");
        assertFalse(ac.isLockedByOwningFeature());
        
        fd = new FeatureDetails();
        fd.setMaximumInstances("1");
        fd.setOwnsMachine(true);
        fd.setId("f2");
        cl.addFeature(fd);
        
        ac.getFeatures().add("f2");
        assertTrue(ac.isLockedByOwningFeature());
        
        fd = new FeatureDetails();
        fd.setMaximumInstances("1");
        fd.setOwnsMachine(false);
        fd.setId("f3");
        cl.addFeature(fd);
        
        ac.getFeatures().add("f3");
        assertTrue(ac.isLockedByOwningFeature());
     
        ac.getFeatures().remove("f2");
        assertFalse(ac.isLockedByOwningFeature());
    }

}
