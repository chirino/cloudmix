/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.resources;

import java.lang.reflect.Method;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.AgentDetails;

public class AgentResourceTest extends TestCase {
    
    public void testAnnotations() throws Exception {
        Class<AgentResource> cls = AgentResource.class;

        Method get = cls.getDeclaredMethod("get");
        assertNotNull(get.getAnnotation(GET.class));
        
        Method update = cls.getDeclaredMethod("update", AgentDetails.class);
        assertNotNull(update.getAnnotation(PUT.class));
        
        Method delete = cls.getDeclaredMethod("delete");
        assertNotNull(delete.getAnnotation(DELETE.class));
        
        Method history = cls.getDeclaredMethod("history", Request.class);
        assertNotNull(history.getAnnotation(GET.class));
        assertNotNull(history.getAnnotation(Path.class));
        assertEquals(1, history.getParameterAnnotations().length);
        assertEquals(1, history.getParameterAnnotations()[0].length);
        assertEquals(Context.class, history.getParameterAnnotations()[0][0].annotationType());
    }
    
    public void testUpdate() {
        AgentDetails ad = new AgentDetails();
        GridController gc = EasyMock.createMock(GridController.class);
        gc.updateAgentDetails("a1", ad);
        EasyMock.replay(gc);
        
        AgentResource ar = new AgentResource(gc, "a1");
        ar.update(ad);
        EasyMock.verify(gc);
    }
    /*
    
    public void testHistory() {
        AgentDetails ad = new AgentDetails();
        GridController gc = EasyMock.createMock(GridController.class);
        gc.updateAgentDetails("a1", ad);
        EasyMock.replay(gc);
        
        AgentResource ar = new AgentResource(new MockGridController(), "a1");
        
        Request request = Client.create().resource("").;
        ar.history(request);
        EasyMock.verify(gc);
    }
    
    
    private class MockGridController implements GridController {

        Map<String, FeatureController> fcs = new HashMap<String, FeatureController>();
        Map<String, Integer> featureInstancesCount = new HashMap<String, Integer>();
        
        public void addFeature(FeatureDetails featureDetails) { }
        public FeatureController getFeatureController(Dependency dependency) { return null; }
        public FeatureController getFeatureController(String featureId) { return null; }
        public String addAgentDetails(AgentDetails agentDetails) { return null; }
        public void addAgentToFeature(String featureId,
                                      String agentId,
                                      Map<String, String> cfgOverridesProps) { }
        public void addProfile(ProfileDetails profileDetails) { }
        public int evaluateIntegerExpression(String minimumInstances) {
            return minimumInstances == null  ? 0 : Integer.parseInt(minimumInstances);
        }
        public AgentDetails getAgentDetails(String agentId) { return null; }
        public ProvisioningHistory getAgentHistory(String agentId) { return null; }
        public long getAgentTimeout() { return 0; }
        public List<String> getAgentsAssignedToFeature(String featureId) { return null; }
        public List<String> getAgentsAssignedToFeature(String featureId,
                                                       String profileId,
                                                       boolean onlyIfDeployed) { return null; }
        public Collection<AgentDetails> getAllAgentDetails() { return null; }
        public Collection<FeatureDetails> getFeatureDetails() { return null; }
        public FeatureDetails getFeatureDetails(String featureId) { return null; }
        public int getFeatureInstanceCount(String id, String profileId, boolean onlyIfDeployed) {
            return featureInstancesCount.get(id) == null  ? 0 : featureInstancesCount.get(id).intValue();
        }
        public Collection<ProfileDetails> getProfileDetails() { return null; }
        public ProfileDetails getProfileDetails(String profileId) { return null; }
        public void removeAgentDetails(String agentId) { }
        public void removeAgentFromFeature(String featureId, String agentId) { }
        public void removeFeature(String featureId) { }
        public void removeProfile(String profileId) { }
        public void updateAgentDetails(String agentId, AgentDetails agentDetails) { }
    }
    */
}
