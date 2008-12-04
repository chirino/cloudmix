/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.fusesource.cloudmix.common.controller.FeatureController;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;

/**
 * @version $Revision: 1.1 $
 */
public interface GridController {
    Collection<AgentDetails> getAllAgentDetails();
    AgentDetails getAgentDetails(String agentId);
    String addAgentDetails(AgentDetails agentDetails);
    void removeAgentDetails(String agentId);
    void updateAgentDetails(String agentId, AgentDetails agentDetails);
    ProvisioningHistory getAgentHistory(String agentId);

    void addFeature(FeatureDetails featureDetails);
    void removeFeature(String featureId);    
    List<String> getAgentsAssignedToFeature(String featureId);
    List<String> getAgentsAssignedToFeature(String featureId, String profileId, boolean onlyIfDeployed);
    void addAgentToFeature(String featureId, String agentId, Map<String, String> cfgOverridesProps);
    void removeAgentFromFeature(String featureId, String agentId); 

    Collection<FeatureDetails> getFeatureDetails();
    FeatureDetails getFeatureDetails(String featureId);    
    
    long getAgentTimeout();
    FeatureController getFeatureController(String featureId);
    FeatureController getFeatureController(Dependency dependency);
    int getFeatureInstanceCount(String id, String profileId, boolean onlyIfDeployed);
    
    void addProfile(ProfileDetails profileDetails);
    void removeProfile(String profileId);
    Collection<ProfileDetails> getProfileDetails();
    ProfileDetails getProfileDetails(String profileId);
    
}
