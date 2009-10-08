/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.karaf;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import junit.framework.TestCase;

import org.apache.felix.karaf.features.Feature;
import org.apache.felix.karaf.features.FeaturesService;
import org.apache.felix.karaf.features.Repository;
import org.apache.felix.karaf.features.internal.FeatureImpl;
import org.easymock.EasyMock;
import org.fusesource.cloudmix.agent.Bundle;
import org.fusesource.cloudmix.agent.FeatureList;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.util.FileUtils;

public class KarafAgentTest extends TestCase {
    private GridClient cl;
    private MockFeaturesService fs;
    private KarafAgent smxa;
    private File workdir;

    class MockFeaturesService implements FeaturesService {

        private Map<URI, FeatureList> repos = new HashMap<URI, FeatureList>();
        private FeatureList lastFeatureList;
        private List<Feature> features = new ArrayList<Feature>();
        private List<String> bundles = new ArrayList<String>();

        public void addRepository(URI uri) throws Exception {
            FeatureList featureList = new FeatureList(uri.toURL(), null) {
                @Override
                protected Bundle extractBundleInfo(Element b) {
                    return new Bundle(b.getAttribute("name"), b.getAttribute("type"), b.getTextContent());
                }
            };
            assertNotNull(featureList);
            assertNull(repos.get(uri));
            repos.put(uri, featureList);
            lastFeatureList = featureList;
        }

        public void installFeature(String name) throws Exception {
            assertFalse(features.contains(name));
            features.add(new FeatureImpl(name));
            assertNotNull(lastFeatureList);
            org.fusesource.cloudmix.agent.Feature f = lastFeatureList.getFeature(name);
            assertNotNull(f);
            for (Bundle b : f.getBundles()) {
                bundles.add(b.getUri());
            }
        }

        public Feature[] listFeatures() throws Exception {
            return (Feature[])features.toArray(new Feature[features.size()]);
        }

        public Feature[] listInstalledFeatures() {
            return (Feature[])features.toArray(new Feature[features.size()]);
        }

        public Repository[] listRepositories() {
            fail("NOT IMPLEMENTED");
            return null;
        }

        public void removeRepository(URI uri) {
            lastFeatureList = null;
            repos.remove(uri);
        }

        public void uninstallFeature(String name) throws Exception {
            features.remove(name);
        }

        public void assertFeatureInstalled(String name) {
            boolean installed = false;
            for (Feature feature : features) {
                if (feature.getName().equals(name)) {
                    installed = true;
                    break;
                }
            }
            assertTrue("Feature " + name + " should have been installed", installed);
        }

        public void assertBundleInstalled(String url) {
            assertTrue(bundles.contains(url));
        }

        public void assertFeatureNotInstalled(String name) {
            assertFalse(features.contains(name));
        }

        public void assertBundleNotInstalled(String url) {
            // TODO - more work needed in uninstallFeature to get this working
            // assertFalse(bundles.contains(url));
        }

        public void installFeature(String name, String version) throws Exception {
            fail("NOT IMPLEMENTED");
        }

        public void uninstallFeature(String name, String version) throws Exception {
            fail("NOT IMPLEMENTED");
        }

        public boolean isInstalled(Feature feature) {
            return features.contains(feature);
        }

    }

    @Override
    protected void setUp() throws Exception {
        cl = EasyMock.createNiceMock(GridClient.class);
        EasyMock.replay(cl);

        smxa = new KarafAgent() {
            @Override
            public GridClient getClient() {
                return cl;
            }
        };
        workdir = new File("target" + File.separator + "testworkdir");
        FileUtils.createDirectory(workdir);

        fs = new MockFeaturesService();

        smxa.setFeaturesService(fs);
        smxa.setWorkDirectory(workdir);
        smxa.setDetailsPropertyFilePath(workdir.getAbsolutePath() + "/agent.properties");
    }

    @Override
    protected void tearDown() throws Exception {
        if (workdir != null) {
            FileUtils.deleteDirectory(workdir);
        }
    }

    public void testPopulateAgentDetails() throws Exception {

        AgentDetails details = new AgentDetails();
        smxa.init();
        details = smxa.getAgentDetails();
        assertTrue(details.getPid() > 0);
        assertEquals(0, details.getCurrentFeatures().size());
        assertEquals(null, details.getAgentLink());
        assertEquals("smx4", details.getContainerType());
        assertEquals(2, details.getSupportPackageTypes().length);
        assertEquals("osgi", details.getSupportPackageTypes()[0]);
        assertEquals("jbi", details.getSupportPackageTypes()[1]);
    }

    public void testGenerateSMX4FeatureDoc() throws Exception {

        // JBI urls
        FeatureList flist = getFeatureList("/features_5.xml");
        String expectedSmxFeatureDoc = "<features>" + "<feature name=\"f5\">"
                                       + "<bundle>jbi:http://example.com/r1.txt</bundle>"
                                       + "<bundle>jbi:http://example.com/r2.txt</bundle>" + "</feature>"
                                       + "</features>";

        String smx4Doc = smxa.generateSMX4FeatureDoc(flist);
        assertEquals(expectedSmxFeatureDoc, smx4Doc.replaceAll("\n", "").replaceAll("  ", ""));

        // complex deps
        flist = getFeatureList("/features_6.xml");
        expectedSmxFeatureDoc = "<features>" + "<feature name=\"f5\">"
                                + "<bundle>http://example.com/bundle_A1.jar</bundle>"
                                + "<bundle>http://example.com/bundle_A21.jar</bundle>"
                                + "<bundle>http://example.com/bundle_A2.jar</bundle>"
                                + "<bundle>http://example.com/bundle_B1.jar</bundle>"
                                + "<bundle>http://example.com/bundle_C.jar</bundle>"
                                + "<bundle>http://example.com/bundle_B.jar</bundle>"
                                + "<bundle>http://example.com/bundle_A.jar</bundle>" + "</feature>"
                                + "</features>";

        smx4Doc = smxa.generateSMX4FeatureDoc(flist);
        assertEquals(expectedSmxFeatureDoc, smx4Doc.replaceAll("\n", "").replaceAll("  ", ""));

    }

    public void testInstallNothing() throws Exception {
        FeatureList flist = getFeatureList("/features_1.xml");

        assertEquals(0, fs.listFeatures().length);
        assertEquals(0, fs.listInstalledFeatures().length);

        String f1 = "f1";
        smxa.installFeature(flist.getFeature(f1), null);

        assertEquals(1, fs.listFeatures().length);
        assertEquals(1, fs.listInstalledFeatures().length);
        assertAgentDetails(f1);

    }

    public void testInstall() throws Exception {
        FeatureList flist = getFeatureList("/features_2.xml");
        String f2 = "f2";
        String url2 = "http://localhost/2";
        smxa.installFeature(flist.getFeature(f2), null);

        Feature[] features = fs.listFeatures();
        assertEquals(1, features.length);
        assertEquals(f2, features[0].getName());
        fs.assertFeatureInstalled(f2);
        fs.assertBundleInstalled(url2);
        assertAgentDetails(f2);
    }

    public void testInstallJBI() throws Exception {
        FeatureList flist = getFeatureList("/features_5.xml");
        String f5 = "f5";
        smxa.installFeature(flist.getFeature(f5), null);

        Feature[] features = fs.listFeatures();
        assertEquals(1, features.length);
        assertEquals(f5, features[0].getName());
        fs.assertFeatureInstalled(f5);
        fs.assertBundleInstalled("jbi:http://example.com/r1.txt");
        fs.assertBundleInstalled("jbi:http://example.com/r2.txt");
        assertAgentDetails(f5);
    }

    public void testInstall2() throws Exception {

        FeatureList flist2 = getFeatureList("/features_2.xml");
        String f2 = "f2";
        String url2 = "http://localhost/2";

        FeatureList flist3 = getFeatureList("/features_3.xml");
        String f3 = "f3";
        String url3 = "http://localhost/3";

        smxa.installFeature(flist2.getFeature(f2), null);
        smxa.installFeature(flist3.getFeature(f3), null);

        fs.assertFeatureInstalled(f2);
        fs.assertBundleInstalled(url2);
        fs.assertFeatureInstalled(f3);
        fs.assertBundleInstalled(url3);

        assertEquals(2, fs.listFeatures().length);
        assertAgentDetails("f2", "f3");
    }

    public void testUninstallNothing() throws Exception {

        assertEquals(0, fs.listFeatures().length);
        assertEquals(0, fs.listInstalledFeatures().length);

        FeatureList flist4 = getFeatureList("/features_4.xml");

        smxa.uninstallFeature(flist4.getFeature("unknown"));

        assertEquals(0, fs.listFeatures().length);
    }

    public void testInstallUninstall() throws Exception {

        FeatureList flist2 = getFeatureList("/features_2.xml");
        String f2 = "f2";
        String url2 = "http://localhost/2";

        FeatureList flist3 = getFeatureList("/features_3.xml");
        String f3 = "f3";
        String url3 = "http://localhost/3";

        smxa.installFeature(flist2.getFeature(f2), null);
        smxa.installFeature(flist3.getFeature(f3), null);
        assertAgentDetails(f2, f3);

        fs.assertFeatureInstalled(f2);
        fs.assertBundleInstalled(url2);
        fs.assertFeatureInstalled(f3);
        fs.assertBundleInstalled(url3);

        smxa.uninstallFeature(flist2.getFeature(f2));
        assertAgentDetails(f3);
        fs.assertFeatureNotInstalled(f2);
        fs.assertBundleNotInstalled(url2);
        fs.assertFeatureInstalled(f3);
        fs.assertBundleInstalled(url3);

        smxa.uninstallFeature(flist3.getFeature(f3));
        assertAgentDetails();
        fs.assertFeatureNotInstalled(f2);
        fs.assertBundleNotInstalled(url2);
        fs.assertFeatureNotInstalled(f3);
        fs.assertBundleNotInstalled(url3);
    }

    private void assertAgentDetails(String... expectedFeatures) throws Exception {

        smxa.updateAgentDetails();
        AgentDetails details = smxa.getAgentDetails();
        Set<String> actualFeatures = details.getCurrentFeatures();
        assertNotNull(actualFeatures);

        assertEquals(expectedFeatures.length, actualFeatures.size());
        for (String ef : expectedFeatures) {
            boolean found = false;
            for (String af : actualFeatures) {
                if (ef.equals(af)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Cannot find expected feature " + ef, found);
        }
    }

    public void testInstallActionsWithErrors() throws Exception {
        // TODO
    }

    public void testUninstallActionsWithErrors() throws Exception {
        // TODO
    }

    public void testGetDetailsPropertyFilePath() throws Exception {
        KarafAgent agent = new KarafAgent();
        System.setProperty(KarafAgent.VM_PROP_SMX_HOME, "home");
        assertEquals("home" + File.separator + "data" + File.separator + "cloudmix" + File.separator
                     + "agent.properties", agent.getDetailsPropertyFilePath());
    }

    private FeatureList getFeatureList(String name) throws Exception {
        URL url = this.getClass().getResource(name);
        FeatureList flist = new FeatureList(url, null);
        assertNotNull(flist);
        return flist;
    }
}
