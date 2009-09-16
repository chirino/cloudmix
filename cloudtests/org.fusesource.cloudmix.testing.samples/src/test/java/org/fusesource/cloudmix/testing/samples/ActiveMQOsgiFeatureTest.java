/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.testing.samples;

import java.util.Map;
import java.util.Properties;

import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.testing.TestController;
import org.junit.Test;


/**
 * @version $Revision$
 */
public class ActiveMQOsgiFeatureTest extends TestController {

    @Test
    public void testScenarioDeploys() throws Exception {
        System.out.println("Worked!!!");
        Thread.sleep(10000);
    }


    protected void installFeatures() {
        // TODO get this from system properties?
        Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.out.println(" " + entry.getKey() + " = " + entry.getValue());
        }
        String version = "1.3-SNAPSHOT";

        FeatureDetails broker = createFeatureDetails("org.fusesource.cloudmix.tests.broker",
                "scan-features:mvn:org.fusesource.cloudmix/org.apache.activemq.broker.multicast/"
                + version + "/xml/features!/org.fusesource.cloudmix.tests.broker")
                    .ownsMachine().maximumInstances("1");

        FeatureDetails producer = createFeatureDetails("org.fusesource.cloudmix.tests.producer",
                "scan-features:mvn:org.fusesource.cloudmix/org.apache.activemq.producer/"
                + version + "/xml/features!/org.fusesource.cloudmix.tests.producer")
                    .depends(broker).maximumInstances("2");
        FeatureDetails consumer = createFeatureDetails("org.fusesource.cloudmix.tests.consumer",
                "scan-features:mvn:org.fusesource.cloudmix/org.apache.activemq.consumer/" 
                + version + "/xml/features!/org.fusesource.cloudmix.tests.consumer")
                    .depends(broker).maximumInstances("3");

        addFeatures(broker, producer, consumer);
    }

}
