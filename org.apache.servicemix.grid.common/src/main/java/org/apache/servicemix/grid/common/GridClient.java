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

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.servicemix.grid.common.dto.AgentDetails;
import org.apache.servicemix.grid.common.dto.FeatureDetails;
import org.apache.servicemix.grid.common.dto.ProfileDetails;
import org.apache.servicemix.grid.common.dto.ProvisioningHistory;

/**
 * @version $Revision$
 */
public interface GridClient {
    String addAgentDetails(AgentDetails agentDetails) throws URISyntaxException;

    AgentDetails getAgentDetails(String agentId) throws URISyntaxException;

    Collection<AgentDetails> getAllAgentDetails() throws URISyntaxException;

    void removeAgentDetails(String agentId) throws URISyntaxException;
    
    void updateAgentDetails(String agentId, AgentDetails agentDetails) throws URISyntaxException;

    ProvisioningHistory getAgentHistory(String agentId) throws URISyntaxException;

    ProvisioningHistory pollAgentHistory(String agentId) throws URISyntaxException;

    List<FeatureDetails> getFeatures() throws URISyntaxException;

    void addFeature(FeatureDetails feature) throws URISyntaxException;

    void removeFeature(String id) throws URISyntaxException;

    void removeFeature(FeatureDetails feature) throws URISyntaxException;

    void addAgentToFeature(String featureId,
                           String agentId,
                           Map<String, String> cfgOverridesProps) throws URISyntaxException;

    void removeAgentFromFeature(String featureId, String agentId) throws URISyntaxException;

    List<String> getAgentsAssignedToFeature(String id) throws URISyntaxException;
    
    void addProfile(ProfileDetails profile) throws URISyntaxException;
}
