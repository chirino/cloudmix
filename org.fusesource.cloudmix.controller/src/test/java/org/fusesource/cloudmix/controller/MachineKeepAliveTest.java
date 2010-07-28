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
 * @version $Revision$
 */
public class MachineKeepAliveTest extends RuntimeTestSupport {
    protected Client client;

    public void testGetMachines() throws Exception {
        WebResource.Builder agentsResource =
            client.resource(getRootUrl() + "agents").accept("application/xml");
        ClientResponse response = agentsResource.get(ClientResponse.class);

        LOG.info("Response: " + response);
        LOG.info("Status: " + response.getStatus());
        LOG.info("Type: " + response.getType());
        LOG.info("EntityTag: " + response.getEntityTag());


        AgentDetailsList list = response.getEntity(AgentDetailsList.class);
        assertNotNull("Should receive a machines list", list);
        LOG.info("List: " + list);
    }

    public void testKeepingMachineAlive() throws Exception {
        WebResource.Builder agentsResource =
            client.resource(getRootUrl() + "agents").type("application/xml");

        AgentDetails details = new AgentDetails();
        ClientResponse response = agentsResource.post(ClientResponse.class, details);
        LOG.info("Received status: " + response.getStatus());

        URI location = new URI(response.getLocation() + "/history");
        assertNotNull("Should have a location!", location);

        LOG.info("Now polling: " + location);
        
        // now lets get the document from the location
        ProvisioningHistory history =
            client.resource(location).accept("application/xml").get(ProvisioningHistory.class);
        assertNotNull(history);

        LOG.info("Received the new history: " + history);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = Client.create();
    }
}
