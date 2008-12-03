package org.fusesource.cloudmix.agent.jbi;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;


import org.fusesource.cloudmix.agent.Bundle;
import org.fusesource.cloudmix.agent.Feature;
import org.easymock.EasyMock;

public class JBIInstallerAgentTest extends TestCase {

    // Constants for JMX invocations
    private static final String[] SIGNATURE_VOID = {};
    private static final String[] SIGNATURE_STRING = {"java.lang.String"};
    private static final String[] PARAMS_VOID = {};
    private static final String[] EMPTY_STRING_ARRAY = {};
    
    private static final String TEST_FEATURE_NAME = "f1";
    private static final String TEST_BUNDLE_URI = "http://somewhere.com/b1";
    private static final String TEST_SERVICE_ASSEMBLY_NAME = "b1-sa";

    
    private static final String DEPLOY_SUCCESS =
          "<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\">"
        + "  <jbi-task-result>"
        + "    <frmwk-task-result>"
        + "      <frmwk-task-result-details>"
        + "        <task-result-details>"
        + "          <task-id>deploy</task-id>"
        + "          <task-result>SUCCESS</task-result>"
        + "        </task-result-details>"
        + "      </frmwk-task-result-details>"
        + "    </frmwk-task-result>"
        + "    <component-task-result>"
        + "      <!-- ... -->"
        + "    </component-task-result>"
        + "    <component-task-result>"
        + "      <!-- ... -->"
        + "    </component-task-result>"
        + "  </jbi-task-result>"
        + "</jbi-task>";

    private static final String DEPLOY_FAILURE =
          "<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\">"
        + "  <jbi-task-result>"
        + "    <frmwk-task-result>"
        + "      <frmwk-task-result-details>"
        + "        <task-result-details>"
        + "          <task-id>deploy</task-id>"
        + "          <task-result>FAILED</task-result>"
        + "        </task-result-details>"
        + "      </frmwk-task-result-details>"
        + "    </frmwk-task-result>"
        + "    <component-task-result>"
        + "      <!-- ... -->"
        + "    </component-task-result>"
        + "    <component-task-result>"
        + "      <!-- ... -->"
        + "    </component-task-result>"
        + "  </jbi-task-result>"
        + "</jbi-task>";

    private static final String START_SUCCESS =
        "<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\">"
        + "  <jbi-task-result>"
        + "    <frmwk-task-result>"
        + "      <frmwk-task-result-details>"
        + "        <task-result-details>"
        + "          <task-id>start</task-id>"
        + "          <task-result>SUCCESS</task-result>"
        + "        </task-result-details>"
        + "      </frmwk-task-result-details>"
        + "    </frmwk-task-result>"
        + "  </jbi-task-result>"
        + "</jbi-task>";

    private static final String START_FAILURE =
        "<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\">"
        + "  <jbi-task-result>"
        + "    <frmwk-task-result>"
        + "      <frmwk-task-result-details>"
        + "        <task-result-details>"
        + "          <task-id>start</task-id>"
        + "          <task-result>FAILED</task-result>"
        + "        </task-result-details>"
        + "      </frmwk-task-result-details>"
        + "    </frmwk-task-result>"
        + "    <component-task-result>"
        + "      <!-- ... -->"
        + "    </component-task-result>"
        + "    <component-task-result>"
        + "      <!-- ... -->"
        + "    </component-task-result>"
        + "  </jbi-task-result>"
        + "</jbi-task>";

    
    private static final String SHUTDOWN_SUCCESS =
        "<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\">"
        + "  <jbi-task-result>"
        + "    <frmwk-task-result>"
        + "      <frmwk-task-result-details>"
        + "        <task-result-details>"
        + "          <task-id>shutDown</task-id>"
        + "          <task-result>SUCCESS</task-result>"
        + "        </task-result-details>"
        + "      </frmwk-task-result-details>"
        + "    </frmwk-task-result>"
        + "  </jbi-task-result>"
        + "</jbi-task>";

    private static final String SHUTDOWN_FAILURE =
        "<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\">"
        + "  <jbi-task-result>"
        + "    <frmwk-task-result>"
        + "      <frmwk-task-result-details>"
        + "        <task-result-details>"
        + "          <task-id>shutDown</task-id>"
        + "          <task-result>FAILED</task-result>"
        + "          <message-type>ERROR</message-type>"
        + "          <task-status-msg>"
        + "            <msg-loc-info>"
        + "              <loc-token/>"
        + "                <loc-message>Some error message</loc-message>"
        + "             </msg-loc-info>"
        + "          </task-status-msg>"
        + "        </task-result-details>"
        + "      </frmwk-task-result-details>"
        + "    </frmwk-task-result>"
        + "  </jbi-task-result>"
        + "</jbi-task>";

    private static final String UNDEPLOY_FAILURE =
        "<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\">"
        + "  <jbi-task-result>"
        + "    <frmwk-task-result>"
        + "      <frmwk-task-result-details>"
        + "        <task-result-details>"
        + "          <task-id>undeploy</task-id>"
        + "          <task-result>FAILED</task-result>"
        + "        </task-result-details>"
        + "      </frmwk-task-result-details>"
        + "    </frmwk-task-result>"
        + "  </jbi-task-result>"
        + "</jbi-task>";

    private static final String BAD_XML =
        "<jbi-task xmlns=\"http://java.sun.com/xml/ns/jbi/management-message\">"
        + "  <jbi-task-result>"
        + "    <frmwk-task-result>";


    private ObjectName mbeanName;
    private MBeanServer mbeanServer;
    private JBIInstallerAgent agent;
    private Bundle bundle;
    private Feature feature;

    public void setUp() throws Exception {
        agent = new JBIInstallerAgent();
        mbeanName = new ObjectName("test:Name=Test");        
        mbeanServer = EasyMock.createStrictMock(MBeanServer.class);
        agent.setMBeanName(mbeanName);
        agent.setMBeanServer(mbeanServer);
                
        bundle = new Bundle("", "", TEST_BUNDLE_URI);
        feature = new Feature(TEST_FEATURE_NAME, null);
        feature.addBundle(bundle);
    }

    public void testJBIAgentValidate() throws Exception {
        
        EasyMock.replay(mbeanServer);
        assertTrue(agent.validateAgent());
        EasyMock.verify(mbeanServer);
    }
        
    public void setupDeployBundle() throws Exception {

        String[] deployParams = {TEST_BUNDLE_URI};
        String deployRet = DEPLOY_SUCCESS;        
        String[] getInstalledSARet2 = {TEST_SERVICE_ASSEMBLY_NAME};        
        String[] startParams = {TEST_SERVICE_ASSEMBLY_NAME};
        String startRet = START_SUCCESS;

        
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("getDeployedServiceAssemblies"), 
                                           EasyMock.aryEq(PARAMS_VOID), 
                                           EasyMock.aryEq(SIGNATURE_VOID)))
                                           .andReturn(EMPTY_STRING_ARRAY);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("deploy"), 
                                           EasyMock.aryEq(deployParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(deployRet);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("getDeployedServiceAssemblies"), 
                                           EasyMock.aryEq(PARAMS_VOID), 
                                           EasyMock.aryEq(SIGNATURE_VOID)))
                                           .andReturn(getInstalledSARet2);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("start"), 
                                           EasyMock.aryEq(startParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(startRet);
    }

    public void testJBIAgentInstall() throws Exception {
        setupDeployBundle();
        EasyMock.replay(mbeanServer);
        assertTrue(agent.installBundle(feature, bundle));
        EasyMock.verify(mbeanServer);
        
    }

    public void testJBIAgentDeployFailure() throws Exception {

        String[] params2 = {TEST_BUNDLE_URI};
                
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("getDeployedServiceAssemblies"), 
                                           EasyMock.aryEq(PARAMS_VOID), 
                                           EasyMock.aryEq(SIGNATURE_VOID)))
                                           .andReturn(EMPTY_STRING_ARRAY);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("deploy"), 
                                           EasyMock.aryEq(params2), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(DEPLOY_FAILURE);
        EasyMock.replay(mbeanServer);
        
        assertFalse(agent.installBundle(feature, bundle));
        EasyMock.verify(mbeanServer);
    }

    public void testJBIAgentStartFailure() throws Exception {
               
        String[] deployParams = {TEST_BUNDLE_URI};
        String deployRet = DEPLOY_SUCCESS;        
        String[] getInstalledSARet2 = {TEST_SERVICE_ASSEMBLY_NAME};        
        String[] startParams = {TEST_SERVICE_ASSEMBLY_NAME};
        String startRet = START_FAILURE;
                    
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("getDeployedServiceAssemblies"), 
                                           EasyMock.aryEq(PARAMS_VOID), 
                                           EasyMock.aryEq(SIGNATURE_VOID)))
                                           .andReturn(EMPTY_STRING_ARRAY);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("deploy"), 
                                           EasyMock.aryEq(deployParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(deployRet);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("getDeployedServiceAssemblies"), 
                                           EasyMock.aryEq(PARAMS_VOID), 
                                           EasyMock.aryEq(SIGNATURE_VOID)))
                                           .andReturn(getInstalledSARet2);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("start"), 
                                           EasyMock.aryEq(startParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(startRet);
        EasyMock.replay(mbeanServer);
            
        assertFalse(agent.installBundle(feature, bundle));
        EasyMock.verify(mbeanServer);
    }

    public void testJBIAgentUninstall() throws Exception {

        setupDeployBundle();
        
        String[] shutDownParams = {TEST_SERVICE_ASSEMBLY_NAME};
        String shutDownRet = SHUTDOWN_SUCCESS;
        String[] undeployParams = {TEST_SERVICE_ASSEMBLY_NAME};
        Object undeployRet = null;
                        
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("shutDown"), 
                                           EasyMock.aryEq(shutDownParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(shutDownRet);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("undeploy"), 
                                           EasyMock.aryEq(undeployParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(undeployRet);
        EasyMock.replay(mbeanServer);
        
        assertTrue(agent.installBundle(feature, bundle));
        assertTrue(agent.uninstallBundle(feature, bundle));
        EasyMock.verify(mbeanServer);
    }

    public void testJBIAgentShutdownFailure() throws Exception {

        setupDeployBundle();
        
        String[] shutDownParams = {TEST_SERVICE_ASSEMBLY_NAME};
        String shutDownRet = SHUTDOWN_FAILURE;
                        
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("shutDown"), 
                                           EasyMock.aryEq(shutDownParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(shutDownRet);
        EasyMock.replay(mbeanServer);
        
        assertTrue(agent.installBundle(feature, bundle));
        assertFalse(agent.uninstallBundle(feature, bundle));
        EasyMock.verify(mbeanServer);
    }

    public void testJBIAgentUndeployFailure() throws Exception {

        setupDeployBundle();
        
        String[] shutDownParams = {TEST_SERVICE_ASSEMBLY_NAME};
        String shutDownRet = SHUTDOWN_SUCCESS;
        String[] undeployParams = {TEST_SERVICE_ASSEMBLY_NAME};
        String undeployRet = UNDEPLOY_FAILURE;
                        
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("shutDown"), 
                                           EasyMock.aryEq(shutDownParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(shutDownRet);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("undeploy"), 
                                           EasyMock.aryEq(undeployParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(undeployRet);
        EasyMock.replay(mbeanServer);
        
        assertTrue(agent.installBundle(feature, bundle));
        assertFalse(agent.uninstallBundle(feature, bundle));
        EasyMock.verify(mbeanServer);
    }

    public void testJBIAgentUnknownSA() throws Exception {
        
        EasyMock.replay(mbeanServer);
        assertFalse(agent.uninstallBundle(feature, bundle));
        EasyMock.verify(mbeanServer);
    }        

    
    public void testJBIAgentBadXML() throws Exception {

        String[] deployParams = {TEST_BUNDLE_URI};
        
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("getDeployedServiceAssemblies"), 
                                           EasyMock.aryEq(PARAMS_VOID), 
                                           EasyMock.aryEq(SIGNATURE_VOID))).andReturn(EMPTY_STRING_ARRAY);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("deploy"), 
                                           EasyMock.aryEq(deployParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING))).andReturn(BAD_XML);
        EasyMock.replay(mbeanServer);
        
        assertFalse(agent.installBundle(feature, bundle));
        EasyMock.verify(mbeanServer);
    }
}
