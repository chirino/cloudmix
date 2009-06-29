/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.testing;

import org.fusesource.cloudmix.common.GridController;
import org.fusesource.cloudmix.common.controller.FeatureController;
import org.fusesource.cloudmix.common.controller.ProfileController;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.controller.provisioning.DefaultGridController;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Before;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
    protected GridController gridController;
    protected long startupTimeout = 30 * 1000;
    protected List<FeatureDetails> features = new CopyOnWriteArrayList<FeatureDetails>();

    protected ProfileController profileController;
    protected ProfileDetails profile;
    protected String profileId;
    protected boolean provisioned;

    /**
     * Registers any features which are required for this system test
     */
    protected abstract void installFeatures();

    @Before
    public void checkProvisioned() {
        if (provisioned) {
            return;
        }

        // lets register the features
        GridController controller = getGridController();

        if (profileId == null) {
            profileId = UUID.randomUUID().toString();
        }
        profile = new ProfileDetails(profileId);
        profileController = new ProfileController(controller, profile);

        installFeatures();


        for (FeatureDetails feature : features) {

            // TODO should we explicity force each featureDetails ID to be unique so we can zap them later
            // and feature details don't overwrite each other?
            String featureId = feature.getId();
            if (!featureId.startsWith(profileId)){
                feature.setId(profileId + ":" + featureId);
            }
            profileController.getGridController().addFeature(feature);

            Map<String, String> config = new TreeMap<String, String>();
            profile.addFeature(feature.getId(), config);
        }

        profileController.getGridController().addProfile(profile);

        // now lets start the remote grid
        assertProvisioned();
        provisioned = true;

        System.out.println("All features provisioned!!");
    }

    public GridController getGridController() {
        if (gridController == null) {
            gridController = createGridController();
        }
        return gridController;
    }

    public void setGridController(GridController gridController) {
        this.gridController = gridController;
    }

    /**
     * Returns a newly created client. Factory method
     */
    protected GridController createGridController() {
        return new DefaultGridController();
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

        Set<String> failedFeatures = null;
        while (true) {
            failedFeatures = new TreeSet<String>();
            long now = System.currentTimeMillis();
            List<FeatureController> features = profileController.getDeployableFeatures();
            for (FeatureController feature : features) {
                if (!feature.hasAtLeastMinimumInstances(profileId)) {
                    failedFeatures.add(feature.getId() + "=" + feature.getResource());
                }
            }

            if (features.isEmpty()) {
                return;
            } else {
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


}
