package org.apache.servicemix.grid.common.controller.constraints.feature;

import java.util.List;

import org.apache.servicemix.grid.common.controller.FeatureController;

/**
 * Simple interface to implement constraints checkers 
 * @param <T>
 */
public interface IFeatureConstraintChecker {

    /**
     * filters the given list of candidates and return the subset that meet the constraint enforced by this
     *  checker
     * @param profileId the context in which the constraints are Applied
     * @param someCandidates the list of the candidates to check against the constraint enforced by this
     *  checker. cannot be null
     * @return return a subset of the candidates. never returns a null object
     */
    List<FeatureController> applyConstraint(String profileId, List<FeatureController> someCandidates);
}
