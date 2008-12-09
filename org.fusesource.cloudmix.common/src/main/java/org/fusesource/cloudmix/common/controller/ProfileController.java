/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.controller.constraints.feature.FeatureDependancyChecker;
import org.fusesource.cloudmix.common.controller.constraints.feature.FeatureMaxInstanceChecker;
import org.fusesource.cloudmix.common.controller.constraints.feature.IFeatureConstraintChecker;
import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.ProfileDetails;

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
