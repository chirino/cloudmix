/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;



/**
 * Some helper methods for working with {@link org.fusesource.cloudmix.common.GridClient} instances
 *
 * @version $Revision: 1.1 $
 */
public final class GridClients {
    private GridClients() {
        //utility class
    }

    /**
     * Returns the agents currently assigned to the given feature
     */
    public static List<AgentDetails> getAgentDetailsAssignedToFeature(GridClient gridClient, 
                                                                      String featureId) {

        List<String> agentIds = gridClient.getAgentsAssignedToFeature(featureId);
        List<AgentDetails> answer = new ArrayList<AgentDetails>();
        for (String agentId : agentIds) {
            AgentDetails agentDetails = gridClient.getAgentDetails(agentId);
            if (agentDetails != null) {
                answer.add(agentDetails);
            }
        }
        return answer;
    }


    /**
     * Returns the feature details for the given profile
     */
    public static List<FeatureDetails> getFeatureDetails(GridClient gridClient,
                                                         ProfileDetails profileDetails) {
        List<FeatureDetails> answer = new ArrayList<FeatureDetails>();
        if (profileDetails != null) {
            List<Dependency> list = profileDetails.getFeatures();
            if (list != null) {
                for (Dependency dependency : list) {
                    String featureId = dependency.getFeatureId();
                    FeatureDetails feature = gridClient.getFeature(featureId);
                    if (feature != null) {
                        answer.add(feature);
                    }
                }
            }
        }
        return answer;
    }
}
