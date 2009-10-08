/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common.controller.constraints.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.controller.AgentController;
import org.fusesource.cloudmix.common.controller.FeatureController;

/**
 * AgentContainerTypeChecker
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class AgentContainerTypeChecker implements IAgentConstraintChecker {

    private static final transient Log LOG = LogFactory.getLog(AgentContainerTypeChecker.class);

    public Collection<AgentController> applyConstraint(String profileId, FeatureController fc, Collection<AgentController> someCandidates) {
        if (LOG.isDebugEnabled())
            LOG.debug("filtering on container type...");
        Collection<AgentController> acceptedCandidates;

        if (someCandidates == null) {
            acceptedCandidates = new ArrayList<AgentController>(0);
        } else if (profileId == null || fc == null || someCandidates.size() == 0) {
            acceptedCandidates = someCandidates;
        } else if (fc.getDetails().getValidContainerTypes().isEmpty()) {
            //If the feature doesn't specify any valid container types
            //we're good.
            acceptedCandidates = new ArrayList<AgentController>(someCandidates);
        } else {
            acceptedCandidates = new ArrayList<AgentController>();
            for (AgentController ac : someCandidates) {
                String acType = ac.getDetails().getContainerType();
                if (acType == null || acType.trim().length() == 0) {
                    continue;
                }
                if (fc.getDetails().getValidContainerTypes().contains(ac.getDetails().getContainerType())) {
                    acceptedCandidates.add(ac);
                }
            }
        }

        if (LOG.isDebugEnabled())
            LOG.debug("filtered on container type: " + acceptedCandidates);
        return acceptedCandidates;
    }

}
