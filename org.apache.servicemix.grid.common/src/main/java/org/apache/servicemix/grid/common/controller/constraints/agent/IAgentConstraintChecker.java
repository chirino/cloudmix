package org.apache.servicemix.grid.common.controller.constraints.agent;

import java.util.Collection;

import org.apache.servicemix.grid.common.controller.AgentController;
import org.apache.servicemix.grid.common.controller.FeatureController;

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
