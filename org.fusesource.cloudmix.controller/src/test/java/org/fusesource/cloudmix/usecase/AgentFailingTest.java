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
package org.fusesource.cloudmix.usecase;

import java.util.List;

import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.controller.provisioning.ProvisioningTestSupport;

/**
 * @version $Revision: 1.1 $
 */
public class AgentFailingTest extends ProvisioningTestSupport {
    public void testProvision() throws Exception {
        agentCluster.createInstallAgents(4); // 4

        waitForAgentsToActivate();

        FeatureDetails broker = new FeatureDetails("activeMQBroker").ownsMachine();
        FeatureDetails producer = new FeatureDetails("producer").depends(broker);
        FeatureDetails consumer = new FeatureDetails("consumer").depends(broker);

        gridController.addFeatures(broker, producer, consumer);
        gridController.addProfiles(getDefaultProfileWithAllFeatures(gridController));

        waitForFeaturesToProvision();

        agentCluster.dumpAgents();
        agentCluster.assertFeatureInstances("activeMQBroker", 1);
        agentCluster.assertFeatureInstances("producer", 1);
        agentCluster.assertFeatureInstances("consumer", 1);
        agentCluster.assertMaximumFeaturesPerAgent(1);

        // now lets kill an agent
        List<InstallerAgent> agents = agentCluster.agentsWithFeature("activeMQBroker");
        assertEquals("Agents running broker", 1, agents.size());
        agentCluster.removeAgents(agents);

        waitForFeaturesToProvision();

        agentCluster.dumpAgents();
        agentCluster.assertFeatureInstances("activeMQBroker", 1);
        agentCluster.assertFeatureInstances("producer", 1);
        agentCluster.assertFeatureInstances("consumer", 1);
        agentCluster.assertMaximumFeaturesPerAgent(1);
    }
}