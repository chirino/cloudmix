/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.usecase;

import java.util.Arrays;

import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.controller.provisioning.ProvisioningTestSupport;

public class SharedFeaturesOnMultipleProfilesTest extends ProvisioningTestSupport {
    
    public void testSharedFeaturesOnMultipleProfiles() throws Exception {
        // 2 profiles: 'testing', 'production'
        // 3 features: tf (min1 max2 testing), gf (min1 max2 general) and pf (min1 max2 production)
        // 2 agents, one per profile can hold many features

        agentCluster.createInstallAgentsInProfiles("testing", "production");
        InstallerAgent ta = null;
        InstallerAgent pa = null;
        for (InstallerAgent ia : agentCluster.getAgents()) {
            if (ia.getProfile().equals("testing")) {
                ta = ia;
            } else if (ia.getProfile().equals("production")) {
                pa = ia;
            } else {
                fail("Unexpected agent found");
            }
            ia.setMaxFeatures(2);
        }

        waitForAgentsToActivate();

        ProfileDetails testProfile = new ProfileDetails("testing")
                                        .addFeature("tf", null)
                                        .addFeature("gf", null);
        ProfileDetails prodProfile = new ProfileDetails("production")
                                        .addFeature("pf", null)
                                        .addFeature("gf", null);

        FeatureDetails tf = new FeatureDetails("tf").minimumInstances("" + 1).maximumInstances("" + 2);
        FeatureDetails gf = new FeatureDetails("gf").minimumInstances("" + 1).maximumInstances("" + 2);
        FeatureDetails pf = new FeatureDetails("pf").minimumInstances("" + 1).maximumInstances("" + 2);

        gridController.addFeatures(tf, gf, pf);
        gridController.addProfiles(testProfile, prodProfile);

        waitForFeaturesToProvision();

        agentCluster.dumpAgents();
        agentCluster.assertFeatureInstances("tf", 1);
        agentCluster.assertFeatureInstances("gf", 2);
        agentCluster.assertFeatureInstances("pf", 1);

        assertEquals(Arrays.asList(ta), agentCluster.agentsWithFeature("tf"));
        assertEquals(Arrays.asList(pa), agentCluster.agentsWithFeature("pf"));
        assertEquals(Arrays.asList(ta, pa), agentCluster.agentsWithFeature("gf"));
    }
}