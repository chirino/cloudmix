/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.testing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.common.CloudmixHelper;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.DependencyStatus;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProfileStatus;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Creates a new temporary environment for a distributed test,
 * initialises the system, then runs the test and kills the enviroment
 *
 * @version $Revision: 1.1 $
 */
public abstract class TestController {
    private static final transient Log LOG = LogFactory.getLog(TestController.class);

    /**
     * The name of the file which all the newly created profile IDs are written on each test run.
     * You can then clean up your test cloud by deleting all of the profiles in this file
     */
    public static final String PROFILE_ID_FILENAME = ".cloudmix.profiles";

    protected long startupTimeout = 60 * 1000;
    protected String controllerUrl = CloudmixHelper.getDefaultRootUrl();

    protected List<FeatureDetails> features = new CopyOnWriteArrayList<FeatureDetails>();
    protected GridClient gridClient;
    protected ProfileDetails profile;
    protected String profileId;
    protected boolean provisioned;
    protected boolean destroyProfileAfter = false;

    @Rule
    public TestName testName = new TestName();

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
     * 
     * This method should be called within each test method so that the profile is setup correctly with the class of the test
     * and the test method name.
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
     * Returns all the agents which are running the given feature
     */
    protected List<AgentDetails> getAgentsFor(FeatureDetails featureDetails) throws URISyntaxException {
        String id = featureDetails.getId();
        Assert.assertNotNull("Feature should have an ID " + featureDetails, id);
        return getAgentsFor(id);
    }

    /**
     * Returns all the agents which are running the given feature  ID
     */
    protected List<AgentDetails> getAgentsFor(String featureId) throws URISyntaxException {
        List<String> agentIds = getGridClient().getAgentsAssignedToFeature(featureId);
        List<AgentDetails> answer = new ArrayList<AgentDetails>();
        for (String agentId : agentIds) {
            AgentDetails agentDetails = getGridClient().getAgentDetails(agentId);
            if (agentDetails != null) {
                answer.add(agentDetails);
            }
        }
        return answer;
    }


    protected String createProfileDescription(ProfileDetails profile) {
        return "CloudMix test case for class <b>" + getClass().getName() + "</b> with test method <b>" + getTestName() + "</b>";
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

    protected void onProfileIdCreated(String profileId) throws IOException {
        String fileName = PROFILE_ID_FILENAME;
        try {
            FileWriter writer = new FileWriter(fileName, true);
            writer.append(profileId);
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

    public GridClient getGridClient() throws URISyntaxException {
        if (gridClient == null) {
            gridClient = createGridController();
        }
        return gridClient;
    }

    public void setGridClient(GridClient gridClient) {
        this.gridClient = gridClient;
    }

    /**
     * Returns a newly created client. Factory method
     */
    protected GridClient createGridController() throws URISyntaxException {
        System.out.println("About to create RestGridClient for: " + controllerUrl);
        return new RestGridClient(controllerUrl);
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
                Assert.fail("Provision failure. Not enough instances of features: " + failedFeatures + " after waiting " + (startupTimeout / 1000) + " seconds");
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
