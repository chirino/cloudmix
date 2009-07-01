/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;

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

    List<ProfileDetails> getProfiles() throws URISyntaxException;
    ProfileDetails getProfile(String id) throws URISyntaxException;

    void addProfile(ProfileDetails profile) throws URISyntaxException;

    /**
     * Typically only used in testing where we create and destroy a profile per integration test
     */
    void removeProfile(ProfileDetails profile) throws URISyntaxException;
    void removeProfile(String profileId) throws URISyntaxException;
}
