/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
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
 * @version $Revision$
 */
public class ProvisioningTestSupport extends RuntimeTestSupport {
    protected GridControllerClient gridController;
    protected AgentCluster agentCluster;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        gridController = new GridControllerClient();
        agentCluster = new AgentCluster();
        agentCluster.afterPropertiesSet();
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
        return agent.getInstalledActions().values();
    }    
}
