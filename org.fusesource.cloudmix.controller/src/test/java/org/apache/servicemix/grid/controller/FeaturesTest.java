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
package org.apache.servicemix.grid.controller;

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