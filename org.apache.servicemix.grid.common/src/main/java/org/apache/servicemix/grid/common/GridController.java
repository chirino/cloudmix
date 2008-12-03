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
package org.apache.servicemix.grid.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.servicemix.grid.common.controller.FeatureController;
import org.apache.servicemix.grid.common.dto.AgentDetails;
import org.apache.servicemix.grid.common.dto.Dependency;
import org.apache.servicemix.grid.common.dto.FeatureDetails;
import org.apache.servicemix.grid.common.dto.ProfileDetails;
import org.apache.servicemix.grid.common.dto.ProvisioningHistory;

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
