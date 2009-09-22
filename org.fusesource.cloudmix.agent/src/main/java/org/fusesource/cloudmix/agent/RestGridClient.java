/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.security.PasswordProvider;
import org.fusesource.cloudmix.agent.security.SecurityUtils;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.ProcessClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.AgentDetailsList;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetailsList;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetailsList;
import org.fusesource.cloudmix.common.dto.ProfileStatus;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;
import org.fusesource.cloudmix.common.dto.Resource;
import org.fusesource.cloudmix.common.dto.ResourceList;
import org.fusesource.cloudmix.common.dto.StringList;
import org.fusesource.cloudmix.common.util.ObjectHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

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
    private boolean loggedNoPassword;

    public RestGridClient() {
    }

    public RestGridClient(String rootUri) throws URISyntaxException {
        this(new URI(rootUri));
    }

    public RestGridClient(URI rootUri) throws URISyntaxException {
        setRootUri(rootUri);
    }

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
            LOG.debug("Getting credentials for user " + username);
            if (passwordProvider == null) {
                if (!loggedNoPassword) {
                    loggedNoPassword = true;
                    LOG.warn("cannot provide credentials for user \"" + username
                            + "\", no password provider");
                }

                return null;
            }
            char[] password = passwordProvider.getPassword();
            if (password == null) {
                if (!loggedNoPassword) {
                    loggedNoPassword = true;
                    LOG.warn("cannot provide credentials for user \"" + username
                            + "\", no password provided");
                }
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
        if (location == null) {
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
        ObjectHelper.notNull(id, "FeatureDetails.id");
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
                resource(append(getFeaturesUri(), "/", featureId,
                                "/agents/", agentId)).type("application/xml");
        getTemplate().put(resource);
    }

    public void removeAgentFromFeature(String featureId, String agentId) throws URISyntaxException {
        WebResource.Builder resource =
                resource(append(getFeaturesUri(), "/", featureId,
                                "/agents/", agentId)).type("application/xml");
        getTemplate().delete(resource);
    }

    public List<String> getAgentsAssignedToFeature(String id) throws URISyntaxException {
        ObjectHelper.notNull(id, "feature.id");
        WebResource.Builder resource =
                resource(append(getFeaturesUri(), "/", id, "/agents")).accept("application/xml");
        StringList answer = getTemplate().get(resource, StringList.class);
        if (answer == null) {
            // TODO just return empty list?
            return new ArrayList<String>();
        }
        return answer.getValues();
    }

    public List<? extends ProcessClient> getProcessClientsForFeature(String featureId) throws URISyntaxException {
        List<RestProcessClient> answer = new ArrayList<RestProcessClient>();
        List<AgentDetails> list = Collections.EMPTY_LIST;
        for (int i = 0; i < 10; i++) {
            list = GridClients.getAgentDetailsAssignedToFeature(this, featureId);
            if (!list.isEmpty()) {
                break;
            }
            LOG.debug("Retrying...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        for (AgentDetails agentDetails : list) {
            String href = agentDetails.getHref();
            if (href != null && href.contains("://")) {
                // lets get the processes for this feature
                if (!href.endsWith("/")) {
                    href = href + "/";
                }
                String uri = href + "features/" + featureId;

                LOG.debug("About to test feature URI: " + uri);
                //System.out.println("Found: " + resource(new URI(uri)).accept("text/xml").get(String.class));

                ResourceList resourceList = resource(new URI(uri)).accept("text/xml").get(ResourceList.class);
                if (resourceList != null) {
                    System.out.println(uri + " Found: " + resourceList);
                    List<Resource> resources = resourceList.getResources();
                    for (Resource resource : resources) {
                        RestProcessClient client = createProcessClient(agentDetails, featureId, resource);
                        if (client != null) {
                            answer.add(client);
                        }
                    }
                }
                else {
                    LOG.warn("No ResourceList found for " + uri);
                }
            } else {
                LOG.warn("Ignoring agent " + agentDetails + " due to bad href " + href);
            }
        }
        return answer;
    }

    private RestProcessClient createProcessClient(AgentDetails agentDetails, String featureId, Resource resource) throws URISyntaxException {
        return new RestProcessClient(agentDetails.getHref() + resource.getHref());
    }

    public ProfileDetails getProfile(String id) throws URISyntaxException {
        WebResource.Builder resource =
                resource(append(getProfilesUri(), "/", id)).accept("application/xml");
        return getTemplate().get(resource, ProfileDetails.class);
    }

    public ProfileStatus getProfileStatus(String id) throws URISyntaxException {
        WebResource.Builder resource =
                resource(append(getProfilesUri(), "/", id, "/status")).accept("application/xml");
        return getTemplate().get(resource, ProfileStatus.class);
    }

    public List<ProfileDetails> getProfiles() throws URISyntaxException {
        WebResource.Builder resource = resource(getProfilesUri()).accept("application/xml");

        ProfileDetailsList answer = getTemplate().get(resource, ProfileDetailsList.class);
        if (answer == null) {
            // TODO just return empty list?
            return new ArrayList<ProfileDetails>();
        } else {
            return answer.getProfiles();
        }
    }

    public void addProfile(ProfileDetails profile) throws URISyntaxException {
        String id = profile.getId();
        WebResource.Builder resource = resource(append(getProfilesUri(), "/", id)).type("application/xml");
        int status = getTemplate().put(resource, profile);
        System.out.println("updating profile " + profile + " status " + status);
    }

    public void removeProfile(ProfileDetails profile) throws URISyntaxException {
        String id = profile.getId();
        ObjectHelper.notNull(id, "profile.id");
        removeProfile(id);
    }

    public void removeProfile(String id) throws URISyntaxException {
        ObjectHelper.notNull(id, "profile.id");
        WebResource.Builder resource = resource(append(getProfilesUri(), "/", id)).type("application/xml");
        getTemplate().delete(resource);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("about to use URI: " + uri);
        }
        return getClient(getCredentials()).resource(uri);
    }

}
