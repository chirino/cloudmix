/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.testing.samples;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.testing.TestController;

import org.junit.Assert;
import org.junit.Test;


/**
 * @version $Revision: 1.1 $
 */
public class ActiveMQMopTest extends TestController {
    protected FeatureDetails broker;
    protected FeatureDetails producer;
    protected FeatureDetails consumer;

    @Test
    public void testScenarioDeploys() throws Exception {
        System.out.println("TEST NAME: " + getTestName());

        checkProvisioned();

        System.out.println("Worked!!!");

        List<AgentDetails> agents = getAgentsFor(broker);
        Assert.assertTrue("has some agents", !agents.isEmpty());

        for (AgentDetails agent : agents) {
            System.out.println("Broker agent: " + agent.getHostname());
        }

        Thread.sleep(1000000);
    }


    protected void installFeatures() {
        // TODO get this from system properties?
        Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.out.println(" " + entry.getKey() + " = " + entry.getValue());
        }
        String version = "1.3-SNAPSHOT";

        broker = createFeatureDetails("amq-test-broker",
                "mop:jar org.fusesource.cloudmix:org.fusesource.cloudmix.tests.broker:" + version)
                    .maximumInstances("1");

        producer = createFeatureDetails("amq-test-producer",
                "mop:jar org.fusesource.cloudmix:org.fusesource.cloudmix.tests.producer:" + version)
                    .depends(broker).maximumInstances("2");

        consumer = createFeatureDetails("amq-test-consumer",
                "mop:jar org.fusesource.cloudmix:org.fusesource.cloudmix.tests.consumer:" + version)
                    .depends(broker).maximumInstances("3");

        addFeatures(broker, producer, consumer);
    }

}