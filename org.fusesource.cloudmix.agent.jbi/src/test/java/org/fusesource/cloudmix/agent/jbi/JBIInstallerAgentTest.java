/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.jbi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;


import org.fusesource.cloudmix.agent.Bundle;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.agent.security.SecurityUtils;
import org.easymock.EasyMock;

public class JBIInstallerAgentTest extends TestCase {

    // Constants for JMX invocations
    private static final String[] SIGNATURE_VOID = {};
    private static final String[] SIGNATURE_STRING = {"java.lang.String"};
    private static final String[] PARAMS_VOID = {};
    private static final String[] EMPTY_STRING_ARRAY = {};
    
    private static final String TEST_FEATURE_NAME_1 = "f1";
    private static final String TEST_FEATURE_NAME_2 = "f2";
    private static final String TEST_BUNDLE_URI_1 = "resource://foo:bar@example.com/sa_1.zip";
    private static final String TEST_BUNDLE_URI_2 = "resource://foo:bar@example.com/sa_2.zip";
    private static final String TEST_SERVICE_ASSEMBLY_NAME = "b1-sa";
    private static final String TEST_COMP_1 = "comp_1";
    private static final String TEST_COMP_2 = "comp_2";
    private static final String TEST_COMP_3 = "comp_3";
    
    private static boolean urlHandlerIsSet;

    
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
    private Bundle bundle1;
    private Bundle bundle2;
    private Feature feature1;
    private Feature feature2;


    
    public void setUp() throws Exception {
        agent = new JBIInstallerAgent();
        mbeanName = new ObjectName("test:Name=Test");        
        mbeanServer = EasyMock.createStrictMock(MBeanServer.class);
        agent.setMBeanName(mbeanName);
        agent.setMBeanServer(mbeanServer);
        agent.setMaxDeployAttempts(3);
        agent.setDeployAttemptDelay(1);
                
        bundle1 = new Bundle("", "", TEST_BUNDLE_URI_1);
        feature1 = new Feature(TEST_FEATURE_NAME_1, null);
        feature1.addBundle(bundle1);

        bundle2 = new Bundle("", "", TEST_BUNDLE_URI_2);
        feature2 = new Feature(TEST_FEATURE_NAME_2, null);
        feature2.addBundle(bundle2);
        
        // Set up a URL handler for resource: URLs.  These resolve to local
        // class resources.
        if (!urlHandlerIsSet) {
            URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
                public URLStreamHandler createURLStreamHandler(String protocol) {
                    if ("resource".equals(protocol)) {
                        return new URLStreamHandler() {
                            protected URLConnection openConnection(URL u) throws IOException {
                                return new ResourceURLConnection(u);
                            }
                        };
                    }
                    return null;
                }
            });
            urlHandlerIsSet = true;
        }

    }

    public void testJBIAgentValidate() throws Exception {
        
        EasyMock.replay(mbeanServer);
        assertTrue(agent.validateAgent());
        EasyMock.verify(mbeanServer);
    }
        
    public void setupDeployBundle() throws Exception {

        String[] comp1Params = {TEST_COMP_1};
        String[] comp2Params = {TEST_COMP_2};
        String[] deployParams = {TEST_BUNDLE_URI_1};
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
                                           EasyMock.eq("canDeployToComponent"), 
                                           EasyMock.aryEq(comp1Params), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(Boolean.TRUE);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName),
                                           EasyMock.eq("canDeployToComponent"), 
                                           EasyMock.aryEq(comp2Params), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(Boolean.TRUE);
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
        assertTrue(agent.installBundle(feature1, bundle1));
        EasyMock.verify(mbeanServer);
        
    }

    public void testJBIAgentDeployFailure() throws Exception {

        String[] params2 = {TEST_BUNDLE_URI_1};                
        String[] comp1Params = {TEST_COMP_1};
        String[] comp2Params = {TEST_COMP_2};

        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("getDeployedServiceAssemblies"), 
                                           EasyMock.aryEq(PARAMS_VOID), 
                                           EasyMock.aryEq(SIGNATURE_VOID)))
                                           .andReturn(EMPTY_STRING_ARRAY);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName),
                                           EasyMock.eq("canDeployToComponent"), 
                                           EasyMock.aryEq(comp1Params), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(Boolean.TRUE);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName),
                                           EasyMock.eq("canDeployToComponent"), 
                                           EasyMock.aryEq(comp2Params), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(Boolean.TRUE);   
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("deploy"), 
                                           EasyMock.aryEq(params2), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(DEPLOY_FAILURE);
        EasyMock.replay(mbeanServer);
        
        assertFalse(agent.installBundle(feature1, bundle1));
        EasyMock.verify(mbeanServer);
    }

    public void testJBIAgentDeployComponentUnavailable() throws Exception {

        String[] params2 = {TEST_BUNDLE_URI_2};                
        String[] comp3Params = {TEST_COMP_3};

        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("getDeployedServiceAssemblies"), 
                                           EasyMock.aryEq(PARAMS_VOID), 
                                           EasyMock.aryEq(SIGNATURE_VOID)))
                                           .andReturn(EMPTY_STRING_ARRAY);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName),
                                           EasyMock.eq("canDeployToComponent"), 
                                           EasyMock.aryEq(comp3Params), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(Boolean.FALSE).anyTimes();
        EasyMock.replay(mbeanServer);
        
        assertFalse(agent.installBundle(feature2, bundle2));
        EasyMock.verify(mbeanServer);
    }

    public void testJBIAgentStartFailure() throws Exception {
               
        String[] comp1Params = {TEST_COMP_1};
        String[] comp2Params = {TEST_COMP_2};
        String[] deployParams = {TEST_BUNDLE_URI_1};
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
                                           EasyMock.eq("canDeployToComponent"), 
                                           EasyMock.aryEq(comp1Params), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(Boolean.TRUE);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName),
                                           EasyMock.eq("canDeployToComponent"), 
                                           EasyMock.aryEq(comp2Params), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(Boolean.TRUE);   
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
            
        assertFalse(agent.installBundle(feature1, bundle1));
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
        
        assertTrue(agent.installBundle(feature1, bundle1));
        assertTrue(agent.uninstallBundle(feature1, bundle1));
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
        
        assertTrue(agent.installBundle(feature1, bundle1));
        assertFalse(agent.uninstallBundle(feature1, bundle1));
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
        
        assertTrue(agent.installBundle(feature1, bundle1));
        assertFalse(agent.uninstallBundle(feature1, bundle1));
        EasyMock.verify(mbeanServer);
    }

    public void testJBIAgentUnknownSA() throws Exception {
        
        EasyMock.replay(mbeanServer);
        assertFalse(agent.uninstallBundle(feature1, bundle1));
        EasyMock.verify(mbeanServer);
    }        

    
    public void testJBIAgentBadXML() throws Exception {

        String[] deployParams = {TEST_BUNDLE_URI_1};        
        String[] comp1Params = {TEST_COMP_1};
        String[] comp2Params = {TEST_COMP_2};

        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("getDeployedServiceAssemblies"), 
                                           EasyMock.aryEq(PARAMS_VOID), 
                                           EasyMock.aryEq(SIGNATURE_VOID))).andReturn(EMPTY_STRING_ARRAY);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName),
                                           EasyMock.eq("canDeployToComponent"), 
                                           EasyMock.aryEq(comp1Params), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(Boolean.TRUE);
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName),
                                           EasyMock.eq("canDeployToComponent"), 
                                           EasyMock.aryEq(comp2Params), 
                                           EasyMock.aryEq(SIGNATURE_STRING)))
                                           .andReturn(Boolean.TRUE);   
        
        EasyMock.expect(mbeanServer.invoke(EasyMock.eq(mbeanName), 
                                           EasyMock.eq("deploy"), 
                                           EasyMock.aryEq(deployParams), 
                                           EasyMock.aryEq(SIGNATURE_STRING))).andReturn(BAD_XML);
        EasyMock.replay(mbeanServer);
        
        assertFalse(agent.installBundle(feature1, bundle1));
        EasyMock.verify(mbeanServer);
    }
    
    /**
     * URLConnection class to resolve resource URLS.
     *
     */
    static class ResourceURLConnection extends URLConnection {
        public static Map<String, String> reqProps = new HashMap<String, String>();
        
        protected ResourceURLConnection(URL url) {
            super(url);
            reqProps.clear();
        }

        @Override
        public void connect() throws IOException {
            // Complete
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            String auth = reqProps.get("Authorization");
            String usrInfo = getURL().getUserInfo();
            if (usrInfo != null && !"".equals(usrInfo)) {
                assertEquals(SecurityUtils.getBasicAuthHeader(usrInfo), auth);
            } else {
                assertNull(auth);
            }
            return this.getClass().getResourceAsStream(url.getPath());
        }
        
        @Override
        public void setRequestProperty(String key, String value) {
            reqProps.put(key, value);
        }
    }
    
}
