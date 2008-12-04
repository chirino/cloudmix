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

import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.controller.provisioning.ProvisioningTestSupport;

/**
 * @version $Revision: 1.1 $
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
