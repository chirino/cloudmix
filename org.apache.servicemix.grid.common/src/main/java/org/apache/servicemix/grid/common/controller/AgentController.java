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

import java.util.HashSet;
import java.util.Set;

import org.apache.servicemix.grid.common.GridController;
import org.apache.servicemix.grid.common.dto.AgentDetails;
import org.apache.servicemix.grid.common.dto.ProvisioningHistory;

/**
 * @version $Revision$
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