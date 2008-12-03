package org.apache.servicemix.grid.common.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicemix.grid.common.GridController;
import org.apache.servicemix.grid.common.dto.AgentDetails;
import org.apache.servicemix.grid.common.dto.Dependency;
import org.apache.servicemix.grid.common.dto.FeatureDetails;
import org.apache.servicemix.grid.common.dto.ProfileDetails;
import org.apache.servicemix.grid.common.dto.ProvisioningHistory;

class MockGridController implements GridController {

    Map<String, FeatureController> fcs = new HashMap<String, FeatureController>();
    Map<String, Integer> featureInstancesCount = new HashMap<String, Integer>();
    
    public void addFeature(FeatureDetails featureDetails) {
        fcs.put(featureDetails.getId(), new FeatureController(this, featureDetails));
    }
        
    public FeatureController getFeatureController(Dependency dependency) {
        return fcs.get(dependency.getFeatureId());
    }
    
    public FeatureController getFeatureController(String featureId) {
        return fcs.get(featureId);
    }
    
    public String addAgentDetails(AgentDetails agentDetails) { return null; }
    public void addAgentToFeature(String featureId, String agentId, Map<String, String> cfgOverridesProps) { }
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