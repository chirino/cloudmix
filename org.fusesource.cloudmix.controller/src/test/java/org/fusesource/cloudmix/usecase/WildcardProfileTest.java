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
import org.fusesource.cloudmix.common.dto.Constants;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.controller.provisioning.ProvisioningTestSupport;

/**
 * @version $Revision$
 */
public class WildcardProfileTest extends ProvisioningTestSupport {
    public void testProvision() throws Exception {
        List<InstallerAgent> agents = agentCluster.createInstallAgents("host1", "host2", "host3");
        for (InstallerAgent ia : agentCluster.getAgents()) {
            ia.setMaxFeatures(500);
            ia.setProfile(Constants.WILDCARD_PROFILE_NAME);
        }

        waitForAgentsToActivate();

        FeatureDetails broker = new FeatureDetails("activeMQBroker").preferredMachine("host1").ownsMachine();
        FeatureDetails producer = new FeatureDetails("producer").depends(broker).maximumInstances("2");
        FeatureDetails consumer = new FeatureDetails("consumer").depends(broker).maximumInstances("3");

        gridController.addFeatures(broker, producer, consumer);
        gridController.addProfiles(getDefaultProfileWithAllFeatures(gridController));

        waitForFeaturesToProvision();

        agentCluster.dumpAgents();
        agentCluster.assertFeatureInstances("activeMQBroker", 1);
        agentCluster.assertFeatureInstances("producer", 2);
        agentCluster.assertFeatureInstances("consumer", 2);
        agentCluster.assertMaximumFeaturesPerAgent(2);

        InstallerAgent host = agents.get(0);
        agentCluster.assertFeatureCount(host, "activeMQBroker", 1);
        agentCluster.assertFeatureCount(host, "producer", 0);
        agentCluster.assertFeatureCount(host, "consumer", 0);

        host = agents.get(1);
        agentCluster.assertFeatureCount(host, "activeMQBroker", 0);
        agentCluster.assertFeatureCount(host, "producer", 1);
        agentCluster.assertFeatureCount(host, "consumer", 1);

        host = agents.get(2);
        agentCluster.assertFeatureCount(host, "activeMQBroker", 0);
        agentCluster.assertFeatureCount(host, "producer", 1);
        agentCluster.assertFeatureCount(host, "consumer", 1);
    }
}