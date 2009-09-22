/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;


/**
 * Some helper methods for working with {@link org.fusesource.cloudmix.common.GridClient} instances
 *
 * @version $Revision: 1.1 $
 */
public final class GridClients {
    private GridClients() {
        //utility class
    }

    public static List<AgentDetails> getAgentDetailsAssignedToFeature(GridClient gridClient, 
                                                                      String featureId) 
        throws URISyntaxException {
        
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


}
