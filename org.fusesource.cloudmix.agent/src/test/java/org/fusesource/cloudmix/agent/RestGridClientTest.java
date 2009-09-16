/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.net.URI;

import junit.framework.TestCase;

import com.sun.jersey.api.client.WebResource.Builder;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.fusesource.cloudmix.common.CloudmixHelper;
import org.fusesource.cloudmix.common.dto.AgentDetails;

public class RestGridClientTest extends TestCase {
    protected String rootUrl = CloudmixHelper.getDefaultRootUrl();

    public void testGetAgentsUri1() throws Exception {
        RestGridClient rgc = new RestGridClient();
        assertEquals(rootUrl + "agents", rgc.getAgentsUri().toString());
    }

    public void testGetAgentsUri2() throws Exception {
        RestGridClient rgc = new RestGridClient();        
        rgc.setRootUri(new URI("http://localhost:9090/controller"));
        assertEquals("http://localhost:9090/controller/agents", rgc.getAgentsUri().toString());        
    }
    
    public void testGetAgentsUri3() throws Exception {
        RestGridClient rgc = new RestGridClient();        
        rgc.setRootUri(new URI("http://localhost:9090/controller/"));
        assertEquals("http://localhost:9090/controller/agents", rgc.getAgentsUri().toString());        
    }
    
    public void testGetFeaturesUri1() throws Exception {
        RestGridClient rgc = new RestGridClient();
        assertEquals(rootUrl + "features", rgc.getFeaturesUri().toString());
    }

    public void testGetFeaturesUri2() throws Exception {
        RestGridClient rgc = new RestGridClient();        
        rgc.setRootUri(new URI("http://localhost:9090/controller"));
        assertEquals("http://localhost:9090/controller/features", rgc.getFeaturesUri().toString());        
    }
    
    public void testGetFeaturesUri3() throws Exception {
        RestGridClient rgc = new RestGridClient();        
        rgc.setRootUri(new URI("http://localhost:9090/controller/"));
        assertEquals("http://localhost:9090/controller/features", rgc.getFeaturesUri().toString());        
    }
    
    public void testProfilesUri() throws Exception {
        RestGridClient rgc = new RestGridClient();
        rgc.setRootUri(new URI(rootUrl));
        assertEquals(rootUrl + "profiles", rgc.getProfilesUri().toString());
    }
    
    public void testUpdateAgentDetails() throws Exception {
        final AgentDetails ad = new AgentDetails();
        RestTemplate rt = EasyMock.createMock(RestTemplate.class);
        rt.put((Builder) EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                assertSame(ad, EasyMock.getCurrentArguments()[1]);
                return null;
            }            
        });
        EasyMock.replay(rt);
        
        RestGridClient rgc = new RestGridClient();
        rgc.setTemplate(rt);
        rgc.setRootUri(new URI(rootUrl));
        rgc.updateAgentDetails("a1", ad);
        EasyMock.verify(rt);
    }
}
