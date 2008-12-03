package org.apache.servicemix.grid.common.controller.constraints.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.servicemix.grid.common.controller.AgentController;
import org.apache.servicemix.grid.common.controller.FeatureController;

/**
 * filters an agent list based on the preferred host of a given feature 
 * 
 * Note that the checker better be used last 
 */
public class AgentPreferedHostChecker implements IAgentConstraintChecker {

    /**
     * returns only the agents on the preferred host as long as there is at least 1 such agent, otherwise 
     * returns all the candidates as they are all equally inadequate anyway.
     */
    public Collection<AgentController> applyConstraint(String profileId,
                                                       FeatureController fc,
                                                       Collection<AgentController> someCandidates) {
        if (someCandidates == null) {
            return new ArrayList<AgentController>(0);
        }
        
        if (profileId == null || fc == null || someCandidates.size() == 0) {
            return new ArrayList<AgentController>(someCandidates);
        }
        
        List<AgentController> acceptedCandidates = new ArrayList<AgentController>();
        for (AgentController ac : someCandidates) {
            if (fc.isAgentOnAPreferredMachine(ac)) {
                acceptedCandidates.add(ac);
            }
        }
        
        // if no agent match is on the preferred host then all agents are equally adequate...
        if (acceptedCandidates.size() == 0) {
            acceptedCandidates.addAll(someCandidates);
        }

        return acceptedCandidates;
    }

}
