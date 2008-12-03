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
package org.apache.servicemix.grid.controller.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.grid.agent.AgentPoller;
import org.apache.servicemix.grid.common.controller.AgentController;
import org.apache.servicemix.grid.common.controller.FeatureController;
import org.apache.servicemix.grid.common.controller.ProfileController;
import org.apache.servicemix.grid.common.dto.AgentCfgUpdate;
import org.apache.servicemix.grid.common.dto.ConfigurationUpdate;
import org.apache.servicemix.grid.common.dto.Dependency;
import org.apache.servicemix.grid.common.dto.ProvisioningAction;
import org.apache.servicemix.grid.common.dto.ProvisioningHistory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @version $Revision: 1.1 $
 */
public class ProvisioningGridController extends DefaultGridController implements InitializingBean,
                                                                                 DisposableBean,
                                                                                 Callable<Object> {

    private static final transient Log LOG = LogFactory.getLog(ProvisioningGridController.class);

    AgentPoller poller;    
    private long startupProvisioningDelay = 5000L;

    public void afterPropertiesSet() throws Exception {
        poller = new AgentPoller(this);
        poller.setInitialPollingDelay(getStartupProvisioningDelay());
        poller.afterPropertiesSet();
    }

    public void destroy() throws Exception {
        if (poller != null) {
            poller.destroy();
        }
    }

    /**
     * Lets poll to see if there are any new features we can provision
     */
    public Object call() throws Exception {
        List<ProvisioningAction> answer = new ArrayList<ProvisioningAction>();
        
        // cleaning up all features from de-activated agents 
        for (AgentController ac : agentTrackers()) {
            if (ac.isDeActivated() && (ac.getFeatures() != null) && (ac.getFeatures().size() > 0)) {
                String agentId = ac.getDetails().getId();
                for (String fid : ac.getFeatures().toArray(new String[ac.getFeatures().size()])) {
                    removeAgentFromFeature(fid, agentId);
                    ProvisioningHistory history = ac.getHistory();
                    history.addCfgUpdate(new AgentCfgUpdate(AgentCfgUpdate.PROPERTY_AGENT_FORCE_REGISTER,
                                                            "true"));
                }
            }
        }
        
        for (ProfileController profile : profileControllers()) {
            String profileID = decodeURL(profile.getDetails().getId());
            
            // the profile was modified so re-deploy everything
            // TODO maybe something less drastic would do, like only uninstalling the features for which the
            // cfg overrides were modified
            if (profile.hasChanged()) {
                LOG.info("profile '"
                         + profile.getDetails().getId()
                         + "' was updated: initiating redeploy...");
                
                List<String> toRemove = new ArrayList<String>();
                for (Dependency dep : profile.getDetails().getFeatures()) {
                    if (dep.hasChanged()) {
                        toRemove.add(dep.getFeatureId());
                        dep.setChanged(false);
                    }
                }
                for (AgentController ac : agentTrackers(profileID)) {
                    for (String featureToRemove : toRemove) {
                        removeAgentFromFeature(featureToRemove, ac.getDetails().getId());
                    }
                }
                profile.setChanged(false);
            }
            
            for (FeatureController fc : profile.getDeployableFeatures()) {
                String featureId = decodeURL(fc.getId());
                AgentController agent = fc.selectAgentForDeployment(profileID, agentTrackers());
                
                
                if (agent == null) {
                    LOG.info("No Agent Selected.");

                } else {
                    LOG.info("ProvisioningGridController, found adequate agent: "
                              + agent.getDetails().getName());
                    
                    Map<String, String> cfgOverridesProps = getFeatureConfigurationOverrides(profile,
                                                                                             featureId);
                    List<ProvisioningAction> list = addAgentToFeature(agent, fc.getId(), cfgOverridesProps);
                    answer.addAll(list);
                }
                
            }

            // cleaning up redundant features from agents that still have a profile assigned
            // (either because we switched profile or because the profile was updated)
            List <String> featureIds = new ArrayList<String>();
                     
            for (Dependency featureDependency : profile.getDetails().getFeatures()) {
                featureIds.add(featureDependency.getFeatureId());
            }
            
            for (AgentController ac : agentTrackers(profileID)) {
                Set<String> featuresToRemove = new HashSet<String>(ac.getFeatures());
                featuresToRemove.removeAll(featureIds);
                
                for (String fid : featuresToRemove) {
                    removeAgentFromFeature(fid, ac.getDetails().getId());
                }
            }
        }
        
        // cleaning up all features from agents that that do not have a profile assigned anymore 
        // (... or only an unpublished one...) or
        for (AgentController ac : agentTrackers()) {
            String assignedProfile = ac.getDetails().getProfile();
            
            if (ac.getFeatures() != null 
                && ac.getFeatures().size() > 0
                && (assignedProfile == null || getProfileController(assignedProfile) == null)) {
                
                String agentId = ac.getDetails().getId();
                for (String fid : ac.getFeatures().toArray(new String[ac.getFeatures().size()])) {
                    removeAgentFromFeature(fid, agentId);
                }
            }
        }
        
        return answer;
    }

    private Map<String, String> getFeatureConfigurationOverrides(ProfileController profile,
                                                                 String featureId) {
        Map<String, String> cfgOverridesProps = null;
        
        LOG.info("getFeatureConfigurationOverrides, relevant feature id: " + featureId);
        LOG.info("getFeatureConfigurationOverrides, features: " + profile.getDetails().getFeatures().size());
        
        for (Dependency dep : profile.getDetails().getFeatures()) {
            LOG.info("getFeatureConfigurationOverrides, dep id: " + dep.getFeatureId());
            LOG.info("getFeatureConfigurationOverrides, dep overrides: "
                     + (dep.getCfgUpdates() == null ? 0  : dep.getCfgUpdates().size()));
            
            if (featureId.equals(decodeURL(dep.getFeatureId())) && dep.getCfgUpdates() != null) {
                
                cfgOverridesProps = new HashMap<String, String>(dep.getCfgUpdates().size());
                for (ConfigurationUpdate  cfgUpdate : dep.getCfgUpdates()) {
                    cfgOverridesProps.put(cfgUpdate.getProperty(), cfgUpdate.getValue());
                }
            }
        }
        return cfgOverridesProps;
    }
    
    // Properties
    //-------------------------------------------------------------------------

    public long getStartupProvisioningDelay() {
        return startupProvisioningDelay;
    }

    public void setStartupProvisioningDelay(long startupProvisioningDelay) {
        this.startupProvisioningDelay = startupProvisioningDelay;
    }
}
