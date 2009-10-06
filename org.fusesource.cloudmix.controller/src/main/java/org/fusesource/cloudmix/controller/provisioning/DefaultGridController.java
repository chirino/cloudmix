/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.provisioning;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.jersey.api.NotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.CloudmixHelper;
import org.fusesource.cloudmix.common.ControllerDataProvider;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.URIs;
import org.fusesource.cloudmix.common.controller.AgentController;
import org.fusesource.cloudmix.common.controller.FeatureController;
import org.fusesource.cloudmix.common.controller.ProfileController;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProcessList;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProfileStatus;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;
import org.fusesource.cloudmix.common.util.ObjectHelper;
import org.mortbay.util.UrlEncoded;

/**
 * @version $Revision$
 */
public class DefaultGridController implements GridController, GridClient {
    private static final transient Log LOG = LogFactory.getLog(DefaultGridController.class);
    private AtomicLong counter = new AtomicLong(0);
    private long agentTimeout = 3000L;    
    private ControllerDataProvider dataProvider = new SimpleControllerDataProvider(this);    

    @Override
    public String toString() {
        return "DefaultGridController[agentTimout: " + agentTimeout + "]";
    }

    public URI getRootUri() {
        return URIs.createURI(CloudmixHelper.getDefaultRootUrl());
    }

    public String addAgentDetails(AgentDetails details) {
        if (details.getId() == null || details.getId().equals("")) {
            details.setId(constructAgentId(details));
        }

        AgentController agent = new AgentController(this, details);
        ProvisioningHistory history = new ProvisioningHistory();

        // lets create a history from the current process lists...
        ProcessList processList = details.getProcesses();
        if (processList != null) {
            processList.populateHistory(history);    
        }

        agent.setHistory(history);
        // lets set the features set too
        history.populate(agent.getFeatures());

        AgentController rc = dataProvider.addAgent(details.getId(), agent);
        if (rc == null) {
            rc = agent;
        }
        rc.markActive();

        return rc.getDetails().getId();
    }
    
    public void updateAgentDetails(String agentId, AgentDetails agentDetails) {
        AgentController ac = dataProvider.getAgent(agentId);        
        if (ac == null) {
            LOG.warn("Could not find agent with ID " + agentId);
            return;
        }
        agentDetails.setId(agentId); // make sure the ID's match up
        ac.setDetails(agentDetails);
        dataProvider.updateAgent(agentId, ac);
    }

    private String constructAgentId(AgentDetails details) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(details.getProfile());
        sb.append('_');        
        
        String hostname = details.getHostname();
        int idx = hostname.indexOf('.');
        if (idx > 0) {
            hostname = hostname.substring(0, idx);
        }
        sb.append(hostname);       
        sb.append('_');   

        sb.append(details.getContainerType());
        sb.append('_');        
        
        sb.append(counter.incrementAndGet());
        String unEncId = sb.toString(); 
        
        try {
            return URLEncoder.encode(unEncId, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            return unEncId;
        }
    }

    public void removeAgentDetails(String agentId) {
        AgentController remove = dataProvider.removeAgent(agentId);
        if (remove == null) {
            throw new NotFoundException("Agent '" + agentId + "' does not exist");
        }
    }

    public Collection<AgentDetails> getAllAgentDetails() {
        List<AgentDetails> rc = new ArrayList<AgentDetails>();

        // Return all the agents that have not timed out.
        for (AgentController agent : agentTrackers()) {
            rc.add(agent.getDetails());
        }
        return rc;
    }

    public AgentDetails getAgentDetails(String agentId) {
        AgentController agent = agentTracker(agentId);
        if (agent == null) {
            throw new NotFoundException("Agent '" + agentId + "' does not exist");
        }
        return agent.getDetails();
    }

    public ProvisioningHistory getAgentHistory(String agentId) {
        AgentController agent = agentTracker(agentId);
        if (agent == null) {
            throw new NotFoundException("Agent '" + agentId + "' does not exist");
        }
        agent.markActive();
        return agent.getHistory();
    }

    public FeatureDetails getFeature(String featureId) {
        FeatureController rc = dataProvider.getFeature(featureId);
        if (rc == null) {
            throw new NotFoundException("Feature '" + featureId + "' does not exist");
        }
        return rc.getDetails();
    }

    public Collection<FeatureDetails> getFeatureDetails() {
        List<FeatureDetails> answer = new ArrayList<FeatureDetails>();        
        Collection<FeatureController> featureControllers = dataProvider.getFeatures();
        for (FeatureController featureController : featureControllers) {
            answer.add(featureController.getDetails());
        }
        return answer;
    }

    public void addFeature(FeatureDetails featureDetails) {        
        dataProvider.addFeature(featureDetails.getId(), new FeatureController(this, featureDetails));
    }

    public void removeFeature(String featureId) {
        FeatureController remove = dataProvider.removeFeature(featureId);
        if (remove == null) {
            throw new NotFoundException("Feature '" + featureId + "' does not exist");
        }
    }

    public void addAgentToFeature(String featureId, String agentId, Map<String, String> cfgOverridesProps) {
        AgentController agent = agentTracker(agentId);
        if (agent == null) {
            throw new NotFoundException("Agent '" + agentId + "' does not exist");
        }
        addAgentToFeature(agent, featureId, cfgOverridesProps);
    }

    protected List<ProvisioningAction> addAgentToFeature(AgentController agent,
                                                         String featureId,
                                                         Map<String, String> cfgOverridesProps) {
        
        if (featureId == null) {
            throw new IllegalArgumentException("featuredId should not be null");
        }
        if (agent.getFeatures().add(featureId)) {
            // If the agent did not have this added yet..
            List<ProvisioningAction> actions = getInstallActionsFor(agent, featureId, cfgOverridesProps);
            for (ProvisioningAction action : actions) {
                agent.getHistory().addAction(action);
            }
            return actions;
        } else {
            return Collections.emptyList();
        }
    }

    public void removeAgentFromFeature(String featureId, String agentId) {
        AgentController agent = agentTracker(agentId);
        if (agent == null) {
            throw new NotFoundException("Agent '" + agentId + "' does not exist");
        }
        
        // There is a chance that the feature cannot be found
        // in which case there is no issue with removing it :)
        // FeatureController feature = dataProvider.getFeature(featureId);
        // if (feature == null) {
        //     throw new NotFoundException("Feature '" + featureId + "' does not exist");
        // }        
        
        List<ProvisioningAction> actions = null;
        if (agent.getFeatures().remove(featureId)) {
            // If the agent did not have this removed yet..
            actions = getUninstallActionsFor(agent, featureId);
            for (ProvisioningAction action : actions) {
                agent.getHistory().addAction(action);
            }
        }
        
    }

    public List<String> getAgentsAssignedToFeature(String featureId) {
        return getAgentsAssignedToFeature(featureId, null, false);
    }
    
    public List<String> getAgentsAssignedToFeature(String featureId, 
                                                   String profileId,
                                                   boolean onlyIfDeployed) {
        List<String> rc = new ArrayList<String>();

        // Return all the agents that have not timed out and that have the feature assigned.
        for (AgentController agent : agentTrackers()) {
            // TODO check for wildcard profile!!!
            //if (profileId != null && !profileId.equals(agent.getDetails().getProfile())) {
            if (profileId != null && !agent.getDetails().matchesProfile(profileId)) {
                continue;
            }                
            
            if (agent.getFeatures().contains(featureId)) {
                boolean addFeature = false;
                if (onlyIfDeployed) {
                    // Restrict to deployed features reported back by the agent
                    Set<String> installedFeatures = agent.getDetails().getCurrentFeatures();
                    if (installedFeatures != null) {
                        for (String f : installedFeatures) {
                            if (f.equals(featureId)) {
                                addFeature = true;
                            }
                        }
                    }
                } else {
                    addFeature = true;
                }
                
                if (addFeature) {
                    rc.add(agent.getDetails().getId());
                }
            }
        }

        return rc;
    }
    
    public void addProfile(ProfileDetails profileDetails) {
        dataProvider.addProfile(profileDetails.getId(), new ProfileController(this, profileDetails));
    }

    public void removeProfile(ProfileDetails profile) {
        String id = profile.getId();
        ObjectHelper.notNull(id, "profile.id");
        removeProfile(id);
    }

    public void removeProfile(String profileId) {
        ProfileController remove = dataProvider.removeProfile(profileId);
        if (remove == null) {
            throw new NotFoundException("Profile '" + profileId + "' does not exist");
        }

        // lets delete any features associated with this profile!
        deleteFeaturesForProfile(profileId);
    }

    /**
     * Deletes the features for a given profile.
     *
     * An implementation might have a more optimal way of implementing this than brute force
     * iterating through all features.
     */
    protected void deleteFeaturesForProfile(String profileId) {
        Collection<FeatureDetails> features = getFeatureDetails();
        for (FeatureDetails feature : features) {
            String ownerId = feature.getOwnedByProfileId();
            if (ownerId != null && ownerId.equals(profileId)) {
                removeFeature(feature.getId());
            }
        }
    }

    // TODO note the difference in APIs between this and getProfileDetails
    public List<ProfileDetails> getProfiles() {
        return new ArrayList<ProfileDetails>(getProfileDetails());
    }

    public Collection<ProfileDetails> getProfileDetails() {
        List<ProfileDetails> answer = new ArrayList<ProfileDetails>();        
        Collection<ProfileController> profileControllers = dataProvider.getProfiles();
        for (ProfileController profileController : profileControllers) {
            answer.add(profileController.getDetails());
        }
        return answer;
    }

    public ProfileDetails getProfile(String profileId) {
        ProfileController rc = getProfileController(profileId);
        if (rc == null) {
            throw new NotFoundException("Profile '" + profileId + "' does not exist");
        }
        return rc.getDetails();
    }

    public ProfileStatus getProfileStatus(String profileId) {
        ProfileController rc = getProfileController(profileId);
        if (rc == null) {
            throw new NotFoundException("Profile '" + profileId + "' does not exist");
        }
        return rc.getStatus();
    }

    protected ProfileController getProfileController(String profileId) {
        ProfileController answer = dataProvider.getProfile(profileId);
        if (answer == null) {
            String encodedId = UrlEncoded.encodeString(profileId);
            answer = dataProvider.getProfile(encodedId);
        }
        return answer;
    }

    public FeatureController getFeatureController(String featureId) {
        return dataProvider.getFeature(featureId);
    }

    public FeatureController featureController(FeatureDetails featureDetails) {
        return getFeatureController(featureDetails.getId());
    }

    public FeatureController getFeatureController(Dependency featureDetails) {
        return getFeatureController(featureDetails.getFeatureId());
    }

    public int getFeatureInstanceCount(String featureId, String profileId, boolean onlyIfDeployed) {
        List<String> agents = getAgentsAssignedToFeature(featureId, profileId, onlyIfDeployed);
        return agents.size();
    }
    
    // Properties
    //-------------------------------------------------------------------------
    public long getAgentTimeout() {
        return agentTimeout;
    }

    public void setAgentTimeout(long machineTimeout) {
        this.agentTimeout = machineTimeout;
    }
    
    public ControllerDataProvider getDataProvider() {
        return dataProvider;
    }
    
    public void setDataProvider(ControllerDataProvider dp) {
        dataProvider = dp;
        dataProvider.setGrid(this);
    }    

    // GridClient API
    //-------------------------------------------------------------------------
    public ProvisioningHistory pollAgentHistory(String agentId) {
        // we are local so no need to poll
        return getAgentHistory(agentId);
    }

    public List<FeatureDetails> getFeatures() {
        return new ArrayList<FeatureDetails>(getFeatureDetails());
    }

    public void removeFeature(FeatureDetails feature) {
        removeFeature(feature.getId());
    }
    
    
    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * This could get much more complicated.  Should it dive into dependencies? The more work we do here,
     * the dumber the agent can stay.
     *
     * @param agent
     * @return
     */
    protected List<ProvisioningAction> getInstallActionsFor(AgentController agent,
                                                            String featureId,
                                                            Map<String, String> cfgOverridesProps) {
        List<ProvisioningAction> rc = new ArrayList<ProvisioningAction>();

        ProvisioningAction action = new ProvisioningAction();
        action.setId(agent.getNextHistoryId());
        action.setCommand("install");
        action.setFeature(featureId);
        
        FeatureController fc = dataProvider.getFeature(featureId);
        if (fc == null) {
            fc = dataProvider.getFeature(encodeURL(featureId));
        }
        action.setResource(fc.getResource());
        
        if (cfgOverridesProps != null && cfgOverridesProps.size() > 0) {
            for (String key : cfgOverridesProps.keySet()) {
                action.addCfgOverride(new ConfigurationUpdate(key, cfgOverridesProps.get(key)));
            }
        }
        
        rc.add(action);

        return rc;
    }

    protected List<ProvisioningAction> getUninstallActionsFor(AgentController agent, String featureId) {
        List<ProvisioningAction> rc = new ArrayList<ProvisioningAction>();

        ProvisioningAction action = new ProvisioningAction();
        action.setId(agent.getNextHistoryId());
        action.setCommand("uninstall");
        action.setFeature(featureId);
        // this should not be needed for uninstallation
        // action.setResource(dataProvider.getFeature(featureId).getResource());
        rc.add(action);

        return rc;
    }

    protected AgentController agentTracker(String agentId) {
        return dataProvider.getAgent(agentId);
    }

    protected Collection<AgentController> agentTrackers() {
        Collection<AgentController> agents = dataProvider.getAgents();

        List<AgentController> l = new ArrayList<AgentController>(agents.size());
        for (AgentController agent : agents) {
            if (agent.isActive(System.currentTimeMillis())) {
                l.add(agent);
            }
        }
        //LOG.debug("Default GridController, live agents available: " + l.size());
        return l;
    }
    
    protected Collection<AgentController> agentTrackers(String profileID) {
        Collection<AgentController> agents = agentTrackers();

        List<AgentController> l = new ArrayList<AgentController>(agents.size());
        for (AgentController agent : agents) {
            if (profileID.equals(agent.getDetails().getProfile())) {
                l.add(agent);
            }
        }
        return l;
    }
     
    protected Collection<FeatureController> featureControllers() {
        return dataProvider.getFeatures();
    }
    
    protected Collection<ProfileController> profileControllers() {
        return dataProvider.getProfiles();
    }
    
    protected static String encodeURL(String name) {
        try {
            return URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Problem encoding URL", e);
            return name;
        }
    }
    
    protected static String decodeURL(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Problem decoding URL", e);
            return url;
        }
    }


}

