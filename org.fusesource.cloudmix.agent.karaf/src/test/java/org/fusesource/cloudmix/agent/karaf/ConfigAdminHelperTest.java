/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.karaf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import org.fusesource.cloudmix.common.util.FileUtils;


/**
 * Test cases for {@link org.fusesource.cloudmix.agent.karaf.ConfigAdminHelper}
 */
public class ConfigAdminHelperTest extends TestCase {

    private static final String REPOSITORY = "mvn:org.fusesource.cloudmix/features/1.3-SNAPSHOT/xml/features";
    private static final String FEATURE = "cloudmix.agent";

    /*
     * Test merging two strings of comma-separated values
     */
    public void testMerge() {
        assertEquals("cloudmix.agent,webconsole",
                     ConfigAdminHelper.merge("cloudmix.agent", "webconsole"));
        assertEquals("cloudmix.agent,spring-dm,webconsole",
                     ConfigAdminHelper.merge("cloudmix.agent", "webconsole,spring-dm"));
        assertEquals("cloudmix.agent,spring-dm,webconsole",
                     ConfigAdminHelper.merge("cloudmix.agent,spring-dm", "webconsole,spring-dm"));        
    }

    /*
     * Test merging new properties into an existing file
     */
    public void testMergeMapIntoFile() throws IOException {
        File file = File.createTempFile("org.apache.felix.karaf.features.", ".cfg", new File("target"));
        FileUtils.copy(getClass().getClassLoader()
                       .getResourceAsStream("org.apache.felix.karaf.features.cfg"), 
                       new FileOutputStream(file));

        Map<String, String> properties = new HashMap<String,  String>();
        properties.put(KarafAgent.FEATURES_BOOT, FEATURE);
        properties.put(KarafAgent.FEATURES_REPOSITORIES, REPOSITORY);
        ConfigAdminHelper.merge(file, properties);

        // now, let's read the file and check if everything got merged in
        Properties result = new Properties();
        result.load(new FileInputStream(file));

        assertTrue(FEATURE + " should have been added to the boot features",
                   result.getProperty(KarafAgent.FEATURES_BOOT).contains(FEATURE));
        assertTrue(REPOSITORY + " should have been added to the feature repositories",
                   result.getProperty(KarafAgent.FEATURES_REPOSITORIES).contains(REPOSITORY));
        assertEquals("We should have two repositories listed in total",
                     2, result.getProperty(KarafAgent.FEATURES_REPOSITORIES).split(",").length);

        // if everything went well, we can delete the temp file
        FileUtils.deleteFile(file);
    }
}
