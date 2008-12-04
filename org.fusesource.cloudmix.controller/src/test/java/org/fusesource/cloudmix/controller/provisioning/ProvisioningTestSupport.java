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
package org.fusesource.cloudmix.controller.provisioning;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.fusesource.cloudmix.agent.GridControllerClient;
import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.controller.RuntimeTestSupport;

/**
 * @version $Revision: 1.1 $
 */
public class ProvisioningTestSupport extends RuntimeTestSupport {
    protected GridControllerClient gridController = new GridControllerClient();
    protected AgentCluster agentCluster = new AgentCluster();

    @Override
    protected void setUp() throws Exception {
        agentCluster.afterPropertiesSet();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        agentCluster.destroy();
        super.tearDown();
    }
    
    /**
     * This creates a Profile called 'default' (which is the default profile client look for)
     * with all the features in it that are currently registered in the controller. 
     */
    protected ProfileDetails getDefaultProfileWithAllFeatures(GridControllerClient aGridController)
        throws URISyntaxException {
        
        ProfileDetails defaultProfile = new ProfileDetails("default");        
        for (FeatureDetails feature : aGridController.getClient().getFeatures()) {
            defaultProfile.getFeatures().add(new Dependency(feature.getId()));
        }
        return defaultProfile;
    }        

    protected void waitForAgentsToActivate() throws InterruptedException {
        //Thread.sleep(2000);
    }

    /**
     * Lets wait until features are all deployed
     */
    protected void waitForFeaturesToProvision() throws InterruptedException {
        // TODO we could get smart here and have some kinda flag to indicate that
        // there is gonna be no more provisioning done on the next poll
        Thread.sleep((long) 10000);
        Thread.sleep((long) 2000);
    }
    
    public static int agentFeatureCount(InstallerAgent agent, String featureId) {
        int answer = 0;
        
        for (ProvisioningAction action : agentInstallActions(agent)) {
            if (featureId.equals(action.getFeature())) {
                answer++;
            }
        }
        return answer;
    }
    
    public static Collection<ProvisioningAction> agentInstallActions(InstallerAgent agent) {
        Map<String, ProvisioningAction> installActions = new HashMap<String, ProvisioningAction>();
        agent.getEffectiveActions(installActions, new HashMap<String, ProvisioningAction>());
        return installActions.values();
    }    
}
