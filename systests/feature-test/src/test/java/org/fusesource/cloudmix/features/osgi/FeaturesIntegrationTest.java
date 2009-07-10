/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.features.osgi;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

/**
 * @version $Revision: 1.1 $
 */
@RunWith(JUnit4TestRunner.class)
public class FeaturesIntegrationTest extends TestCase {

    @Inject
    protected BundleContext bundleContext;

    @Test
    public void testFeatureRunsInOsgi() throws Exception {
        System.out.println("Started up!");
        Thread.sleep(1000);

        System.out.println("Worked!!!");
    }

              
    @Configuration
    public static Option[] configure() {
        Option[] options = options(
        		
            // lets zap the caches first to ensure we're using the latest/greatest
            cleanCaches(),

            // install log service using pax runners profile abstraction (there are more profiles, like DS)
            logProfile().version("1.3.0"),
            profile("karaf.gogo", "1.2.0"),
            
            // using the features to install the features
            scanFeatures(mavenBundle().groupId("org.fusesource.cloudmix").
                         artifactId("features").versionAsInProject().type("xml/features"),
                          "cloudmix.agent"),
            
            cleanCaches(),

            systemProperty("karaf.home").value(System.getProperty("user.dir")),
            systemProperty("karaf.name").value("root"),
            systemProperty("karaf.startLocalConsole").value("false"),
            systemProperty("karaf.startRemoteConsole").value("false"),
            systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("DEBUG"),
            
            felix() //, equinox(), knopflerfish()
          );

        return options;
    }
}
