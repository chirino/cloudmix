package org.apache.servicemix.grid.agent;

import java.io.File;
import java.util.Date;

import org.apache.servicemix.grid.common.util.FileUtils;

import junit.framework.TestCase;

public class AgentStateTest extends TestCase {

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
            assertEquals(0, agentProperties.keySet().size());
        }
    
        public void assertInitState() throws Exception {
            assertEquals(2, agentProperties.keySet().size());
            assertTrue(agentProperties.get("started") instanceof Date);
            assertTrue(agentProperties.get("created") instanceof Date);
        }
        
        public void testCreateState() throws Exception {
            Object o = agentProperties.get("keyA");
            assertNull(o);
            agentProperties.put("keyA", "ValueA");
            
            o = agentProperties.get("KeyB");
            assertNull(o);            
            agentProperties.put("KeyB", new Integer(1234));
            
            assertStateExists();
        }
        
        public void assertStateExists() {
            Object o = agentProperties.get("keyA");
            assertNotNull(o);
            String s = (String) o;
            assertEquals("ValueA", s);

            o = agentProperties.get("KeyB");
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
