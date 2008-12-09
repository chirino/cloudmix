/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller;

import java.net.URI;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.AgentDetailsList;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;

/**
 * @version $Revision: 1.1 $
 */
public class MachineKeepAliveTest extends RuntimeTestSupport {
    protected Client client;

    public void testGetMachines() throws Exception {
        WebResource.Builder agentsResource =
            client.resource("http://localhost:9091/agents").accept("application/xml");
        ClientResponse response = agentsResource.get(ClientResponse.class);

        System.out.println("Response: " + response);
        System.out.println("Status: " + response.getStatus());
        System.out.println("Type: " + response.getType());
        System.out.println("EntityTag: " + response.getEntityTag());


        AgentDetailsList list = response.getEntity(AgentDetailsList.class);
        assertNotNull("Should receive a machines list", list);
        System.out.println("List: " + list);
    }

    public void testKeepingMachineAlive() throws Exception {
        WebResource.Builder agentsResource =
            client.resource("http://localhost:9091/agents").type("application/xml");

        AgentDetails details = new AgentDetails();
        ClientResponse response = agentsResource.post(ClientResponse.class, details);
        System.out.println("Received status: " + response.getStatus());

        URI location = new URI(response.getLocation() + "/history");
        assertNotNull("Should have a location!", location);

        System.out.println("Now polling: " + location);
        
        // now lets get the document from the location
        ProvisioningHistory history =
            client.resource(location).accept("application/xml").get(ProvisioningHistory.class);
        assertNotNull(history);

        System.out.println("Received the new history: " + history);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = Client.create();
    }
}
