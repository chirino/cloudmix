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
package org.fusesource.cloudmix.controller.configadmindataprovider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;

import org.fusesource.cloudmix.common.ControllerDataProvider;
import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.controller.AgentController;
import org.fusesource.cloudmix.common.controller.FeatureController;
import org.fusesource.cloudmix.common.controller.ProfileController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ConfigurationException;

public class ConfigAdminControllerDataProvider implements ControllerDataProvider, ManagedService {

    private static final Log LOG = LogFactory.getLog(ConfigAdminControllerDataProvider.class);

    private ConcurrentMap<String, AgentController> agents = new ConcurrentHashMap<String, AgentController>();
    private ConcurrentMap<String, FeatureController> features = new ConcurrentHashMap<String, FeatureController>();
    private ConcurrentMap<String, ProfileController> profiles = new ConcurrentHashMap<String, ProfileController>();
    private GridController grid;

    public ConfigAdminControllerDataProvider() {
    }

    public void updated(Dictionary dictionary) throws ConfigurationException {
        LOG.info("Updating controller data provider");
        loadFeatures(dictionary);
        loadProfiles(dictionary);
    }

    protected void loadFeatures(Dictionary dictionary) {
        int numFeatures = Integer.parseInt((String) dictionary.get("features"));
        for (int i = 1; i <= numFeatures; i++) {
            String name = (String) dictionary.get("feature." + i + ".name");
            String url = (String) dictionary.get("feature." + i + ".url");
            FeatureDetails fd = new FeatureDetails(name, url);
            String deps = (String) dictionary.get("feature." + i + ".deps");
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

    protected void loadProfiles(Dictionary dictionary) {
        int numProfiles = Integer.parseInt((String) dictionary.get("profiles"));
        for (int i = 1; i <= numProfiles; i++) {
            String name = (String) dictionary.get("profile." + i + ".name");
            ProfileDetails pd = new ProfileDetails(name);
            String feats = (String) dictionary.get("profile." + i + ".features");
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
