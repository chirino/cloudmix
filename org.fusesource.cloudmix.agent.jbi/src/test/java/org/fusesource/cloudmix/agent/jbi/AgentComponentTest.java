package org.fusesource.cloudmix.agent.jbi;

import javax.jbi.component.ComponentContext;
import javax.jbi.management.MBeanNames;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.easymock.EasyMock;

public class AgentComponentTest extends TestCase {
    
    
    public void testAgentComponent() throws Exception {
        
        MBeanServer mbeanServer = EasyMock.createMock(MBeanServer.class);
        MBeanInfo info = new MBeanInfo("foo", "bar", null, null, null, null) {            
        };
        MBeanNames names = new MBeanNames() {
            public ObjectName createCustomComponentMBeanName(String n) {
                return null;
            }
            public String getJmxDomainName() {
                return "com.example";
            }            
        };
        AgentComponent comp = new AgentComponent();
        ComponentContext ctx = EasyMock.createMock(ComponentContext.class);
        
        EasyMock.expect(ctx.getMBeanServer()).andReturn(mbeanServer).anyTimes();
        EasyMock.expect(ctx.getMBeanNames()).andReturn(names).anyTimes();
        EasyMock.replay(ctx);
        EasyMock.expect(mbeanServer.getMBeanInfo((ObjectName)EasyMock.anyObject()))
            .andReturn(info).anyTimes();
        EasyMock.expect(mbeanServer.registerMBean(EasyMock.anyObject(),
                                                  (ObjectName)EasyMock.anyObject())).andReturn(null);
        mbeanServer.unregisterMBean((ObjectName)EasyMock.anyObject());
        EasyMock.replay(mbeanServer);
        
        comp.init(ctx);
        comp.start();
        comp.stop();
        comp.shutDown();
        
        assertNull(comp.getExtensionMBeanName());
        assertNotNull(comp.getLifeCycle());
        assertNull(comp.getServiceDescription(null));
        assertNull(comp.getServiceUnitManager());
        assertFalse(comp.isExchangeWithConsumerOkay(null, null));
        assertFalse(comp.isExchangeWithProviderOkay(null, null));
        assertNull(comp.resolveEndpointReference(null));
        

        EasyMock.verify(ctx);
        EasyMock.verify(mbeanServer);
    }

}
