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

import com.sun.jersey.api.client.UniformInterfaceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.ProcessClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.testing.TestController;
import org.junit.Test;
import static org.junit.Assert.*;



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
        assertTrue("has some agents", !agents.isEmpty());

        for (AgentDetails agent : agents) {
            System.out.println("Broker agent: " + agent.getHostname());
        }

        System.out.println("Configuration Properties = {");
        Properties properties = getConfigurationProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.out.println("  " + entry.getKey() + " " + entry.getValue());
        }
        System.out.println("}");

        assertEquals("Number of configuration properties", 1, properties.size());
        String brokerUrl = properties.getProperty("broker.url");
        assertNotNull("should have broker.url configuration property", brokerUrl);
        System.out.println("BrokerURL: " + brokerUrl);



        // show the processes
        List<? extends ProcessClient> producerProcesses = getProcessClientsFor(producer);
        assertEquals("size of producer processes", 1, producerProcesses.size());
        ProcessClient processClient = producerProcesses.get(0);
        assertNotNull("Should have a processClient for a producer", processClient);
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
        assertNotNull("Should not have a null log!", log);
        System.out.println("Process Log >>>>");
        System.out.println(log);

        Thread.sleep(10000);
    }

    public Properties getConfigurationProperties() {
        return gridClient.getProperties(profileId);
    }


    protected void installFeatures() {
/*
        Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            System.out.println(" " + entry.getKey() + " = " + entry.getValue());
        }
*/
        // TODO get this from system properties?
        String version = "1.3-SNAPSHOT";

        broker = createFeatureDetails("amq-test-broker",
                "mop:jar org.fusesource.cloudmix:org.fusesource.cloudmix.tests.broker:" + version)
                .maximumInstances("1")
                .property("broker.url", "Strings.mkString(('tcp://' + hostname + ':61616' in agents), 'failover:(', ',', ')?maxReconnectAttempts=2')");

        producer = createFeatureDetails("amq-test-producer",
                "mop:jar org.fusesource.cloudmix:org.fusesource.cloudmix.tests.producer:" + version)
                .depends(broker).maximumInstances("2");

        consumer = createFeatureDetails("amq-test-consumer",
                "mop:jar org.fusesource.cloudmix:org.fusesource.cloudmix.tests.consumer:" + version)
                .depends(broker).maximumInstances("3");

        addFeatures(broker, producer, consumer);
    }
}