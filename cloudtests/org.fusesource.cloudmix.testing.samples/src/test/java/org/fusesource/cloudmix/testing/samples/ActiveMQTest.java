/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.testing.samples;

import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.testing.TestController;
import org.junit.Test;

import java.util.Properties;
import java.util.Map;

/**
 * @version $Revision: 1.1 $
 */
public class ActiveMQTest extends TestController {

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

        FeatureDetails broker = createFeatureDetails("org.apache.activemq.broker.multicast",
                "scan-features:mvn:org.fusesource.cloudmix/org.apache.activemq.broker.multicast/" + version + "/xml/features!/org.apache.activemq.broker.multicast").ownsMachine().maximumInstances("1");

        FeatureDetails producer = createFeatureDetails("org.apache.activemq.producer",
                "scan-features:mvn:org.fusesource.cloudmix/org.apache.activemq.producer/" + version + "/xml/features!/org.apache.activemq.producer").depends(broker).maximumInstances("2");
        FeatureDetails consumer = createFeatureDetails("org.apache.activemq.consumer",
                "scan-features:mvn:org.fusesource.cloudmix/org.apache.activemq.consumer/" + version + "/xml/features!/org.apache.activemq.consumer").depends(broker).maximumInstances("3");

        addFeatures(broker, producer, consumer);
    }

}
