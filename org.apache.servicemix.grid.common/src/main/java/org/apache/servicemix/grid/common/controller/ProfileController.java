/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.grid.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.grid.common.GridController;
import org.apache.servicemix.grid.common.controller.constraints.feature.FeatureDependancyChecker;
import org.apache.servicemix.grid.common.controller.constraints.feature.FeatureMaxInstanceChecker;
import org.apache.servicemix.grid.common.controller.constraints.feature.IFeatureConstraintChecker;
import org.apache.servicemix.grid.common.dto.ConfigurationUpdate;
import org.apache.servicemix.grid.common.dto.Dependency;
import org.apache.servicemix.grid.common.dto.ProfileDetails;

public class ProfileController {
    
    private static final transient Log LOG = LogFactory.getLog(ProfileController.class);
    
    private static final List<IFeatureConstraintChecker> CHECKERS = 
        new ArrayList<IFeatureConstraintChecker>();

    static {
        CHECKERS.add(new FeatureDependancyChecker());
        CHECKERS.add(new FeatureMaxInstanceChecker());
    }
    
    private GridController grid;
    private ProfileDetails details;
    private boolean hasChanged;

    public ProfileController(GridController grid, ProfileDetails details) {
        this.grid = grid;
        this.details = details;
    }
    
    public ProfileDetails getDetails() {
        return details;
    }
    
    public GridController getGridController() {
        return grid;
    }
    
    public void setChanged(boolean hasIt) {
        hasChanged = hasIt;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public List<FeatureController> getDeployableFeatures() {
        
        List<FeatureController> candidates = getFeatureControllersForDependencies();
        
        for (IFeatureConstraintChecker checker : CHECKERS) {
            candidates = checker.applyConstraint(details.getId(), candidates);
        }
        
        return candidates;
    }

    private List<FeatureController> getFeatureControllersForDependencies() {
        List<FeatureController> candidates = new ArrayList<FeatureController>();
        for (Dependency featureDependency : getDetails().getFeatures()) {
            FeatureController feature = grid.getFeatureController(featureDependency);
            if (feature == null) {
                LOG.warn("Could not find feature: " + featureDependency.getFeatureId());
                continue;
            }
            candidates.add(feature);
        }
        return candidates;
    }
    
    public boolean compare(ProfileController another) {
        
        Map<String, Dependency> otherDeps = new HashMap<String, Dependency>();

        if (another == null) {
            setChanged(true);
        
        } else {

            ProfileDetails otherDetails = another.getDetails();
            
            if (!details.getId().equals(otherDetails.getId())
                    || details.getFeatures().size() != otherDetails.getFeatures().size()) {
                setChanged(true);
            }
            
            for (Dependency dep : otherDetails.getFeatures()) {
                otherDeps.put(dep.getFeatureId(), dep);
            }
        }
        

        for (Dependency dep : details.getFeatures()) {
            Dependency otherDep = otherDeps.get(dep.getFeatureId());
            dep.setChanged(false);
            if (otherDep == null || dep.getCfgUpdates().size() != otherDep.getCfgUpdates().size()) {
                setChanged(true);
                dep.setChanged(true);
            }
            
            Map<String, String> otherCfgUpdates = new HashMap<String, String>();
            if (otherDep != null) {
                for (ConfigurationUpdate cfgUpdate : otherDep.getCfgUpdates()) {
                    otherCfgUpdates.put(cfgUpdate.getProperty(), cfgUpdate.getValue());
                }
            }
            
            for (ConfigurationUpdate cfgUpdate : dep.getCfgUpdates()) {
                if (!cfgUpdate.getValue().equals(otherCfgUpdates.get(cfgUpdate.getProperty()))) {
                    setChanged(true);
                    dep.setChanged(true);
                }
            }
            
        }
        
        return !hasChanged();
        
    }

}
