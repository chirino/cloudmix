/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
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