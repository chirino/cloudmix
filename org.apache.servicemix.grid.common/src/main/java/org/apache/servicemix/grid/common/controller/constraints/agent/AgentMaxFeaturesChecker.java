package org.apache.servicemix.grid.common.controller.constraints.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.servicemix.grid.common.controller.AgentController;
import org.apache.servicemix.grid.common.controller.FeatureController;

/**
 * checks that a given agents haven't reached their maximum number of features instances yet
 */
public class AgentMaxFeaturesChecker implements IAgentConstraintChecker {

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
            if (!ac.hasReachedMaxNumberOfFeatureAllowed()) {
                acceptedCandidates.add(ac);
            }
        }
        return acceptedCandidates;
    }

}
