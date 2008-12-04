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
