/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller;

import java.util.Collection;

import com.sun.jersey.api.client.Client;

import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;

/**
 * @version $Revision: 1.1 $
 */
public class AgentTest extends RuntimeTestSupport {
    protected InstallerAgent agent = new InstallerAgent();
    protected RestGridClient adminClient = new RestGridClient();

    public void testMachineKeepAlive() throws Exception {
        agent.call();
        agent.call();
        agent.call();
        agent.call();

        // now lets test that this machine turns up in the list of available machines
        Collection<AgentDetails> list = adminClient.getAllAgentDetails();
        int size = list.size();
        assertTrue("Should not be empty!", size > 0);


        Client client = new Client();
        String xml = client.resource(adminClient.getAgentsUri()).accept("text/xml").get(String.class);
        System.out.println("XMl: " + xml);

        String hostname = agent.getAgentDetails().getHostname();

        AgentDetails localMachine = null;
        for (AgentDetails machine : list) {
            System.out.println("Machine: " + machine);
            if (hostname.equals(machine.getHostname())) {
                if (localMachine == null) {
                    localMachine = machine;
                } else {
                    fail("Found two machines with the same hostname: " + hostname + " in list: " + list);
                }
            }
        }

        assertNotNull("Should have found a local machine!", localMachine != null);

        System.out.println("Local machine: " + localMachine);


        // now lets sleep to force the timeout to kick in
        Thread.sleep(5000L);

        // now the lack of keep alives should have caused the machine to close
        list = adminClient.getAllAgentDetails();
        assertEquals("We should now have one less machines!", size - 1, list.size());
    }

}
