/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.AgentPoller;
import org.fusesource.cloudmix.common.controller.AgentController;
import org.fusesource.cloudmix.common.controller.FeatureController;
import org.fusesource.cloudmix.common.controller.ProfileController;
import org.fusesource.cloudmix.common.dto.AgentCfgUpdate;
import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;
import org.fusesource.cloudmix.common.dto.Constants;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * @version $Revision$
 */
public class ProvisioningGridController extends DefaultGridController implements InitializingBean,
        DisposableBean,
        Callable<Object> {

    private static final transient Log LOG = LogFactory.getLog(ProvisioningGridController.class);

    AgentPoller poller;
    private long startupProvisioningDelay = 5000L;

    @Override
    public String toString() {
        return "ProvisioningGridController[agentTimout: " + getAgentTimeout() + "]";
    }

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

            List<FeatureController> deployableFeatures = profile.getDeployableFeatures();
            for (FeatureController fc : deployableFeatures) {
                String featureId = decodeURL(fc.getId());
                Collection<AgentController> agentTrackers = agentTrackers();
                AgentController agent = fc.selectAgentForDeployment(profileID, agentTrackers);


                if (agent == null) {
                    LOG.debug("for feature: " + featureId + " no agent selected from possible agents " 
                              + agentTrackers.size());

                } else {
                    LOG.debug("for feature: " + featureId + " found adequate agent: "
                            + agent.getDetails());

                    Map<String, String> cfgOverridesProps = getFeatureConfigurationOverrides(profile,
                            featureId);
                    List<ProvisioningAction> list = addAgentToFeature(agent, fc.getId(), cfgOverridesProps);
                    answer.addAll(list);
                }

            }

            // cleaning up redundant features from agents that still have a profile assigned
            // (either because we switched profile or because the profile was updated)
            List<String> featureIds = new ArrayList<String>();

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
            boolean agentProfileGone = assignedProfile == null 
                || hasProfileGone(assignedProfile) 
                && !assignedProfile.equals(Constants.WILDCARD_PROFILE_NAME);
            if (ac.getFeatures() != null && !ac.getFeatures().isEmpty()) {
                String agentId = ac.getDetails().getId();
                String[] featuresCopy = ac.getFeatures().toArray(new String[ac.getFeatures().size()]);
                if (agentProfileGone) {
                    for (String fid : featuresCopy) {
                        removeAgentFromFeature(fid, agentId);
                    }
                } else {
                    for (String fid : featuresCopy) {
                        FeatureController featureController = getFeatureController(fid);
                        // if the feature controller has gone, then the feature has gone
                        // either by being deleted itself, or due to the profile going
                        boolean deleteFeature = true;
                        if (featureController != null) {
                            deleteFeature = false;
                            FeatureDetails details = featureController.getDetails();
                            if (details != null
                                && details.getOwnedByProfileId() != null
                                && hasProfileGone(details.getOwnedByProfileId())) {
                                deleteFeature = true;
                            }
                        }
                        if (deleteFeature) {
                            removeAgentFromFeature(fid, agentId);
                        }
                    }

                }
            }
        }

        return answer;
    }

    /**
     * Returns true if the given profile ID has been destroyed
     */
    protected boolean hasProfileGone(String assignedProfile) {
        return getProfileController(assignedProfile) == null;
    }

    private Map<String, String> getFeatureConfigurationOverrides(ProfileController profile,
                                                                 String featureId) {
        Map<String, String> cfgOverridesProps = null;

        LOG.debug("getFeatureConfigurationOverrides, relevant feature id: " + featureId);
        LOG.debug("getFeatureConfigurationOverrides, features: " + profile.getDetails().getFeatures().size());

        for (Dependency dep : profile.getDetails().getFeatures()) {
            LOG.debug("getFeatureConfigurationOverrides, dep id: " + dep.getFeatureId());
            LOG.debug("getFeatureConfigurationOverrides, dep overrides: "
                    + (dep.getCfgUpdates() == null ? 0 : dep.getCfgUpdates().size()));

            if (featureId.equals(decodeURL(dep.getFeatureId())) && dep.getCfgUpdates() != null) {

                cfgOverridesProps = new HashMap<String, String>(dep.getCfgUpdates().size());
                for (ConfigurationUpdate cfgUpdate : dep.getCfgUpdates()) {
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
