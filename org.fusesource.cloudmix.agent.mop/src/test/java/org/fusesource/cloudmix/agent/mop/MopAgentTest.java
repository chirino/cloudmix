/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent.mop;

import junit.framework.TestCase;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.agent.AgentPoller;

import java.net.URISyntaxException;

/**
 * @version $Revision: 1.1 $
 */
public class MopAgentTest extends TestCase {
    protected MopAgent agent = new MopAgent();
    protected String version = "1.3-SNAPSHOT";
    protected RestGridClient client;
    protected AgentPoller poller = new AgentPoller();

    public void testMultipleConcurrentFeatures() throws Exception {

        installFeature("jar org.fusesource.cloudmix:org.fusesource.cloudmix.tests.broker:");
        installFeature("jar org.fusesource.cloudmix:org.fusesource.cloudmix.tests.producer:");
        installFeature("jar org.fusesource.cloudmix:org.fusesource.cloudmix.tests.consumer:");

        Thread.sleep(20 * 60 * 1000);
    }

    protected void installFeature(String mopCommand) throws Exception {
        agent.installMopFeature(mopCommand + version);
    }

    @Override
    protected void setUp() throws Exception {
        if (client == null) {
            client = createGridClient();
        }
        agent.setClient(client);
        agent.init();
        poller.setAgent(agent);
    }

    protected RestGridClient createGridClient() throws URISyntaxException {
        return new RestGridClient();
    }
}
