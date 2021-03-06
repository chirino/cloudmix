/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.usecase;

import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.controller.provisioning.ProvisioningTestSupport;

/**
 * @version $Revision$
 */
public class BrokerConsumerProducerTest extends ProvisioningTestSupport {
    public void testOneFeaturePerBox() throws Exception {
        agentCluster.createInstallAgents(6);

        waitForAgentsToActivate();

        FeatureDetails broker = new FeatureDetails("activeMQBroker").ownsMachine();
        FeatureDetails producer = new FeatureDetails("producer").depends(broker).maximumInstances("2");
        FeatureDetails consumer = new FeatureDetails("consumer").depends(broker).maximumInstances("3");

        gridController.addFeatures(broker, producer, consumer);        
        gridController.addProfiles(getDefaultProfileWithAllFeatures(gridController));

        waitForFeaturesToProvision();

        agentCluster.dumpAgents();
        agentCluster.assertFeatureInstances("activeMQBroker", 1);
        agentCluster.assertFeatureInstances("producer", 2);
        agentCluster.assertFeatureInstances("consumer", 3);
        agentCluster.assertMaximumFeaturesPerAgent(1);
    }

    public void testThreeBoxes() throws Exception {
        agentCluster.createInstallAgents(3);
        for (InstallerAgent ia : agentCluster.getAgents()) {
            ia.setMaxFeatures(500);
        }

        waitForAgentsToActivate();

        FeatureDetails broker = new FeatureDetails("activeMQBroker");
        FeatureDetails producer = new FeatureDetails("producer").depends(broker).maximumInstances("2");
        FeatureDetails consumer = new FeatureDetails("consumer").depends(broker).maximumInstances("3");

        gridController.addFeatures(broker, producer, consumer);
        gridController.addProfiles(getDefaultProfileWithAllFeatures(gridController));

        waitForFeaturesToProvision();

        agentCluster.dumpAgents();
        agentCluster.assertFeatureInstances("activeMQBroker", 1);
        agentCluster.assertFeatureInstances("producer", 2);
        agentCluster.assertFeatureInstances("consumer", 3);
        agentCluster.assertMaximumFeaturesPerAgent(2);
    }

    public void testThreeBoxesWithBrokerOwningBox() throws Exception {
        agentCluster.createInstallAgents(3);
        for (InstallerAgent ia : agentCluster.getAgents()) {
            ia.setMaxFeatures(2);
        }

        waitForAgentsToActivate();

        FeatureDetails broker = new FeatureDetails("activeMQBroker").ownsMachine();
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
    }

    public void testTwoBoxes() throws Exception {
        agentCluster.createInstallAgents(2);
        for (InstallerAgent ia : agentCluster.getAgents()) {
            ia.setMaxFeatures(500);
        }

        waitForAgentsToActivate();

        FeatureDetails broker = new FeatureDetails("activeMQBroker").ownsMachine();
        FeatureDetails producer = new FeatureDetails("producer").depends(broker).maximumInstances("2");
        FeatureDetails consumer = new FeatureDetails("consumer").depends(broker).maximumInstances("3");

        gridController.addFeatures(broker, producer, consumer);
        gridController.addProfiles(getDefaultProfileWithAllFeatures(gridController));

        waitForFeaturesToProvision();

        agentCluster.dumpAgents();
        agentCluster.assertFeatureInstances("activeMQBroker", 1);
        agentCluster.assertFeatureInstances("producer", 1);
        agentCluster.assertFeatureInstances("consumer", 1);
        agentCluster.assertMaximumFeaturesPerAgent(2);
    }    
}
