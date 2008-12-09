/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.controller.constraints.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fusesource.cloudmix.common.controller.AgentController;
import org.fusesource.cloudmix.common.controller.FeatureController;

/**
 * checks that no "owning" feature is already installed on a given agents
 */
public class AgentOwnedByFeatureChecker implements IAgentConstraintChecker {

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
            if (!ac.isLockedByOwningFeature()) {
                acceptedCandidates.add(ac);
            }
        }
        return acceptedCandidates;
    }

}
