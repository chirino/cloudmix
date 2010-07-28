/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.controller.constraints.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.controller.FeatureController;

/**
 * checks that more instance(s) of a given feature can still be installed in the
 * context of a given profile
 */
public class FeatureMaxInstanceChecker implements IFeatureConstraintChecker {

    private static final transient Log LOG = LogFactory.getLog(FeatureMaxInstanceChecker.class);

    public List<FeatureController> applyConstraint(String profileId, List<FeatureController> someCandidates) {

        if (LOG.isDebugEnabled())
            LOG.debug("filtering on max instances: >" + profileId + "< " + someCandidates);

        List<FeatureController> acceptedCandidates = null;
        if (someCandidates == null) {
            acceptedCandidates = new ArrayList<FeatureController>(0);
        } else if (profileId == null || someCandidates.size() == 0) {
            acceptedCandidates = new ArrayList<FeatureController>(someCandidates);
        } else {
            acceptedCandidates = new ArrayList<FeatureController>();
            for (FeatureController fc : someCandidates) {
                if (fc.canDeployMoreInstances(profileId)) {
                    acceptedCandidates.add(fc);
                }
            }
        }
        
        if (LOG.isDebugEnabled())
            LOG.debug("filtered on max instances: >" + profileId + "< " + acceptedCandidates);

        return acceptedCandidates;
    }

}
