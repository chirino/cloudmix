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
        FeatureDetails broker = createFeatureDetails("org.apache.activemq.broker.multicast",
                "mvn:org.fusesource.cloudmix/org.apache.activemq.broker.multicast/1.0-SNAPSHOT/xml/feature").ownsMachine().maximumInstances("1");

        FeatureDetails producer = createFeatureDetails("org.apache.activemq.producer",
                "mvn:org.fusesource.cloudmix/org.apache.activemq.producer/1.0-SNAPSHOT/xml/feature").depends(broker).maximumInstances("2");
        FeatureDetails consumer = createFeatureDetails("org.apache.activemq.consumer",
                "mvn:org.fusesource.cloudmix/org.apache.activemq.consumer/1.0-SNAPSHOT/xml/feature").depends(broker).maximumInstances("3");

        addFeatures(broker, producer, consumer);
    }

}
