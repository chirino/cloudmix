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
package org.apache.servicemix.grid.controller.provisioning;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicemix.grid.common.ControllerDataProvider;
import org.apache.servicemix.grid.common.GridController;
import org.apache.servicemix.grid.common.controller.AgentController;
import org.apache.servicemix.grid.common.controller.FeatureController;
import org.apache.servicemix.grid.common.controller.ProfileController;
import org.apache.servicemix.grid.common.dto.Dependency;
import org.apache.servicemix.grid.common.dto.FeatureDetails;
import org.apache.servicemix.grid.common.dto.ProfileDetails;

/**
 * @version $Revision$
 */
public class SimpleControllerDataProvider implements ControllerDataProvider {
    private ConcurrentHashMap<String, AgentController> agents =
        new ConcurrentHashMap<String, AgentController>();
    private ConcurrentHashMap<String, FeatureController> features =
        new ConcurrentHashMap<String, FeatureController>();
    private ConcurrentHashMap<String, ProfileController> profiles =
        new ConcurrentHashMap<String, ProfileController>();    
    private GridController grid;
        
    public SimpleControllerDataProvider() { }
    
    public SimpleControllerDataProvider(GridController g) {
        grid = g;
        loadInitialDataFromFile(g);
    }
    
    private void loadInitialDataFromFile(GridController g) {
        File dataFile = new File("controller_data.properties");
        if (!dataFile.exists()) {
            return;
        }
        
        try {
            System.out.println("Loading intial controller data from " + dataFile.getCanonicalPath());
            
            Properties props = new Properties();
            props.load(new FileInputStream(dataFile));

            loadFeatures(props);
            loadProfiles(props);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void loadFeatures(Properties props) {
        int numFeatures = Integer.parseInt(props.getProperty("features"));
        for (int i = 1; i <= numFeatures; i++) {
            String name = props.getProperty("feature." + i + ".name");
            String url = props.getProperty("feature." + i + ".url");
            FeatureDetails fd = new FeatureDetails(name, url);
            
            String deps = props.getProperty("feature." + i + ".deps");
            if (deps != null) {
                List<Dependency> dependencies = new ArrayList<Dependency>();
                for (String dep : deps.split(",")) {
                    Dependency d = new Dependency();
                    d.setFeatureId(dep);
                    dependencies.add(d);
                }
                fd.setDependencies(dependencies);
            }
            
            FeatureController fc = new FeatureController(grid, fd);
            addFeature(name, fc);
        }
    }

    private void loadProfiles(Properties props) {
        int numProfiles = Integer.parseInt(props.getProperty("profiles"));
        for (int i = 1; i <= numProfiles; i++) {
            String name = props.getProperty("profile." + i + ".name");
            ProfileDetails pd = new ProfileDetails(name);
            
            String feats = props.getProperty("profile." + i + ".features");
            if (feats != null) {
                List<Dependency> profileFeatures = new ArrayList<Dependency>();
                for (String feat : feats.split(",")) {
                    Dependency f = new Dependency();
                    f.setFeatureId(feat);
                    profileFeatures.add(f);                    
                }
                pd.setFeatures(profileFeatures);
            }
            
            ProfileController pc = new ProfileController(grid, pd);
            addProfile(name, pc);
        }
    }
    public AgentController addAgent(String id, AgentController agent) {
        return agents.putIfAbsent(id, agent);
    }
    
    public AgentController getAgent(String agentId) {
        return agents.get(agentId);
    }

    public AgentController removeAgent(String agentId) {
        return agents.remove(agentId);
    }
    
    public AgentController updateAgent(String id, AgentController agent) {
        synchronized (agents) {
            removeAgent(id);
            return addAgent(id, agent);
        }
    }        
    
    public Collection<FeatureController> getFeatures() {
        return features.values();
    }
    
    public FeatureController getFeature(String featureId) {
        return features.get(featureId);
    }

    public FeatureController addFeature(String id, FeatureController featureController) {
        return features.putIfAbsent(id, featureController);
    }
    
    public FeatureController removeFeature(String featureId) {
        return features.remove(featureId);
    }

    public Collection<AgentController> getAgents() {
        return agents.values();
    }
    
    public ProfileController getProfile(String profileId) {
        return profiles.get(profileId);
    }
    
    public ProfileController addProfile(String id, ProfileController profileController) {
        return profiles.putIfAbsent(id, profileController);
    }
        
    public ProfileController removeProfile(String profileId) {
        return profiles.remove(profileId);
    }
    
    public Collection<ProfileController> getProfiles() {
        return profiles.values();
    }

    public GridController getGrid() {
        return grid;
    }

    public void setGrid(GridController g) {
        grid = g;
    }
}
