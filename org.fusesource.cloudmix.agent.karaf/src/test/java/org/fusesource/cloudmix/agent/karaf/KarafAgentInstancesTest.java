/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.karaf;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.felix.karaf.gshell.admin.AdminService;
import org.apache.felix.karaf.gshell.admin.Instance;
import org.easymock.EasyMock;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.common.util.FileUtils;

/**
 * Test cases for using the Karaf agent to create new Karaf instances on the fly
 */
public class KarafAgentInstancesTest extends TestCase {

	private GridClient cl;
    private AdminService service;
    private KarafAgent agent;
    private List<Feature> agentFeatures = new LinkedList<Feature>();
    
    private File workdir;
    
    @Override
    protected void setUp() throws Exception {
        cl = EasyMock.createNiceMock(GridClient.class);
        EasyMock.replay(cl);
        
        agent = new KarafAgent() {
            @Override
            public GridClient getClient() {
                return cl;
            }            
            @Override
            protected void addAgentFeature(Feature feature) {
            	super.addAgentFeature(feature);
            	agentFeatures.add(feature);
            }
        };
        workdir = new File("testworkdir");
        FileUtils.createDirectory(workdir);
        
        service = new MockAdminService();
        
        agent.setAdminService(service);
        agent.setWorkDirectory(workdir);
        agent.setDetailsPropertyFilePath(workdir.getAbsolutePath() + "/agent.properties");
    }

    @Override
    protected void tearDown() throws Exception {    
        if (workdir != null) {
            FileUtils.deleteDirectory(workdir);
        }
    }

    public void testCreateNewInstance() throws Exception {
    	ProvisioningAction action = new ProvisioningAction();
    	action.setId("my-action-id");
    	action.setFeature("my-feature-name");
    	
		agent.installFeatures(action, null, "karaf:instance");
		assertEquals("One instance should have been created", 1, service.getInstances().length);
		assertEquals("One agent feature should have registered", 1, agentFeatures.size());
		
		Instance instance = service.getInstances()[0];
		assertEquals("Instance should have been started", Instance.STARTED, instance.getState());
		
		// now, let's uninstall the agent feature to destroy the instance
		agent.uninstallFeature(agentFeatures.get(0));
		assertEquals("Instance should have been stopped", Instance.STOPPED, instance.getState());
		assertEquals("Instance should have been destroyed", 0, service.getInstances().length);
	}
    
    public void testGetInstanceName() throws Exception {
		assertEquals("Replace : by _ when determining the instance name", 
				     "ceffbab3-bc3d-4c72-a89c-154ab2052971_my-feature",
				     agent.getInstanceName("ceffbab3-bc3d-4c72-a89c-154ab2052971:my-feature"));
	}
    
    /*
     * Mock AdminService implementation
     */
    private class MockAdminService implements AdminService {
    	
    	private Map<String, Instance> instances = new HashMap<String, Instance>();

		public Instance createInstance(final String name, int port, String location) throws Exception {
			assertEquals("The agent should not assign a port number", 0, port);
			assertNull("The agent should not assign a location", location);
			
			Instance instance = new Instance() {
				
				private String state;

				public void stop() throws Exception {
					state = STOPPED;
				}
				
				public void start(String arg0) throws Exception {
					state = STARTED;
				}
				
				public String getState() throws Exception {
					return state;
				}
				
				public int getPort() {
					return 0;
				}
				
				public int getPid() {
					return 0;
				}
				
				public String getName() {
					return name;
				}
				
				public String getLocation() {
					return null;
				}
				
				public void destroy() throws Exception {
					instances.remove(getName());
				}
				
				public void changePort(int arg0) throws Exception {
					throw new UnsupportedOperationException("Not implemented");
				}
			};
			instances.put(name, instance);
			return instance;
		}

		public Instance getInstance(String name) {
			return instances.get(name);
		}

		public Instance[] getInstances() {
			return instances.values().toArray(new Instance[] {});
		}
    }
}
