package org.fusesource.cloudmix.agent;


import javax.xml.ws.wsaddressing.W3CEndpointReference;

import junit.framework.TestCase;

import org.fusesource.cloudmix.agent.EndpointRegistry;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.agent.common.EndpointReferenceBuilder;
import org.fusesource.cloudmix.agent.InstallerAgent;

import org.fusesource.cloudmix.common.dto.AgentDetails;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class EndpointRegistryTest extends TestCase {

    private IMocksControl control;
    private InstallerAgent agent; 
    private AgentDetails details;
    private RestGridClient client;

    public void testAddEndpoint() throws Exception {
        EndpointRegistry registry = setUpRegistry();
        String id = "urn:{http://cxf.apache.org}SoapPort";
        String encodedId = "urn:%7Bhttp:%2F%2Fcxf.apache.org%7DSoapPort";
        W3CEndpointReference ref = 
            EndpointReferenceBuilder.create("http://tempuri.org/foo/bar");
        String agentId = "localhost_agent_1";

        agent.getAgentId();
        EasyMock.expectLastCall().andReturn(agentId);
        details.addEndpoint(id, ref);
        EasyMock.expectLastCall();
        registry.getClient().updateAgentDetails(agentId, details);
        EasyMock.expectLastCall();

        control.replay();
        
        registry.addEndpoint(id, ref);
       
        control.verify();
    }

    public void testRemoveEndpoint() throws Exception {
        EndpointRegistry registry = setUpRegistry();
        String id = "urn:{http://cxf.apache.org}SoapPort";
        String agentId = "localhost_agent_1";

        agent.getAgentId();
        EasyMock.expectLastCall().andReturn(agentId);
        details.removeEndpoint(id);
        EasyMock.expectLastCall().andReturn(true);
        client.updateAgentDetails(agentId, details);
        EasyMock.expectLastCall();
        control.replay();
        
        assertTrue(registry.removeEndpoint(id));
       
        control.verify();
    }

    public void testRemoveNonExistentEndpoint() throws Exception {
        EndpointRegistry registry = setUpRegistry();
        String id = "urn:{http://cxf.apache.org}SoapPort";

        details.removeEndpoint(id);
        EasyMock.expectLastCall().andReturn(false);
        control.replay();
        
        assertFalse(registry.removeEndpoint(id));
       
        control.verify();
    }

    private EndpointRegistry setUpRegistry() {
        control = EasyMock.createNiceControl();
        agent = control.createMock(InstallerAgent.class);
        details = control.createMock(AgentDetails.class);
        client = control.createMock(RestGridClient.class);

        agent.getAgentDetails();
        EasyMock.expectLastCall().andReturn(details).anyTimes();

        EndpointRegistry endpointRegistry = new EndpointRegistry();
        endpointRegistry.setClient(client);
        endpointRegistry.setAgent(agent);
        return endpointRegistry;
    }
}
