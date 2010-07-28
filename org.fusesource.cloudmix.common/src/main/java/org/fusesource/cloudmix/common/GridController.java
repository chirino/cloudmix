/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common;

import java.util.List;

import org.fusesource.cloudmix.common.controller.FeatureController;
import org.fusesource.cloudmix.common.dto.Dependency;

/**
 * @version $Revision$
 */
public interface GridController extends GridClient {

    List<String> getAgentsAssignedToFeature(String featureId, String profileId, boolean onlyIfDeployed);


    long getAgentTimeout();
    FeatureController getFeatureController(String featureId);
    FeatureController getFeatureController(Dependency dependency);
    int getFeatureInstanceCount(String id, String profileId, boolean onlyIfDeployed);

}
