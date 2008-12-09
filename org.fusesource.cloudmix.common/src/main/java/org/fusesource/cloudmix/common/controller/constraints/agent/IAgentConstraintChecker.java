/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.controller.constraints.agent;

import java.util.Collection;

import org.fusesource.cloudmix.common.controller.AgentController;
import org.fusesource.cloudmix.common.controller.FeatureController;

/**
 * Simple interface to implement constraints checkers 
 * @param <T>
 */
public interface IAgentConstraintChecker {

    /**
     * filters the given list of candidates and return the subset that meet the constraint enforced by this
     *  checker
     * @param profileId part of the context in which the constraints are Applied
     * @param fc part of the context in which the constraints are Applied
     * @param someCandidates the list of the candidates to check against the constraint enforced by this
     *  checker. cannot be null
     * @return return a subset of the candidates. never returns a null object
     */
    Collection<AgentController> applyConstraint(String profileId,
                                                FeatureController fc,
                                                Collection<AgentController> someCandidates);
}
