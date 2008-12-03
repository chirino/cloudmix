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

import org.apache.servicemix.grid.common.controller.AgentController;
import org.apache.servicemix.grid.common.controller.FeatureController;
import org.apache.servicemix.grid.common.controller.ProfileController;

/**
 * @version $Revision$
 */
public interface ControllerDataProvider {
    GridController getGrid();
    void setGrid(GridController grid);
    
    AgentController addAgent(String id, AgentController agent);
    AgentController removeAgent(String agentId);
    AgentController getAgent(String agentId);
    AgentController updateAgent(String id, AgentController agent);
    Collection<AgentController> getAgents();

    FeatureController addFeature(String id, FeatureController featureController);
    FeatureController removeFeature(String featureId);
    FeatureController getFeature(String featureId);
    Collection<FeatureController> getFeatures();
    
    ProfileController addProfile(String id, ProfileController profileController);
    ProfileController removeProfile(String profileId);
    ProfileController getProfile(String profileId);
    Collection<ProfileController> getProfiles();
}
