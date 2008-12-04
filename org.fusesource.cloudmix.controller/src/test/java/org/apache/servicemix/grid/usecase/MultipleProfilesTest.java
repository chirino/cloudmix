package org.apache.servicemix.grid.usecase;

import java.util.Arrays;

import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.apache.servicemix.grid.controller.provisioning.ProvisioningTestSupport;

public class MultipleProfilesTest extends ProvisioningTestSupport {    
    public void testMultipleProfiles() throws Exception {
        // 3 profiles: 'default', 'testing', 'production'
        // 3 features: tf1, tf2 and pf1
        // 6 agents, two per profile
        
        agentCluster.createInstallAgentsInProfiles("default", "default",
                                                   "testing", "testing",
                                                   "production", "production");
        
        waitForAgentsToActivate();
        
        ProfileDetails defProfile = new ProfileDetails("default");
        ProfileDetails testProfile = new ProfileDetails("testing")
                                        .addFeature("tf1", null)
                                        .addFeature("tf2", null);
        ProfileDetails prodProfile = new ProfileDetails("production").addFeature("pf1", null);
        
        FeatureDetails tf1 = new FeatureDetails("tf1");
        FeatureDetails tf2 = new FeatureDetails("tf2");
        FeatureDetails pf1 = new FeatureDetails("pf1");
                
        gridController.addFeatures(tf1, tf2, pf1);
        gridController.addProfiles(defProfile, testProfile, prodProfile);
        
        waitForFeaturesToProvision();
        
        agentCluster.dumpAgents();
        agentCluster.assertFeatureInstances("tf1", 1);
        agentCluster.assertFeatureInstances("tf2", 1);
        agentCluster.assertFeatureInstances("pf1", 1);
        agentCluster.assertMaximumFeaturesPerAgent(1);
                
        
        // Now check that the features got deployed on the correct agents in the 
        // right profile...
        int tf1Count = 0;
        int tf2Count = 0;
        int pf1Count = 0;
        int emptyProdAgentCount = 0;
                
        for (InstallerAgent agent : agentCluster.getAgents()) {
            if (agent.getProfile().equals("default")) {
                assertEquals(0, agentInstallActions(agent).size());
            } else if (agent.getProfile().equals("testing")) {
                assertEquals(1, agentInstallActions(agent).size());

                if (agentFeatureCount(agent, "tf1") == 1) {
                    tf1Count++;
                } else if (agentFeatureCount(agent, "tf2") == 1) {
                    tf2Count++;
                } else {
                    fail("Expected tf1 and tf2 each to be deployed on 1 of the 2 agents in the profile");
                }
            } else if (agent.getProfile().equals("production")) {
                switch (agentInstallActions(agent).size()) {
                case 0:
                    emptyProdAgentCount++;
                    break;
                case 1:
                    if (agentFeatureCount(agent, "pf1") == 1) {
                        pf1Count++;
                    }
                    break;
                default:
                    fail("Unexpected agent in production: " + agent);
                }
            } else {
                fail("Unexpected agent in profile " + agent.getProfile());
            }
        }
        
        assertEquals(1, tf1Count);
        assertEquals(1, tf2Count);
        assertEquals(1, pf1Count);
        assertEquals(1, emptyProdAgentCount);
    }
    
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
