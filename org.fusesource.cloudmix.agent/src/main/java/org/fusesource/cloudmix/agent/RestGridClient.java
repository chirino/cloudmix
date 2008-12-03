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
package org.fusesource.cloudmix.agent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.security.PasswordProvider;
import org.fusesource.cloudmix.agent.security.SecurityUtils;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.AgentDetailsList;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetailsList;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;
import org.fusesource.cloudmix.common.dto.StringList;
import org.fusesource.cloudmix.common.util.ObjectHelper;

/**
 * @version $Revision: 61256 $
 */
public class RestGridClient extends RestClientSupport implements GridClient {
    
    private static final transient Log LOG = LogFactory.getLog(RestGridClient.class);

    private URI agentsUri;
    private URI featuresUri;
    private URI profilesUri;

    private String username;
    private PasswordProvider passwordProvider;
    private String credentials;
    
    public void setUsername(String u) {
        username = u;
    }
    
    public String getUsername() {
        return username;
    }

    public void setPasswordProvider(PasswordProvider pp) {
        passwordProvider = pp;
    }
    
    public PasswordProvider getPasswordProvider() {
        return passwordProvider;
    }
    
    public void setCredentials(String c) {
        credentials = c;
    }
    
    public String getCredentials() {
        if (credentials == null) {
            // Determine credentials from username/password
            if (username == null) {
                return null;
            }
            LOG.info("Getting credentials for user " + username);
            if (passwordProvider == null) {
                LOG.warn("cannot provide credentials for user \"" + username
                        + "\", no password provider");
                return null;
            }
            char[] password = passwordProvider.getPassword();
            
            if (password == null) {
                LOG.warn("cannot provide credentials for user \"" + username
                        + "\", no password provided");
                return null;
            }
            credentials = SecurityUtils.toBasicAuth(username, password);        
        }
        
        return credentials;
    }
    
    public String addAgentDetails(AgentDetails agentDetails) throws URISyntaxException {
        WebResource.Builder agentsResource = resource(getAgentsUri()).type("application/xml");

        ClientResponse response = agentsResource.post(ClientResponse.class, agentDetails);
        URI location = response.getLocation();
        if(location == null) {
            throw new URISyntaxException("", "Error getting location from response!");
        }
        String path = location.getPath();
        // The agent id is the last part of that path..
        path = path.substring(path.lastIndexOf("/") + 1);
        return path;
    }
    
    public void updateAgentDetails(String agentId, AgentDetails agentDetails) throws URISyntaxException {
        WebResource.Builder resource =
            resource(append(getAgentsUri(), "/", agentId)).accept("application/xml");
        getTemplate().put(resource, agentDetails);
    }

    public AgentDetails getAgentDetails(String agentId) throws URISyntaxException {
        WebResource.Builder resource =
            resource(append(getAgentsUri(), "/", agentId)).accept("application/xml");
        return getTemplate().get(resource, AgentDetails.class);
    }

    public List<AgentDetails> getAllAgentDetails() throws URISyntaxException {
        WebResource.Builder resource = resource(getAgentsUri()).accept("application/xml");

        AgentDetailsList answer = getTemplate().get(resource, AgentDetailsList.class);
        if (answer == null) {
            // TODO just return empty list?
            return new ArrayList<AgentDetails>();
        } else {
            return answer.getAgents();
        }
    }

    public void removeAgentDetails(String agentId) throws URISyntaxException {
        WebResource.Builder resource =
            resource(append(getAgentsUri(), "/", agentId)).accept("application/xml");
        getTemplate().delete(resource);
    }

    public ProvisioningHistory getAgentHistory(String agentId) throws URISyntaxException {
        WebResource.Builder resource =
            resource(append(getAgentsUri(), "/", agentId, "/history")).accept("application/xml");
        return getTemplate().get(resource, ProvisioningHistory.class);
    }

    public ProvisioningHistory pollAgentHistory(String agentId) throws URISyntaxException {
        WebResource.Builder resource =
            resource(append(getAgentsUri(), "/", agentId, "/history")).accept("application/xml");
        LOG.debug("polling agent history, agent id: " + agentId);
        LOG.debug("polling agent history, resource: " + resource);
        
        return getTemplate().poll(resource, ProvisioningHistory.class);
    }

    public List<FeatureDetails> getFeatures() throws URISyntaxException {
        WebResource.Builder resource = resource(getFeaturesUri()).accept("application/xml");

        FeatureDetailsList answer = getTemplate().get(resource, FeatureDetailsList.class);
        if (answer == null) {
            // TODO just return empty list?
            return new ArrayList<FeatureDetails>();
        } else {
            return answer.getFeatures();
        }
    }

    public void addFeature(FeatureDetails feature) throws URISyntaxException {
        String id = feature.getId();
        WebResource.Builder resource = resource(append(getFeaturesUri(), "/", id)).type("application/xml");
        getTemplate().put(resource, feature);
    }

    public void removeFeature(String id) throws URISyntaxException {
        ObjectHelper.notNull(id, "feature.id");
        WebResource.Builder resource = resource(append(getFeaturesUri(), "/", id)).type("application/xml");
        getTemplate().delete(resource);
    }

    public void removeFeature(FeatureDetails feature) throws URISyntaxException {
        String id = feature.getId();
        ObjectHelper.notNull(id, "feature.id");
        removeFeature(id);
    }

    public void addAgentToFeature(String featureId,
                                  String agentId,
                                  Map<String, String> cfgOverridesProps) throws URISyntaxException {
        WebResource.Builder resource =
            resource(append(getFeaturesUri(), "/", featureId, "/agents/", agentId)).type("application/xml");
        getTemplate().put(resource);
    }

    public void removeAgentFromFeature(String featureId, String agentId) throws URISyntaxException {
        WebResource.Builder resource =
            resource(append(getFeaturesUri(), "/", featureId, "/agents/", agentId)).type("application/xml");
        getTemplate().delete(resource);
    }

    public List<String> getAgentsAssignedToFeature(String id) throws URISyntaxException {
        ObjectHelper.notNull(id, "feature.id");
        WebResource.Builder resource =
            resource(append(getFeaturesUri(), "/", id, "/agents")).type("application/xml");
        StringList answer = getTemplate().get(resource, StringList.class);
        return answer.getValues();
    }
    
    public void addProfile(ProfileDetails profile) throws URISyntaxException {
        String id = profile.getId();
        WebResource.Builder resource = resource(append(getProfilesUri(), "/", id)).type("application/xml");
        getTemplate().put(resource, profile);
    }

    

    // Properties
    //-------------------------------------------------------------------------

    public URI getAgentsUri() throws URISyntaxException {
        if (agentsUri == null) {
            agentsUri = getRootUri().resolve("agents");
            LOG.debug("agents URI : " + agentsUri);
        }
        return agentsUri;
    }

    public URI getFeaturesUri() throws URISyntaxException {
        if (featuresUri == null) {
            featuresUri = getRootUri().resolve("features");
        }
        return featuresUri;
    }

    public URI getProfilesUri() throws URISyntaxException {
        if (profilesUri == null) {
            profilesUri = getRootUri().resolve("profiles");
        }
        return profilesUri;
    }
    
    
    
    private WebResource resource(URI uri) {
        return getClient(getCredentials()).resource(uri);
    }
    
    
}
