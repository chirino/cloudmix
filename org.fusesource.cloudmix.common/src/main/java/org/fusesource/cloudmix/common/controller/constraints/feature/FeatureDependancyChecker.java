package org.fusesource.cloudmix.common.controller.constraints.feature;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.cloudmix.common.controller.FeatureController;

/**
 * checks that all the dependencies of a given feature are already setup and ready in the context of a 
 * given profile
 */
public class FeatureDependancyChecker implements IFeatureConstraintChecker {

    public List<FeatureController> applyConstraint(String profileId, List<FeatureController> someCandidates) {
        if (someCandidates == null) {
            return new ArrayList<FeatureController>(0);
        }
        
        if (profileId == null || someCandidates.size() == 0) {
            return new ArrayList<FeatureController>(someCandidates);
        }
        
        List<FeatureController> acceptedCandidates = new ArrayList<FeatureController>();
        for (FeatureController fc : someCandidates) {
            if (fc.areDependanciesInstanciated(profileId)) {
                acceptedCandidates.add(fc);
            }
        }
        return acceptedCandidates;
    }

}