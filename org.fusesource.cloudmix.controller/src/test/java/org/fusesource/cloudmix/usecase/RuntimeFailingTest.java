/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.usecase;

import java.util.List;

import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.controller.provisioning.ProvisioningTestSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

/**
 * Tests the runtime (server side) failing over to a new instance and continuing to function
 *
 * @version $Revision: 1.1 $
 */
public class RuntimeFailingTest extends ProvisioningTestSupport {

    public void testProvidion() throws Exception {
        // lets not kill the broker
        assertProvisioningAfterServerRestart(false);
    }

    protected void assertProvisioningAfterServerRestart(boolean killBroker) throws Exception {
        agentCluster.createInstallAgents("host1", "host2", "host3", "host4");

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

        String brokerHostName = agentCluster.firstAgentHostNameForFeature("activeMQBroker");
        String producerHostName = agentCluster.firstAgentHostNameForFeature("producer");
        String consumerHostName = agentCluster.firstAgentHostNameForFeature("consumer");

        // now lets kill an agent
        if (killBroker) {
            List<InstallerAgent> agents = agentCluster.agentsWithFeature("activeMQBroker");
            assertEquals("Agents running broker", 1, agents.size());
            agentCluster.removeAgents(agents);
        }
        restartWebServer();

        // TODO this test doesn't have HA FeatureDetails but that could be in a
        // database or read from some rsync based file system etc
        gridController.addFeatures(broker, producer, consumer);
        gridController.addProfiles(getDefaultProfileWithAllFeatures(gridController));

        waitForFeaturesToProvision();

        agentCluster.dumpAgents();
        agentCluster.assertFeatureInstances("activeMQBroker", 1);
        agentCluster.assertFeatureInstances("producer", 1);
        agentCluster.assertFeatureInstances("consumer", 1);
        agentCluster.assertMaximumFeaturesPerAgent(1);

        // lets assert that the same machines have the same features

        agentCluster.assertFirstAgentHostNameForFeature("producer", producerHostName);
        agentCluster.assertFirstAgentHostNameForFeature("consumer", consumerHostName);

        if (killBroker) {
            String newBrokerHostName = agentCluster.firstAgentHostNameForFeature("activeMQBroker");
            assertThat(newBrokerHostName, not(brokerHostName));
        } else {
            agentCluster.assertFirstAgentHostNameForFeature("activeMQBroker", brokerHostName);
        }
    }
}