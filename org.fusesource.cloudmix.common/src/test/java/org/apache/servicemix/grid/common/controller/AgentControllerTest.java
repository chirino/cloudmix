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
