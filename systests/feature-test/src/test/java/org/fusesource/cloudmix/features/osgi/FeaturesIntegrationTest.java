/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.features.osgi;

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.knopflerfish;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
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
            // install the spring dm profile
            //profile("spring.dm").version("1.2.0"),
            // this is how you set the default log level when using pax logging (logProfile)
            systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("DEBUG"),
            
            mavenBundle().groupId("org.apache.felix.karaf.deployer").artifactId("org.apache.felix.karaf.deployer.filemonitor").version("1.2.0-SNAPSHOT"),
            mavenBundle().groupId("org.apache.geronimo").artifactId("blueprint-bundle").version("1.0.0-SNAPSHOT"),
            mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.jline").version("0.9.94_1"),
            mavenBundle().groupId("org.apache.felix.gogo").artifactId("org.apache.felix.gogo.runtime").version("0.9.0-SNAPSHOT"),
            mavenBundle().groupId("org.apache.felix.gogo").artifactId("org.apache.felix.gogo.commands").version("0.9.0-SNAPSHOT"),
            mavenBundle().groupId("org.apache.felix.karaf.gshell").artifactId("org.apache.felix.karaf.gshell.console").version("1.2.0-SNAPSHOT"),
            mavenBundle().groupId("org.apache.felix.karaf.gshell").artifactId("org.apache.felix.karaf.gshell.features").version("1.2.0-SNAPSHOT"),
            mavenBundle().groupId("org.apache.felix.karaf.deployer").artifactId("org.apache.felix.karaf.deployer.blueprint").version("1.2.0-SNAPSHOT"),
        
            // using the features to install the features
            scanFeatures(mavenBundle().groupId("org.fusesource.cloudmix").
                         artifactId("features").versionAsInProject().type("xml/features"),
                          "cloudmix.agent"),
            
            cleanCaches(),

            //knopflerfish(), felix(), equinox());
            felix(), equinox(), knopflerfish()
          );

        return options;
    }
}
