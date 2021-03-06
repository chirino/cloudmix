/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.util.FileUtils;

public class AgentStateTest extends TestCase {
    @Override
    protected void tearDown() {
        File tmpWorkDirectory = new File("tmp.work.dir");
        FileUtils.deleteDirectory(tmpWorkDirectory);        
    }

    class TestAgent extends InstallerAgent {

        public TestAgent() {
            // Complete.
        }        
        
        @Override
        protected boolean installBundle(Feature feature, Bundle bundle) {
            return false;
        }
        @Override
        protected boolean uninstallBundle(Feature feature, Bundle bundle) {
            return false;
        }
        @Override
        protected boolean validateAgent() {
            return false;
        } 
        
        public void assertEmptyState() throws Exception {
            assertEquals(0, agentState.getAgentProperties().keySet().size());
            assertEquals(0, agentState.getAgentFeatures().keySet().size());
        }
    
        public void assertInitState() throws Exception {
            assertEquals(2, agentState.getAgentProperties().keySet().size());
            assertTrue(agentState.getAgentProperties()
                           .get("org.fusesource.cloudmix.agent.InstallerAgent.started") instanceof Date);
            assertTrue(agentState.getAgentProperties()
                           .get("org.fusesource.cloudmix.agent.InstallerAgent.created") instanceof Date);
        }
        
        public void testCreateState() throws Exception {
            Object o = agentState.getAgentProperties().get("keyA");
            assertNull(o);
            agentState.getAgentProperties().put("keyA", "ValueA");
            
            o = agentState.getAgentProperties().get("KeyB");
            assertNull(o);            
            agentState.getAgentProperties().put("KeyB", new Integer(1234));
            
            assertStateExists();
        }
        
        public void assertStateExists() {
            Object o = agentState.getAgentProperties().get("keyA");
            assertNotNull(o);
            String s = (String) o;
            assertEquals("ValueA", s);

            o = agentState.getAgentProperties().get("KeyB");
            assertNotNull(o);
            Integer i = (Integer) o;
            assertEquals(1234, i.intValue());           
        }
    };
    
    public void testAgentState() throws Exception {
        
        File tmpWorkDirectory = new File("tmp.work.dir");
        FileUtils.deleteDirectory(tmpWorkDirectory);
        
        TestAgent agent1 = new TestAgent();
        
        agent1.init();
        agent1.assertEmptyState();
        agent1.testCreateState();
        agent1.persistState();
        assertFalse(tmpWorkDirectory.exists());

        TestAgent agent2 = new TestAgent();
        agent2.setWorkDirectory(tmpWorkDirectory);
        agent2.init();
        agent2.assertInitState();
        agent2.testCreateState();
        agent2.persistState();
        assertTrue(tmpWorkDirectory.exists());
        
        TestAgent agent3 = new TestAgent();
        agent3.assertEmptyState();
        agent3.loadState();
        agent3.assertEmptyState();
        
        TestAgent agent4 = new TestAgent();
        agent4.setWorkDirectory(tmpWorkDirectory);
        agent4.assertEmptyState();
        agent4.loadState();
        agent4.assertStateExists();
    }
}
