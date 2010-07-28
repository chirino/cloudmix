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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.controller.AgentController;
import org.fusesource.cloudmix.common.controller.FeatureController;

/**
 * filters an agent list based on the preferred host of a given feature
 * 
 * Note that the checker better be used last
 */
public class AgentPreferedHostChecker implements IAgentConstraintChecker {

    private static final transient Log LOG = LogFactory.getLog(AgentPreferedHostChecker.class);

    /**
     * returns only the agents on the preferred host as long as there is at
     * least 1 such agent, otherwise returns all the candidates as they are all
     * equally inadequate anyway.
     */
    public Collection<AgentController> applyConstraint(String profileId, FeatureController fc, Collection<AgentController> someCandidates) {

        if (LOG.isDebugEnabled())
            LOG.debug("filtering on preferred host: " + fc.getDetails().getId() + someCandidates);
        List<AgentController> acceptedCandidates = null;

        if (someCandidates == null) {
            acceptedCandidates = new ArrayList<AgentController>(0);
        } else if (profileId == null || fc == null || someCandidates.size() == 0) {
            acceptedCandidates = new ArrayList<AgentController>(someCandidates);
        } else {
            acceptedCandidates = new ArrayList<AgentController>();
            for (AgentController ac : someCandidates) {
                if (fc.isAgentOnAPreferredMachine(ac)) {
                    acceptedCandidates.add(ac);
                }
            }
            
            // if no agent match is on the preferred host then all agents are equally adequate...
            if (acceptedCandidates.size() == 0) {
                acceptedCandidates.addAll(someCandidates);
            }
        }
        
        if(LOG.isDebugEnabled())
            LOG.debug("filtered on preferred host: " + fc.getDetails().getId() + someCandidates);

        return acceptedCandidates;
    }

}
