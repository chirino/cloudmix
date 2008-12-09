/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller;

import java.util.List;

import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;

/**
 * @version $Revision: 1.1 $
 */
public class FeaturesTest extends RuntimeTestSupport {
    protected GridClient adminClient = new RestGridClient();

    public void testAddingAndRemovingFeatures() throws Exception {
        assertFeatureSize(0, adminClient.getFeatures());

        adminClient.addFeature(new FeatureDetails("activemqBroker-5.0.0.9"));
        assertFeatureSize(1, adminClient.getFeatures());

        adminClient.addFeature(new FeatureDetails("camelRouter-1.3.2.0"));
        assertFeatureSize(2, adminClient.getFeatures());

        List<String> agentsAssignedToFeature = adminClient.getAgentsAssignedToFeature("camelRouter-1.3.2.0");
        assertEquals(0, agentsAssignedToFeature.size());
        
        // Add an agent so that we can assign a feature to him
        AgentDetails agent1 = new AgentDetails();
        agent1.setId(adminClient.addAgentDetails(agent1));
        
        adminClient.addAgentToFeature("camelRouter-1.3.2.0", agent1.getId(), null);
        
        agentsAssignedToFeature = adminClient.getAgentsAssignedToFeature("camelRouter-1.3.2.0");
        assertEquals(1, agentsAssignedToFeature.size());

        adminClient.removeFeature("activemqBroker-5.0.0.9");
        adminClient.removeFeature("camelRouter-1.3.2.0");
        assertFeatureSize(0, adminClient.getFeatures());
    }

    protected void assertFeatureSize(int expectedSize, List<FeatureDetails> list) {
        assertEquals("List size: " + list, expectedSize, list.size());
    }
}