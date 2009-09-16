/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;

public class FeatureControllerTest extends TestCase {
    

    public void testSelectAgentForDeploymentAgentEmtpy() {
        GridController cl = EasyMock.createMock(GridController.class);
        FeatureDetails fd = new FeatureDetails("f1");
        FeatureController fc = new FeatureController(cl, fd);
        assertNull(fc.selectAgentForDeployment("testing", Collections.<AgentController>emptyList()));
    }
    
    public void testSelectAgentForDeployment() {
        GridController cl = EasyMock.createMock(GridController.class);
        AgentDetails ad1 = new AgentDetails();
        // ad1.setProfile("default"); not needed, this is the default
        ad1.setHostname("host1");
        AgentController a1 = new AgentController(cl, ad1);
        
        AgentDetails ad2 = new AgentDetails();
        ad2.setProfile("production");
        ad2.setHostname("host1");
        AgentController a2 = new AgentController(cl, ad2);
        
        AgentDetails ad3 = new AgentDetails();
        ad3.setProfile("default");
        ad3.setHostname("host2");
        AgentController a3 = new AgentController(cl, ad3);
        
        FeatureDetails fd = new FeatureDetails("f1");
        FeatureController fc = new FeatureController(cl, fd);
        
        List<AgentController> agents = Arrays.asList(a1, a2, a3);

        assertSame(a1, fc.selectAgentForDeployment("default", agents));
        assertSame(a2, fc.selectAgentForDeployment("production", agents));
        
        fc.getDetails().setPreferredMachines(new HashSet<String>(Arrays.asList("host2")));
        assertSame(a3, fc.selectAgentForDeployment("default", agents));

        fc.getDetails().setPreferredMachines(new HashSet<String>(Arrays.asList("host3")));
        assertSame(a1, fc.selectAgentForDeployment("default", agents));
        
        fc.getDetails().setPreferredMachines(null);
        a1.getFeatures().add("f99");
        assertSame(a3, fc.selectAgentForDeployment("default", agents)); 
    }

    public void testValidatePackageType() {
        String[] warType = {"war"};
        String[] osgiType = {"osgi"};
        String[] mixedType = {"osgi", "ear"};
        String[] superMixedType = {"osgi", "ear", "tar"};
        
        GridController cl = EasyMock.createMock(GridController.class);
        AgentDetails ad1 = new AgentDetails();
        ad1.setSupportPackageTypes(superMixedType);
        AgentController a1 = new AgentController(cl, ad1);
        
        AgentDetails ad2 = new AgentDetails();        
        ad2.setSupportPackageTypes(warType);
        AgentController a2 = new AgentController(cl, ad2);
        
        AgentDetails ad3 = new AgentDetails();
        ad3.setSupportPackageTypes(osgiType);
        AgentController a3 = new AgentController(cl, ad3);
        
        FeatureDetails fd = new FeatureDetails("f1");
        FeatureController fc = new FeatureController(cl, fd);
        
        List<AgentController> agents = Arrays.asList(a1, a2, a3);

        // Normal behaviour
        assertSame(null, fc.selectAgentForDeployment("production", agents));
        assertSame(a1, fc.selectAgentForDeployment("default", agents));
        
        // Now make sure we stop using a1 when packages aren't compatible
        fd.setPackageTypes(warType);
        assertSame(a2, fc.selectAgentForDeployment("default", agents));
        
        fd.setPackageTypes(mixedType);
        assertSame(a1, fc.selectAgentForDeployment("default", agents));
    }
    
    public void testResourceAPI() {
        FeatureDetails fd = new FeatureDetails("f1");
        fd.setResource("http://localhost/123");
        FeatureController fc = new FeatureController(null, fd);
        assertEquals("http://localhost/123", fc.getResource());
    }
    
    public void testPreferredMachinesAPI() {
        FeatureDetails fd = new FeatureDetails("f1");
        Set<String> pm = new HashSet<String>(Arrays.asList("1", "2"));
        fd.setPreferredMachines(pm);
        FeatureController fc = new FeatureController(null, fd);
        assertEquals(pm, fc.getPreferredMachines());
    }
}
