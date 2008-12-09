/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.controller;

import java.util.HashSet;
import java.util.Set;

import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;

/**
 * @version $Revision: 63441 $
 */
public class AgentController {
    

    AgentDetails details;
    ProvisioningHistory history;
    long nextHistoryId = 1;
    long lastActive;
    boolean deActivated;
    final Set<String> features = new HashSet<String>();

    private final GridController grid;
    
    public AgentController(GridController aGrid, AgentDetails someDetails) {        
        grid = aGrid;
        details = someDetails;
        deActivated = false;
    }

    public AgentDetails getDetails() {
        return details;
    }

    public void setDetails(AgentDetails details) {
        this.details = details;
    }
    
    public boolean isDeActivated() {
        return deActivated;
    }

    /**
     * only to be used when the represented agent has to be taken off the grid permanently
     */
    public void deActivate() {
        deActivated = true;
    }


    public ProvisioningHistory getHistory() {
        return history;
    }

    public void setHistory(ProvisioningHistory history) {
        this.history = history;
    }

    public void markActive() {
        lastActive = System.currentTimeMillis();
    }

    public boolean isActive(long now) {
        return now - lastActive < grid.getAgentTimeout();
    }

    public Set<String> getFeatures() {
        return features;
    }

    public String getNextHistoryId() {
        return "" + (nextHistoryId++);
    }

    public boolean hasReachedMaxNumberOfFeatureAllowed() {
        return getDetails().getMaximumFeatures() <= getFeatures().size();
    }

    public boolean isLockedByOwningFeature() {
        Set<String> featureIds = getFeatures();
        for (String featureId : featureIds) {
            FeatureController fc = grid.getFeatureController(featureId);
            if (fc != null) {
                if (fc.getDetails().isOwnsMachine()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    public boolean isPackageSupported(String packageType) {
        for (String testType : getDetails().getSupportPackageTypes()) {
            if (packageType.compareTo(testType) == 0) {
                return true;
            }
        }
        return false;
    }
    
    public boolean areAllPackagesSupported(String[] packageTypes) {
        for (String packageType : packageTypes) {
            if (!isPackageSupported(packageType)) {
                return false;
            }
        }
        return true;
    }
}