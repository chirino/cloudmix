/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent;

import java.io.File;
import java.io.FileInputStream;
import java.net.Inet4Address;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;
import org.fusesource.cloudmix.agent.InstallerAgent;
import org.easymock.EasyMock;

public class InstallerAgentTest extends TestCase {
    public void testMaxProcessesAccessor() {
        InstallerAgent ia = new InstallerAgent();
        assertEquals(1, ia.getMaxFeatures());
        
        ia.setMaxFeatures(500);
        assertEquals(500, ia.getMaxFeatures());
    }
    
    public void testProfileName() {
        InstallerAgent ia = new InstallerAgent();
        assertEquals("default", ia.getProfile());
        
        ia.setProfile("testing");
        assertEquals("testing", ia.getProfile());
    }
    
    public void testLoadPersistedAgentConfig() throws Exception {
        
        File propFile = new File(this.getClass().getResource("/testAgentProps.properties").toURI());

        InstallerAgent agent = new InstallerAgent();
        agent.setDetailsPropertyFilePath(propFile.toString());
        
        AgentDetails details = agent.getAgentDetails();

        assertEquals("007", agent.getAgentId());
        assertEquals("kill", agent.getProfile());
        assertEquals("bond", details.getName());
        
        assertEquals(Inet4Address.getLocalHost().getCanonicalHostName(), details.getHostname());
        assertEquals(System.getProperty("os.name"), details.getOs());    
        assertEquals(System.getProperty("os.name"), details.getSystemProperties().get("os.name"));
    }

    public void testPersistAgentConfig() throws Exception {
        
        File propFile = new File("testPersistAgentConfig.properties");

        if (propFile.exists()) {
            propFile.delete();
        }
        
        try {
            InstallerAgent agent = new InstallerAgent() {
                protected String addToClient(AgentDetails details) throws URISyntaxException {
                    return "someGeneratedId";
                }
            };

            agent.setDetailsPropertyFilePath(propFile.toString());
            
            AgentDetails details = agent.getAgentDetails();

            
            String generatedId = agent.getAgentId();
            assertEquals("someGeneratedId", generatedId);
            assertEquals("default", agent.getProfile());
            assertNull(agent.getAgentName());
            
            agent.setProfile("kill");
            agent.setAgentName("bond");
            
            agent.persistAgentDetails();
            
            Properties props = new Properties();
            props.load(new FileInputStream(propFile));

            assertEquals("kill", props.getProperty(InstallerAgent.PERSISTABLE_PROPERTY_PROFILE_ID));
            assertEquals("bond", props.getProperty(InstallerAgent.PERSISTABLE_PROPERTY_AGENT_NAME));
            assertEquals(generatedId, props.getProperty(InstallerAgent.PERSISTABLE_PROPERTY_AGENT_ID));

            // check that it can reload...
            agent = new InstallerAgent();
            agent.setDetailsPropertyFilePath(propFile.toString());
            
            details = agent.getAgentDetails();
            
            assertEquals(generatedId, agent.getAgentId());
            assertEquals("kill", agent.getProfile());
            assertEquals("bond", agent.getAgentName());
            
            assertEquals(Inet4Address.getLocalHost().getCanonicalHostName(), details.getHostname());
            assertEquals(System.getProperty("os.name"), details.getOs());    
            assertEquals(System.getProperty("os.name"), details.getSystemProperties().get("os.name"));
        
        } finally {
            if (propFile.exists()) {
                propFile.delete();
            }
        }
        
    }

    
    public void testPopulateInitialAgentDetailsThenUpdate() throws Exception {
        InstallerAgent ia = new InstallerAgent();
        GridClient client = EasyMock.createMock(GridClient.class);
        client.updateAgentDetails((String) EasyMock.anyObject(), (AgentDetails)EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(client.addAgentDetails((AgentDetails)EasyMock.anyObject())).andReturn("").anyTimes();
        EasyMock.replay(client);
        
        ia.setClient(client);
        ia.setMaxFeatures(5);
        ia.setProfile("production");
        
        AgentDetails details = ia.getAgentDetails();
        assertEquals(Inet4Address.getLocalHost().getCanonicalHostName(), details.getHostname());
        assertEquals(5, details.getMaximumFeatures());
        assertEquals("production", details.getProfile());
        assertEquals(System.getProperty("os.name"), details.getOs());    
        assertEquals(System.getProperty("os.name"), details.getSystemProperties().get("os.name"));

        AgentDetails unchangedDetails = ia.updateAgentDetails();
        assertEquals("Nothing should have changed yet",
                     details.getSystemProperties(),
                     unchangedDetails.getSystemProperties());
        
        Properties oldProps = System.getProperties();
        try {
            String v = "" + System.currentTimeMillis();
            System.setProperty("testing", v);
            
            assertNull("Precondition failed", details.getSystemProperties().get("testing"));
            AgentDetails newDetails = ia.updateAgentDetails();
            assertEquals(v, newDetails.getSystemProperties().get("testing"));            
        } finally {
            System.setProperties(oldProps);
        }
        
        EasyMock.verify(client);
    }
    
    public void testEffectiveActions() throws Exception {
        GridClient client = EasyMock.createMock(GridClient.class);
        client.updateAgentDetails((String) EasyMock.anyObject(), (AgentDetails)EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(client.addAgentDetails((AgentDetails)EasyMock.anyObject())).andReturn("").anyTimes();
        EasyMock.replay(client);
        InstallerAgent ia = new InstallerAgent();
        ia.setClient(client);
        
        Map<String, ProvisioningAction> installActions = new HashMap<String, ProvisioningAction>();
        Map<String, ProvisioningAction> uninstallActions = new HashMap<String, ProvisioningAction>();
        
        ia.getEffectiveActions(installActions, uninstallActions);
        assertEquals(0, installActions.size());
        assertEquals(0, uninstallActions.size());
        assertNull(ia.getProvisioningHistory());

        ProvisioningHistory ph = new ProvisioningHistory();
        ia.onProvisioningHistoryChanged(ph);
        assertSame(ph, ia.getProvisioningHistory());
        
        ia.getEffectiveActions(installActions, uninstallActions);
        assertEquals(0, installActions.size());
        assertEquals(0, uninstallActions.size());                
        
        // install f1
        ProvisioningAction a1 = new ProvisioningAction(
                ProvisioningAction.INSTALL_COMMAND, "f1", "http://somewhere1");
        ia.getProvisioningHistory().addAction(a1);
        ia.getEffectiveActions(installActions, uninstallActions);
        assertEquals(1, installActions.size());
        assertEquals("f1", installActions.keySet().iterator().next());
        assertSame(a1, installActions.get("f1"));
        assertEquals(0, uninstallActions.size());
        
        // install f2
        ProvisioningAction a2 = new ProvisioningAction(
                ProvisioningAction.INSTALL_COMMAND, "f2", "http://somewhere2");
        ia.getProvisioningHistory().addAction(a2);
        ia.getEffectiveActions(installActions, uninstallActions);
        assertEquals(2, installActions.size());
        assertSame(a1, installActions.get("f1"));
        assertSame(a2, installActions.get("f2"));
        assertEquals(0, uninstallActions.size());
        
        // uninstall f1, which should not tell me to install any more
        ProvisioningAction a3 = new ProvisioningAction(
                ProvisioningAction.UNINSTALL_COMMAND, "f1", null);
        ia.getProvisioningHistory().addAction(a3);
        ia.getEffectiveActions(installActions, uninstallActions);
        assertEquals(1, installActions.size());
        assertEquals("f2", installActions.keySet().iterator().next());
        assertSame(a2, installActions.get("f2"));
        assertEquals(1, uninstallActions.size());
        assertEquals("f1", uninstallActions.keySet().iterator().next());
        assertSame(a3, uninstallActions.get("f1"));
        
        // install f1 again, which means it should not tell me to uninstall any more
        ProvisioningAction a4 = new ProvisioningAction(
                ProvisioningAction.INSTALL_COMMAND, "f1", "http://somewhere1");
        ia.getProvisioningHistory().addAction(a4);
        ia.getEffectiveActions(installActions, uninstallActions);
        assertEquals(2, installActions.size());
        assertSame(a4, installActions.get("f1"));
        assertSame(a2, installActions.get("f2"));
        assertEquals(0, uninstallActions.size());      
        
        EasyMock.verify(client);
    }
}
