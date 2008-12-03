package org.fusesource.cloudmix.common.controller.constraints.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.controller.AgentController;
import org.fusesource.cloudmix.common.controller.FeatureController;

/**
 * filters an agent list based their assigned feature
 */
public class AgentProfileChecker implements IAgentConstraintChecker {
    
    private static final transient Log LOG = LogFactory.getLog(AgentProfileChecker.class);

    public Collection<AgentController> applyConstraint(String profileId,
                                                       FeatureController fc,
                                                       Collection<AgentController> someCandidates) {
        LOG.debug("filtering on profile: >" + profileId + "<");

        if (someCandidates == null) {
            return new ArrayList<AgentController>(0);
        }
        
        if (profileId == null || fc == null || someCandidates.size() == 0) {
            return new ArrayList<AgentController>(someCandidates);
        }
        
        List<AgentController> acceptedCandidates = new ArrayList<AgentController>();
        for (AgentController ac : someCandidates) {
            if (profileId.equals(ac.getDetails().getProfile())) {
                acceptedCandidates.add(ac);
            }
        }
        return acceptedCandidates;
    }
    
    

}
