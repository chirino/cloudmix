/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common;

import java.util.Collection;

import org.fusesource.cloudmix.common.controller.AgentController;
import org.fusesource.cloudmix.common.controller.FeatureController;
import org.fusesource.cloudmix.common.controller.ProfileController;

/**
 * @version $Revision: 42104 $
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
