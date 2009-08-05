/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.AgentCfgUpdate;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;
import org.fusesource.cloudmix.common.util.FileUtils;

public class AbstractInstallerAgentTest extends TestCase {
    
    
    class TestInstaller extends InstallerAgent {

        private Set<String> installedNames;
        private Set<String> installedURIs;
        private boolean validated;
        private String featureList;
        
        public TestInstaller() {
            super();
            installedNames = new HashSet<String>();
            installedURIs = new HashSet<String>();
        }
        
        public String[] getInstalledNames() {
            return (String[])installedNames.toArray(new String[installedNames.size()]);            
        }

        public String[] getInstalledURIs() {
            return (String[])installedURIs.toArray(new String[installedURIs.size()]);            
        }
        
        public boolean wasValidated() {
            return validated;
        }
        
        public String getFeatureListDoc() {
            return featureList;
        }
        
        @Override
        protected void installFeature(Feature feature, List<ConfigurationUpdate> cfg) throws Exception {

            super.installFeature(feature, cfg);
            featureList = feature.getFeatureList().toString();            
        }
        
        @Override
        protected boolean installBundle(Feature feature, Bundle bundle) {
            if (bundle.getUri().startsWith("https")) { 
                return false;
            }
            installedURIs.add(bundle.getUri());
            installedNames.add(bundle.getName());
            return true;
        }

        @Override
        protected boolean uninstallBundle(Feature feature, Bundle bundle) {
            installedURIs.remove(bundle.getUri());
            installedNames.remove(bundle.getName());
            return true;
        }

        @Override
        protected boolean validateAgent() {
            validated = true;
            return true;
        }    
    }
    
    private GridClient cl;
    private ProvisioningHistory ph;
    private TestInstaller installer;
   
    @Override
    protected void setUp() throws Exception {        
        
        cl = EasyMock.createNiceMock(GridClient.class);
        EasyMock.replay(cl);

        File tmpWorkDirectory = new File("tmp.work.dir");
        FileUtils.deleteDirectory(tmpWorkDirectory);

        if (tmpWorkDirectory.exists()) {
            System.out.println("\n\nWARNING: directory " + tmpWorkDirectory + " still exists\n\n");
        }
        
        installer = new TestInstaller();
        installer.setClient(cl);
        installer.setWorkDirectory(tmpWorkDirectory);
        installer.init();


        ph = new ProvisioningHistory();
        
    }
    
    
    public void testInstallNothing() throws Exception {

        assertNothingInstalled();        
        AgentDetails details = installer.getAgentDetails();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);
        assertEquals(null, details.getAgentLink());
        assertEquals(null, details.getContainerType());
        assertEquals(0, details.getSupportPackageTypes().length);
        details.setAgentLink("http://localhost:9999/stuff");
        details.setContainerType("Test");
        String [] supportPackageTypes = {"horse", "cart"};
        details.setSupportPackageTypes(supportPackageTypes);
        assertEquals("http://localhost:9999/stuff", details.getAgentLink());
        assertEquals("Test", details.getContainerType());
        assertEquals(2, details.getSupportPackageTypes().length);
        assertEquals("horse", details.getSupportPackageTypes()[0]);
        assertEquals("cart", details.getSupportPackageTypes()[1]);

        installer.onProvisioningHistoryChanged(ph);
        assertTrue(installer.wasValidated());
        
        assertNothingInstalled();        
        details = installer.getAgentDetails();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);       
    }

    public void testInstallSimple() throws Exception {
        
        assertNothingInstalled();        
        ph.addAction(createInstallAction("f1", "/features_1.xml"));
            
        AgentDetails details = installer.getAgentDetails();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);

        installer.onProvisioningHistoryChanged(ph);
        assertInstalledNames("r_1.txt");
        assertInstalled("http://example.com/r1.txt");

        details = installer.getAgentDetails();   
        assertEquals(1, details.getCurrentFeatures().length);
        assertEquals("f1", details.getCurrentFeatures()[0]);
    }
    
    
    // TODO: Enable when CM-2 is fixed.
    /*
    public void testInstallRestart() throws Exception {        
        testInstallSimple();
        
        installer.init();
        
        assertNothingInstalled();
        
        AgentDetails details = installer.getAgentDetails();   
        assertEquals(0, details.getCurrentFeatures().length);
    }
    */
   

    public void testInstallSimpleAsync() throws Exception {
        
        assertNothingInstalled();        
        ph.addAction(createInstallAction("f1", "/features_1.xml"));
            
        AgentDetails details = installer.getAgentDetails();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);


        installer.asyncOnProvisioningHistoryChanged(ph);
        // Let the provisioning thread run.
        Thread.sleep(2000);
        assertInstalledNames("r_1.txt");
        assertInstalled("http://example.com/r1.txt");

        details = installer.getAgentDetails();   
        assertEquals(1, details.getCurrentFeatures().length);
        assertEquals("f1", details.getCurrentFeatures()[0]);
    }

    
    public void testInstallMultipleBundles() throws Exception {
        
        assertNothingInstalled();        
        ph.addAction(createInstallAction("f2", "/features_2.xml"));
            
        AgentDetails details = installer.getAgentDetails();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);

        installer.onProvisioningHistoryChanged(ph);
        assertInstalledNames("r2.txt");
        assertInstalled("http://example.com/r2.txt", 
                        "http://example.com/r3.txt", 
                        "http://example.com/r4.txt");

        details = installer.getAgentDetails();   
        assertEquals(1, details.getCurrentFeatures().length);
        assertEquals("f2", details.getCurrentFeatures()[0]);
    }
    
    
    public void testInstallMultipleFeatures() throws Exception {
        
        assertNothingInstalled();        
        ph.addAction(createInstallAction("f1", "/features_1.xml"));
        ph.addAction(createInstallAction("f2", "/features_2.xml"));
        
        AgentDetails details = installer.getAgentDetails();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);

        installer.onProvisioningHistoryChanged(ph);
        assertInstalled("http://example.com/r1.txt", 
                        "http://example.com/r2.txt", 
                        "http://example.com/r3.txt", 
                        "http://example.com/r4.txt");
        

        details = installer.getAgentDetails();   
        assertEquals(2, details.getCurrentFeatures().length);
        assertTrue(contains("f1", details.getCurrentFeatures()));
        assertTrue(contains("f2", details.getCurrentFeatures()));
    }
   
    public void testInstallUninstall() throws Exception {
        
        assertNothingInstalled();        
        ph.addAction(createInstallAction("f1", "/features_1.xml"));
        ph.addAction(createInstallAction("f2", "/features_2.xml"));
        ph.addAction(createUninstallAction("f3"));
        
        AgentDetails details = installer.getAgentDetails();
        assertEquals(0, details.getCurrentFeatures().length);

        installer.onProvisioningHistoryChanged(ph);
        assertInstalled("http://example.com/r1.txt", 
                        "http://example.com/r2.txt", 
                        "http://example.com/r3.txt", 
                        "http://example.com/r4.txt");

        details = installer.getAgentDetails();   
        assertEquals(2, details.getCurrentFeatures().length);
        assertTrue(contains("f1", details.getCurrentFeatures()));
        assertTrue(contains("f2", details.getCurrentFeatures()));
        assertTrue(!contains("f3", details.getCurrentFeatures()));
        

        // Run same history again.
        installer.onProvisioningHistoryChanged(ph);
        assertInstalled("http://example.com/r1.txt", 
                         "http://example.com/r2.txt", 
                         "http://example.com/r3.txt", 
                        "http://example.com/r4.txt");

        details = installer.getAgentDetails();   
        assertEquals(2, details.getCurrentFeatures().length);
        assertTrue(contains("f1", details.getCurrentFeatures()));
        assertTrue(contains("f2", details.getCurrentFeatures()));
        assertTrue(!contains("f3", details.getCurrentFeatures()));
        
        // Uninstall a feature.
        ph.addAction(createUninstallAction("f1"));
        installer.onProvisioningHistoryChanged(ph);
        assertNotInstalled("http://example.com/r1.txt");
        assertInstalled("http://example.com/r2.txt", 
                        "http://example.com/r3.txt", 
                        "http://example.com/r4.txt");

        details = installer.getAgentDetails();   
        assertEquals(1, details.getCurrentFeatures().length);
        assertTrue(!contains("f1", details.getCurrentFeatures()));
        assertTrue(contains("f2", details.getCurrentFeatures()));
        assertTrue(!contains("f3", details.getCurrentFeatures()));

        // This is an updated feature list - what should happen here?
        ph.addAction(createInstallAction("f2", "/features_2a.xml"));
        installer.onProvisioningHistoryChanged(ph);
        assertNotInstalled("http://example.com/r1.txt", 
                           "http://example.com/r3.txt", 
                           "http://example.com/r4.txt");
        assertInstalled("http://example.com/r2.txt");
        
        // TODO more here.
        
    }
   
    public void testInstallBadFeature() throws Exception {

        assertNothingInstalled();                
        ph.addAction(createInstallAction("f3", "/features_3.xml"));
            
        AgentDetails details = installer.getAgentDetails();
        assertEquals(0, details.getCurrentFeatures().length);

        installer.onProvisioningHistoryChanged(ph);
        assertInstalled();

        details = installer.getAgentDetails();   
        assertEquals(0, details.getCurrentFeatures().length);
    }

    
    public void testInstallBadBundles() throws Exception {
        
        assertNothingInstalled();        
        ph.addAction(createInstallAction("f4", "/features_4.xml"));
            
        AgentDetails details = installer.getAgentDetails();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);

        installer.onProvisioningHistoryChanged(ph);
        assertNotInstalled("https://example.com/r2.txt", 
                           "https://example.com/r4.txt");

        assertInstalled("http://example.com/r1.txt", 
                        "http://example.com/r3.txt");

        details = installer.getAgentDetails();   
        assertEquals(1, details.getCurrentFeatures().length);
        assertEquals("f4", details.getCurrentFeatures()[0]);
    }

    
    public void testUpdateConfiguration() throws Exception {
        
        assertNothingInstalled();        
        
        ph.addCfgUpdate(new AgentCfgUpdate(AgentCfgUpdate.PROPERTY_AGENT_NAME, "bob"));
        ph.addCfgUpdate(new AgentCfgUpdate(AgentCfgUpdate.PROPERTY_PROFILE_ID, "umberProfile"));
            
        AgentDetails details = installer.getAgentDetails();
        
        String initialAgentId = installer.getAgentId();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);

        installer.onProvisioningHistoryChanged(ph);
        assertEquals("bob", details.getName());
        assertEquals("umberProfile", details.getProfile());
        
        assertEquals(initialAgentId, installer.getAgentId());
        assertEquals(initialAgentId, details.getId());
    }

    public void testSetApplicationProperties() throws Exception {
        assertNothingInstalled();        
        ph.addAction(createInstallAction("f6", "/features_6.xml"));
            
        AgentDetails details = installer.getAgentDetails();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);

        installer.onProvisioningHistoryChanged(ph);
        assertEquals("Application cfg 1", System.getProperty("appProp1"));
        assertEquals("appCfg2", System.getProperty("appProp2"));
        assertEquals("3", System.getProperty("app_Prop_3"));
    }

    public void testSetApplicationPropertyOverrides() throws Exception {
        assertNothingInstalled();        
        ProvisioningAction installAction = createInstallAction("f6", "/features_6.xml");
        installAction.addCfgOverride(new ConfigurationUpdate("appCfg2", "has been Overridden"));
        ph.addAction(installAction);
            
        AgentDetails details = installer.getAgentDetails();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);

        installer.onProvisioningHistoryChanged(ph);
        assertEquals("Application cfg 1", System.getProperty("appProp1"));
        assertEquals("has been Overridden", System.getProperty("appCfg2"));
        assertEquals("3", System.getProperty("app_Prop_3"));
    }

    
    private ProvisioningAction createInstallAction(String feature, String resource) {
        String url = this.getClass().getResource(resource).toString();
        return new ProvisioningAction(ProvisioningAction.INSTALL_COMMAND, feature, url);
    }

    private ProvisioningAction createUninstallAction(String feature) {
        return new ProvisioningAction(ProvisioningAction.UNINSTALL_COMMAND, feature, null);
    }
    
    private void assertNothingInstalled() {
        assertEquals("Nothing deployed", 0, installer.getInstalledNames().length);
    }
    
    private void assertInstalled(String... names) {
        assertListsMatch(installer.getInstalledURIs(), names);
    }
    
    private void assertInstalledNames(String... names) {
        assertListsMatch(installer.getInstalledNames(), names);
    }

    private void assertNotInstalled(String... names) {

        String[] installedURIs = installer.getInstalledURIs();
        int count = 0;
        for (String n : names) {
            boolean found = false;
            for (String s : installedURIs) {
                if (n.equals(s)) {
                    count++;
                    found = true;
                }
            } 
            if (found) {
                fail("Bundle " + n + " is installed, but should not be");
            }
        }
        assertEquals(0, count);
    }

    private void assertListsMatch(String[] list, String... names) {

        int count = 0;        
        for (String n : names) {
            boolean found = false;
            for (String s : list) {
                if (n.equals(s)) {
                    count++;
                    found = true;
                }
            }
            if (!found) {
                fail("Bundle " + n + " is not installed");
            }
        }
        assertEquals(names.length, count);
    }


    private boolean contains(String s, String[] l) {
        for (String s2 : l) {
            if (s.equals(s2)) {
                return true;
            }
        }
        return false;
    }

}
