/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common;

import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProfileStatus;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.net.URI;

/**
 * A client interface for working with the cloudmix grid.
 * <p/>
 * <ul>
 * <li>
 * a profile represents an environment (like development, testing, produciton) or can be
 * a specific distributed test case which is isolated from other test cases
 * </li>
 * <li>
 * a feature represents some code that is to be ran such as an executable jar, a war, an osgi bundle
 * </li>
 * <li>
 * an agent runs some features
 * </li>
 * </ul>
 *
 * @version $Revision$
 */
public interface GridClient {

    /**
     * Returns the root URI for accessing the controller
     */
    URI getRootUri();
    
    // Profiles
    //-------------------------------------------------------------------------

    List<ProfileDetails> getProfiles();

    ProfileDetails getProfile(String id);

    void addProfile(ProfileDetails profile);

    /**
     * Typically only used in testing where we create and destroy a profile per integration test
     */
    void removeProfile(ProfileDetails profile);

    void removeProfile(String profileId);

    ProfileStatus getProfileStatus(String id);


    // Agents
    //-------------------------------------------------------------------------

    String addAgentDetails(AgentDetails agentDetails);

    AgentDetails getAgentDetails(String agentId);

    Collection<AgentDetails> getAllAgentDetails();

    void removeAgentDetails(String agentId);

    void updateAgentDetails(String agentId, AgentDetails agentDetails);


    // Features
    //-------------------------------------------------------------------------

    List<FeatureDetails> getFeatures();

    FeatureDetails getFeature(String featureId);

    void addFeature(FeatureDetails feature);

    void removeFeature(String id);

    void removeFeature(FeatureDetails feature);


    // Provisitioning History
    //-------------------------------------------------------------------------

    /**
     * Returns the current agent history
     */
    ProvisioningHistory getAgentHistory(String agentId);


    /**
     * Polls the agent history to see if its changed since the last time
     * we looked - so could return null if nothing has changed since the last call
     *
     * @return the polling history if its changed or null
     */
    ProvisioningHistory pollAgentHistory(String agentId);



    // Helper
    //-------------------------------------------------------------------------

    void addAgentToFeature(String featureId,
                           String agentId,
                           Map<String, String> cfgOverridesProps);

    void removeAgentFromFeature(String featureId, String agentId);


    List<String> getAgentsAssignedToFeature(String id);

}
