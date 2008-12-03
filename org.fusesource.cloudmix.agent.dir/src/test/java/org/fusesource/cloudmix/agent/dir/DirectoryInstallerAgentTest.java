package org.fusesource.cloudmix.agent.dir;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import junit.framework.TestCase;

import org.fusesource.cloudmix.agent.Bundle;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.agent.dir.DirectoryInstallerAgent;

public class DirectoryInstallerAgentTest extends TestCase {
    
    private static boolean urlHandlerIsSet;

    private DirectoryInstallerAgent installer;
    private File installDir;
    
    @Override
    protected void setUp() throws Exception {

        installDir = new File("deployDir");
        cleanDirectory(installDir);
        installDir.mkdir();
        
        installer = new DirectoryInstallerAgent();
        installer.setInstallDirectory(installDir.getPath());
        
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
    
    @Override
    protected void tearDown() throws Exception {
        cleanDirectory(installDir);
    }
    
    
    public void testDirectoryInstallerValidate() throws Exception {
        
        assertDirEmpty(installDir);
        assertTrue(installer.validateAgent());
        assertEquals(installDir.getPath(), installer.getInstallDirectory());
        assertNull(installer.getTempSuffix());
        AgentDetails details = installer.getAgentDetails();
        assertEquals("default", details.getProfile());
        assertEquals(0, details.getCurrentFeatures().length);
        
    }
    
    public void testDirectoryInstaller() throws Exception {
        
        assertDirEmpty(installDir);
         
        // Install r1
        Bundle r1 = new Bundle("r_1.txt", "", "resource:///r1.txt");
        Feature feature = new Feature("f1", null);
        feature.addBundle(r1);                          
        assertTrue(installer.installBundle(feature, r1));
        assertDirContains(installDir, "r_1.txt");
        assertEquals("Resource one.", getResourceAsString(installDir, "r_1.txt"));
        
        // Install r2
        Bundle r2 = new Bundle(null, "", "resource:///r2.txt");
        feature.addBundle(r2);
        assertTrue(installer.installBundle(feature, r2));
        assertDirContains(installDir, "r_1.txt", "r2.txt");
        assertEquals("Resource two.", getResourceAsString(installDir, "r2.txt"));
      
        // Install r3
        Bundle r3 = new Bundle("", "", "resource:///r3.txt");
        feature.addBundle(r3);
        assertTrue(installer.installBundle(feature, r3));
        assertDirContains(installDir, "r_1.txt", "r2.txt", "r3.txt");
        assertEquals("Resource three.", getResourceAsString(installDir, "r3.txt"));
            
        // Uninstall r2
        assertTrue(installer.uninstallBundle(feature, r2));
        assertDirContains(installDir, "r_1.txt", "r3.txt");
        assertDirNotContains(installDir, "r2.txt");

        // Uninstall r2 again
        assertTrue(installer.uninstallBundle(feature, r2));
        assertDirContains(installDir, "r_1.txt", "r3.txt");
        assertDirNotContains(installDir, "r2.txt");

        // Uninstall r1
        assertTrue(installer.uninstallBundle(feature, r1));
        assertDirContains(installDir, "r3.txt");
        assertDirNotContains(installDir, "r_1.txt", "r2.txt");

        // Uninstall r3
        assertTrue(installer.uninstallBundle(feature, r3));
        assertDirEmpty(installDir);

    }

    public void testDirectoryInstallerSuffix() throws Exception {
        
        assertDirEmpty(installDir);

        installer.setTempSuffix(".temp");
        assertEquals(".temp", installer.getTempSuffix());
         
        Bundle r1 = new Bundle("r_1.txt", "", "resource:///r1.txt");
        Feature feature = new Feature("f1", null);
        feature.addBundle(r1);
                          
        assertTrue(installer.installBundle(feature, r1));

        assertDirContains(installDir, "r_1.txt");
        assertEquals("Resource one.", getResourceAsString(installDir, "r_1.txt"));

    }

    public void testDirectoryInstallerBadBundle() throws Exception {
        
        assertDirEmpty(installDir);

        Bundle r4 = new Bundle(null, "", "resource:///r4.txt");
        Feature feature = new Feature("f4", null);
        feature.addBundle(r4);
                          
        assertFalse(installer.installBundle(feature, r4));

        assertDirEmpty(installDir);
    }

    
    
    private void listDirContents(File dir) {
        int count = 0;
        System.out.println("Contents of directory " + dir.getPath());
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : dir.listFiles()) {
                System.out.println("  " + count++ + " " + file.getName());
            }
        }
    }
    
    private void assertDirContains(File dir, String... names) {
        assertTrue(dir.isDirectory());
        
        int count = 0;
        for (String n : names) {
            for (File file : dir.listFiles()) {
                if (n.equals(file.getName())) {
                    count++;
                }
            }
        }
        assertEquals(names.length, count);
        assertEquals(names.length, dir.listFiles().length);
    }

    private void assertDirNotContains(File dir, String... names) {
        assertTrue(dir.isDirectory());
        
        int count = 0;
        for (String n : names) {
            for (File file : dir.listFiles()) {
                if (n.equals(file.getName())) {
                    count++;
                }
            }
        }
        assertEquals(0, count);
    }


    private void assertDirEmpty(File dir) {
        assertTrue(dir.isDirectory());
        assertTrue(dir.listFiles().length == 0);
    }

    private String getResourceAsString(File dir, String name) throws Exception {
        File file = new File(dir, name);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }
    
    private void cleanDirectory(File dir) throws Exception {
        File[] files = installDir.listFiles();
        if (files != null) {
            for (File file : installDir.listFiles()) {
                file.delete();
            }
        }
        installDir.delete();
    }

    /**
     * URLConnection class to resolve resource URLS.
     *
     */
    class ResourceURLConnection extends URLConnection {

        protected ResourceURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
            // Complete
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            return this.getClass().getResourceAsStream(url.getPath());
        }
    }
}
