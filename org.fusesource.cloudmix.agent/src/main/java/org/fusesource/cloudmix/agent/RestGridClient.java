/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.logging.LogRecord;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.GridClients;
import org.fusesource.cloudmix.common.ProcessClient;
import org.fusesource.cloudmix.common.URIs;
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


/**
 * @version $Revision: 61256 $
 */
public class RestGridClient extends RestClientSupport implements GridClient {
    private static final transient Log LOG = LogFactory.getLog(RestGridClient.class);


    private URI agentsUri;
    private URI featuresUri;
    private URI profilesUri;

    public RestGridClient() {
    }

    public RestGridClient(String rootUri) {
        this(URIs.createURI(rootUri));
    }

    public RestGridClient(URI rootUri) {
        setRootUri(rootUri);
    }

    public String addAgentDetails(AgentDetails agentDetails) {
        WebResource.Builder agentsResource = resource(getAgentsUri()).type("application/xml");

        ClientResponse response = agentsResource.post(ClientResponse.class, agentDetails);
        URI location = response.getLocation();
        if (location == null) {
            throw new IllegalArgumentException("No Location header returned from response!");
        }
        String path = location.getPath();
        // The agent id is the last part of that path..
        path = path.substring(path.lastIndexOf("/") + 1);
        return path;
    }

    public void updateAgentDetails(String agentId, AgentDetails agentDetails) {
        WebResource.Builder resource =
                resource(append(getAgentsUri(), "/", agentId)).accept("application/xml");
        getTemplate().put(resource, agentDetails);
    }

    public List<LogRecord> getLogRecords(Map<String, List<String>> queries) {
        WebResource.Builder resource =
                resource(append(getRootUri(), "/log")).accept("application/xml");
        assert resource != null;
        // TODO: how to tell Jersey to get List<LogRecord> ?
        return Collections.emptyList();
    }

    public AgentDetails getAgentDetails(String agentId) {
        WebResource.Builder resource =
                resource(append(getAgentsUri(), "/", agentId)).accept("application/xml");
        return getTemplate().get(resource, AgentDetails.class);
    }

    public List<AgentDetails> getAllAgentDetails() {
        WebResource.Builder resource = resource(getAgentsUri()).accept("application/xml");

        AgentDetailsList answer = getTemplate().get(resource, AgentDetailsList.class);
        if (answer == null) {
            // TODO just return empty list?
            return new ArrayList<AgentDetails>();
        } else {
            return answer.getAgents();
        }
    }

    public InputStream getInputStream() {
        WebResource.Builder resource =
                resource(getRootUri()).accept("*/*");
        return resource.get(InputStream.class);
    }

    public void removeAgentDetails(String agentId) {
        WebResource.Builder resource =
                resource(append(getAgentsUri(), "/", agentId)).accept("application/xml");
        getTemplate().delete(resource);
    }

    public ProvisioningHistory getAgentHistory(String agentId) {
        WebResource.Builder resource =
                resource(append(getAgentsUri(), "/", agentId, "/history")).accept("application/xml");
        return getTemplate().get(resource, ProvisioningHistory.class);
    }

    public ProvisioningHistory pollAgentHistory(String agentId) {
        WebResource.Builder resource =
                resource(append(getAgentsUri(), "/", agentId, "/history")).accept("application/xml");
        LOG.debug("polling agent history, agent id: " + agentId);
        LOG.debug("polling agent history, resource: " + resource);

        return getTemplate().poll(resource, ProvisioningHistory.class);
    }

    public List<FeatureDetails> getFeatures() {
        WebResource.Builder resource = resource(getFeaturesUri()).accept("application/xml");

        FeatureDetailsList answer = getTemplate().get(resource, FeatureDetailsList.class);
        if (answer == null) {
            // TODO just return empty list?
            return new ArrayList<FeatureDetails>();
        } else {
            return answer.getFeatures();
        }
    }

    public void addFeature(FeatureDetails feature) {
        String id = feature.getId();
        ObjectHelper.notNull(id, "FeatureDetails.id");
        WebResource.Builder resource = resource(append(getFeaturesUri(), "/", id)).type("application/xml");
        getTemplate().put(resource, feature);
    }

    public void removeFeature(String id) {
        ObjectHelper.notNull(id, "feature.id");
        WebResource.Builder resource = resource(append(getFeaturesUri(), "/", id)).type("application/xml");
        getTemplate().delete(resource);
    }

    public FeatureDetails getFeature(String featureId) {
        ObjectHelper.notNull(featureId, "featureId");
        WebResource.Builder resource = resource(append(getFeaturesUri(), "/", featureId))
            .accept("application/xml");
        return getTemplate().get(resource, FeatureDetails.class);
    }

    public void removeFeature(FeatureDetails feature) {
        String id = feature.getId();
        ObjectHelper.notNull(id, "feature.id");
        removeFeature(id);
    }

    public void addAgentToFeature(String featureId,
                                  String agentId,
                                  Map<String, String> cfgOverridesProps) {
        WebResource.Builder resource =
                resource(append(getFeaturesUri(), "/", featureId,
                        "/agents/", agentId)).type("application/xml");
        getTemplate().put(resource);
    }

    public void removeAgentFromFeature(String featureId, String agentId) {
        WebResource.Builder resource =
                resource(append(getFeaturesUri(), "/", featureId,
                        "/agents/", agentId)).type("application/xml");
        getTemplate().delete(resource);
    }

    public List<String> getAgentsAssignedToFeature(String id) {
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

    public List<? extends ProcessClient> getProcessClientsForFeature(String featureId) {

        List<RestProcessClient> answer = new ArrayList<RestProcessClient>();
        List<AgentDetails> list = Collections.emptyList();
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

                ResourceList resourceList = resource(URIs.createURI(uri)).accept("text/xml")
                    .get(ResourceList.class);
                if (resourceList != null) {
                    System.out.println(uri + " Found: " + resourceList);
                    List<Resource> resources = resourceList.getResources();
                    for (Resource resource : resources) {
                        RestProcessClient client = createProcessClient(agentDetails, featureId, resource);
                        if (client != null) {
                            answer.add(client);
                        }
                    }
                } else {
                    LOG.warn("No ResourceList found for " + uri);
                }
            } else {
                LOG.warn("Ignoring agent " + agentDetails + " due to bad href " + href);
            }
        }
        return answer;
    }

    private RestProcessClient createProcessClient(AgentDetails agentDetails, String featureId,
                                                  Resource resource) {
        return new RestProcessClient(agentDetails.getHref() + resource.getHref());
    }

    public ProfileDetails getProfile(String id) {
        WebResource.Builder resource =
                resource(append(getProfilesUri(), "/", id)).accept("application/xml");
        return getTemplate().get(resource, ProfileDetails.class);
    }

    public ProfileStatus getProfileStatus(String id) {
        WebResource.Builder resource =
                resource(append(getProfilesUri(), "/", id, "/status")).accept("application/xml");
        return getTemplate().get(resource, ProfileStatus.class);
    }

    public List<ProfileDetails> getProfiles() {
        WebResource.Builder resource = resource(getProfilesUri()).accept("application/xml");

        ProfileDetailsList answer = getTemplate().get(resource, ProfileDetailsList.class);
        if (answer == null) {
            // TODO just return empty list?
            return new ArrayList<ProfileDetails>();
        } else {
            return answer.getProfiles();
        }
    }

    public void addProfile(ProfileDetails profile) {
        String id = profile.getId();
        WebResource.Builder resource = resource(append(getProfilesUri(), "/", id)).type("application/xml");
        int status = getTemplate().put(resource, profile);
        System.out.println("updating profile " + profile + " status " + status);
    }

    public void removeProfile(ProfileDetails profile) {
        String id = profile.getId();
        ObjectHelper.notNull(id, "profile.id");
        removeProfile(id);
    }

    public void removeProfile(String id) {
        ObjectHelper.notNull(id, "profile.id");
        WebResource.Builder resource = resource(append(getProfilesUri(), "/", id)).type("application/xml");
        getTemplate().delete(resource);
    }


    /**
     * Returns the configuration properties for the given profile ID
     */
    public Properties getProperties(String profileId) {
        ObjectHelper.notNull(profileId, "profile.id");
        WebResource.Builder resource = resource(append(getProfilesUri(), "/", profileId, "/properties"))
            .accept("application/xml");
        return getTemplate().get(resource, Properties.class);
    }


    // Properties
    //-------------------------------------------------------------------------

    public URI getAgentsUri() {
        if (agentsUri == null) {
            agentsUri = getRootUri().resolve("agents");
            LOG.debug("agents URI : " + agentsUri);
        }
        return agentsUri;
    }

    public URI getFeaturesUri() {
        if (featuresUri == null) {
            featuresUri = getRootUri().resolve("features");
        }
        return featuresUri;
    }

    public URI getProfilesUri() {
        if (profilesUri == null) {
            profilesUri = getRootUri().resolve("profiles");
        }
        return profilesUri;
    }
}
