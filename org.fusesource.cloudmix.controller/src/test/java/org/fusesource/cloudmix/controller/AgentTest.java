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

import java.util.Collection;

import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;

/**
 * @version $Revision: 1.1 $
 */
public class AgentTest extends RuntimeTestSupport {
    protected InstallerAgent agent = new InstallerAgent();
    protected GridClient adminClient = new RestGridClient();

    public void testMachineKeepAlive() throws Exception {
        agent.call();
        agent.call();
        agent.call();
        agent.call();

        // now lets test that this machine turns up in the list of available machines
        Collection<AgentDetails> list = adminClient.getAllAgentDetails();
        int size = list.size();
        assertTrue("Should not be empty!", size > 0);

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
