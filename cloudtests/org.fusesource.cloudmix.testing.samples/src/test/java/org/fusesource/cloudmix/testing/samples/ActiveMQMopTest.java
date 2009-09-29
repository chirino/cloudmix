/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.testing.samples;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.client.UniformInterfaceException;

import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.common.ProcessClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.testing.TestController;

import org.junit.Assert;
import org.junit.Test;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @version $Revision$
 */
public class ActiveMQMopTest extends TestController {
    private static final transient Log LOG = LogFactory.getLog(ActiveMQMopTest.class);

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


        // show the processes
        List<? extends ProcessClient> producerProcesses = getProcessClientsFor(producer);
        Assert.assertEquals("size of producer processes", 1, producerProcesses.size());
        ProcessClient processClient = producerProcesses.get(0);
        Assert.assertNotNull("Should have a processClient for a producer", processClient);
        System.out.println("ProcessClient: " + processClient);

        // now lets get the log so far!
        String log = null;
        for (int i = 0; i < 20; i++) {
            if (i > 0) {
                Thread.sleep(5000);
                LOG.info("Reattempting to get the log");
            }
            try {
                log = processClient.directoryResource("output.log").get(String.class);
                if (log != null) {
                    break;
                }
            } catch (UniformInterfaceException e) {
                LOG.warn("Failed to find log " + e);
            }
        }
        Assert.assertNotNull("Should not have a null log!", log);
        System.out.println("Process Log >>>>");
        System.out.println(log);

        Thread.sleep(10000);
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

    @Override
    protected RestGridClient createGridController() throws URISyntaxException {
        RestGridClient answer = super.createGridController();
        answer.getClient(null).addFilter(new LoggingFilter());
        return answer;

    }
}