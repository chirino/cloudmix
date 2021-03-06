/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.testing;


import com.sun.jersey.api.client.filter.LoggingFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.agent.logging.LogRecord;
import org.fusesource.cloudmix.common.CloudmixHelper;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.GridClients;
import org.fusesource.cloudmix.common.ProcessClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.DependencyStatus;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProfileStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestName;

import javax.ws.rs.core.UriBuilder;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Creates a new temporary environment for a distributed test,
 * initialises the system, then runs the test and kills the enviroment
 *
 * @version $Revision: 1.1 $
 */
public abstract class TestController {

    /**
     * The name of the file which all the newly created profile IDs are written on each test run.
     * You can then clean up your test cloud by deleting all of the profiles in this file
     */
    public static final String PROFILE_ID_FILENAME = "cloudmix.profiles";

    private static final transient Log LOG = LogFactory.getLog(TestController.class);

    //CHECKSTYLE:OFF
    @Rule
    public TestName testName = new TestName();
    //CHECKSTYLE:ON

    protected long startupTimeout = 60 * 1000;
    protected String controllerUrl = CloudmixHelper.getDefaultRootUrl();

    protected List<FeatureDetails> features = new CopyOnWriteArrayList<FeatureDetails>();
    protected RestGridClient gridClient;
    protected ProfileDetails profile;
    protected String profileId;
    protected boolean provisioned;
    protected boolean destroyProfileAfter;
    protected boolean destroyOtherProfilesOnStartup = true;
    protected boolean logRestOperations;


    protected String getTestName() {
        String answer = testName.getMethodName();
        if (answer == null || answer.length() == 0) {
            return "Unknown";
        }
        return answer;
    }

    /**
     * Registers any features which are required for this system test
     */
    protected abstract void installFeatures();


    /**
     * Factory method to create a new feature which is unique to the current test's profile
     */
    protected FeatureDetails createFeatureDetails(String featureId, String uri) {
        FeatureDetails answer = new FeatureDetails(featureId, uri);
        ensureFeatureIdLocalToProfile(answer);
        return answer;
    }


    /**
     * Asserts that the test cloud is setup and provisioned properly within the given {@link #startupTimeout}.
     * <p/>
     * This method should be called within each test method so that the profile is setup
     * correctly with the class of the test and the test method name.
     */
    public void checkProvisioned() throws Exception {
        try {
            if (provisioned) {
                return;
            }

            // lets get the default URL for cloudmix
            System.out.println("Using controller URL: " + controllerUrl);

            // lets register the features
            GridClient controller = getGridClient();

            // allow system property to override this value
            String systemProperty = "cloudmix.destroyOtherProfilesOnStartup";
            String flag = System.getProperty(systemProperty);
            if (flag != null) {
                try {
                    destroyOtherProfilesOnStartup = Boolean.parseBoolean(flag);
                } catch (Exception e) {
                    LOG.error("Failed to parse boolean system property " + systemProperty + " with value: " + flag + ". Reason: " + e, e);
                }
            }
            else if (destroyOtherProfilesOnStartup) {
                LOG.info("About to destroy all previous JUnit profiles on the CloudMix server. " +
                        "To disable this behaviour set the destroyOtherProfilesOnStartup field to false on your JUnit class or set the '" +
                        systemProperty + "' system property to 'false''");
            }
            if (destroyOtherProfilesOnStartup) {
                destroyCurrentProfiles();
            }
            if (profileId == null) {
                profileId = UUID.randomUUID().toString();
            }

            // lets append the profileId to the file!
            onProfileIdCreated(profileId);
            profile = new ProfileDetails(profileId);

            installFeatures();

            for (FeatureDetails feature : features) {
                ensureFeatureIdLocalToProfile(feature);

                profile.getFeatures().add(new Dependency(feature.getId()));

                System.out.println("Adding feature: " + feature.getId());
                controller.addFeature(feature);
            }

            profile.setDescription(createProfileDescription(profile));

            controller.addProfile(profile);


            // now lets start the remote grid
            assertProvisioned();
            provisioned = true;

            System.out.println("All features provisioned!!");
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
            Throwable t = e;
            while (true) {
                Throwable throwable = t.getCause();
                if (throwable == t || throwable == null) {
                    break;
                }
                System.out.println("Caused by : " + throwable);
                throwable.printStackTrace();
                t = throwable;
            }
            LOG.error("Caught: " + e, e);
            throw e;
        }
    }

    /**
     * Destroys
     */
    protected void destroyCurrentProfiles() {
        File file = new File(PROFILE_ID_FILENAME);
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    line = line.trim();
                    if (line.startsWith("#") || line.length() == 0) {
                        continue;
                    }
                    LOG.info("Destroying old profile: " + line);
                    gridClient.removeFeature(line);
                }
                file.delete();
            } catch (IOException e) {
                LOG.error("Failed to read old profiles to file: " + file);
            }
        }
    }

    protected List<? extends ProcessClient> getProcessClientsFor(FeatureDetails featureDetails)
            throws URISyntaxException {

        return getProcessClientsFor(id(featureDetails));
    }

    private List<? extends ProcessClient> getProcessClientsFor(String featureId) throws URISyntaxException {
        return getGridClient().getProcessClientsForFeature(featureId);
    }

    /**
     * Returns all the agents which are running the given feature
     */
    protected List<AgentDetails> getAgentsFor(FeatureDetails featureDetails) throws URISyntaxException {
        return getAgentsFor(id(featureDetails));
    }

    protected String id(FeatureDetails featureDetails) {
        String id = featureDetails.getId();
        Assert.assertNotNull("Feature should have an ID " + featureDetails, id);
        return id;
    }

    /**
     * Returns all the agents which are running the given feature  ID
     */
    protected List<AgentDetails> getAgentsFor(String featureId) throws URISyntaxException {
        return GridClients.getAgentDetailsAssignedToFeature(getGridClient(), featureId);
    }


    protected String createProfileDescription(ProfileDetails pd) {
        return "CloudMix test case for class <b>" + getClass().getName()
                + "</b> with test method <b>" + getTestName() + "</b>";
    }

    /**
     * associate the feature with the profile, so that when the profile is deleted, so is the feature
     */
    protected void ensureFeatureIdLocalToProfile(FeatureDetails feature) {
        Assert.assertNotNull("profile ID should be defined!", profileId);

        // lets ensure the feature ID is unique (though the code could be smart enough to deduce it!)
        String featureId = feature.getId();
        if (!featureId.startsWith(profileId)) {
            featureId = profileId + ":" + featureId;
            feature.setId(featureId);
        }
        feature.setOwnedByProfileId(profileId);
    }

    protected void onProfileIdCreated(String profileid) throws IOException {
        String fileName = PROFILE_ID_FILENAME;
        try {
            FileWriter writer = new FileWriter(fileName, true);
            writer.append(profileid);
            writer.append("\n");
            writer.close();
        } catch (IOException e) {
            LOG.error("Failed to write profileId to file: " + fileName);
        }
    }

    @After
    public void tearDown() throws Exception {
        if (destroyProfileAfter) {
            if (gridClient != null) {
                if (profile != null) {
                    gridClient.removeProfile(profile);
                }
            }
            provisioned = false;
        }
    }

    public RestGridClient getGridClient() throws URISyntaxException {
        if (gridClient == null) {
            gridClient = createGridController();
        }
        return gridClient;
    }

    public void setGridClient(RestGridClient gridClient) {
        this.gridClient = gridClient;
    }

    /**
     * Returns a newly created client. Factory method
     */
    protected RestGridClient createGridController() throws URISyntaxException {
        System.out.println("About to create RestGridClient for: " + controllerUrl);
        RestGridClient answer = new RestGridClient(controllerUrl);
        if (logRestOperations) {
            answer.getClient(null).addFilter(new LoggingFilter());
        }
        return answer;
    }


    /**
     * Allow a feature to be registered prior to starting the profile
     */
    protected void addFeature(FeatureDetails featureDetails) {
        features.add(featureDetails);
    }


    /**
     * Allows feature to be registered prior to starting the profile
     */
    protected void addFeatures(FeatureDetails... featureDetails) {
        for (FeatureDetails featureDetail : featureDetails) {
            addFeature(featureDetail);
        }
    }


    /**
     * Allows feature to be registered prior to starting the profile
     */
    protected void addFeatures(Iterable<FeatureDetails> featureDetails) {
        for (FeatureDetails featureDetail : featureDetails) {
            addFeature(featureDetail);
        }
    }


    protected void getFeatureLogFromAgent(AgentDetails agent, FeatureDetails feature,
                                          String relativeLogPath, OutputStream os) throws Exception {
        if (!isSupportedAgent(agent)) {
            return;
        }
        URI uri = createRequestURI(agent, feature, relativeLogPath);
        RestGridClient client = new RestGridClient();
        client.setRootUri(uri, false);
        InputStream logStream = new BufferedInputStream(client.getInputStream());
        byte[] buf = new byte[4096];
        int len = 0;
        while ((len = logStream.read(buf)) != -1) {
            os.write(buf, 0, len);
        }

    }

    private boolean isSupportedAgent(AgentDetails agent) {
        if (!"mop".equals(agent.getContainerType().toLowerCase())) {
            LOG.info("Unsupported agent type " + agent.getContainerType());
            return false;
        }
        if (agent.getHref() == null) {
            LOG.info("Agent href is null, no log can be retrieved");
            return false;
        }
        return true;
    }

    private URI createRequestURI(AgentDetails agent, FeatureDetails feature, String relativeLogPath) {
        UriBuilder ub = UriBuilder.fromUri(agent.getHref());
        if ("mop".equals(agent.getContainerType().toLowerCase())) {
            ub.path("directory");
        }
        //else if ("karaf".equals(agent.getContainerType().toLowerCase())) {
        //   ub.path("instance"); ? 
        //}
        return ub.path(feature.getId().replace(':', '_')).path(relativeLogPath).build();
    }

    protected List<LogRecord> getFeatureLogRecordsFromAgent(AgentDetails agent, FeatureDetails feature,
                                                            String relativeLogPath, String queryName, String queryValue) throws Exception {
        return getFeatureLogRecordsFromAgent(agent, feature, relativeLogPath,
                Collections.singletonMap(queryName, Collections.singletonList(queryValue)));
    }

    protected List<LogRecord> getFeatureLogRecordsFromAgent(AgentDetails agent, FeatureDetails feature,
                                                            String relativeLogPath, Map<String, List<String>> queries) throws Exception {
        if (!isSupportedAgent(agent)) {
            return Collections.emptyList();
        }
        URI uri = createRequestURI(agent, feature, relativeLogPath);
        RestGridClient client = new RestGridClient(uri);
        return client.getLogRecords(queries);
    }

    /**
     * Asserts that all the requested features have been provisioned properly
     */
    protected void assertProvisioned() {
        long start = System.currentTimeMillis();

        Set<String> provisionedFeatures = new TreeSet<String>();
        Set<String> failedFeatures = null;
        while (true) {
            failedFeatures = new TreeSet<String>();
            long now = System.currentTimeMillis();

            try {
                ProfileStatus profileStatus = getGridClient().getProfileStatus(profileId);
                if (profileStatus != null) {
                    List<DependencyStatus> dependencyStatus = profileStatus.getFeatures();
                    for (DependencyStatus status : dependencyStatus) {
                        String featureId = status.getFeatureId();
                        if (status.isProvisioned()) {
                            if (provisionedFeatures.add(featureId)) {
                                LOG.info("Provisioned feature: " + featureId);
                            }
                        } else {
                            failedFeatures.add(featureId);
                        }
                    }
                }
                if (failedFeatures.isEmpty()) {
                    return;
                }
            } catch (URISyntaxException e) {
                LOG.warn("Failed to poll profile status: " + e, e);
            }

            long delta = now - start;
            if (delta > startupTimeout) {
                Assert.fail("Provision failure. Not enough instances of features: "
                        + failedFeatures + " after waiting " + (startupTimeout / 1000) + " seconds");
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

}
