/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.usecase;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import org.fusesource.cloudmix.agent.GridControllerClient;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;
import org.fusesource.cloudmix.common.jetty.WebServer;

public final class ProvisioningDemo {
    private ProvisioningDemo() {
        //not constructed
    }
    public static void main(String[] args) throws Exception {
        WebServer webServer = new WebServer();
        webServer.start();

        GridControllerClient gridController = new GridControllerClient();

        FeatureDetails broker = new FeatureDetails("org.apache.activemq.broker.multicast",
            "mvn:org.fusesource.cloudmix/org.apache.activemq.broker.multicast/1.0-SNAPSHOT/xml/feature")
            .ownsMachine().maximumInstances("1");

        FeatureDetails producer = new FeatureDetails("org.apache.activemq.producer",
            "mvn:org.fusesource.cloudmix/org.apache.activemq.producer/1.0-SNAPSHOT/xml/feature")
            .depends(broker).maximumInstances("2");
        FeatureDetails consumer = new FeatureDetails("org.apache.activemq.consumer",
            "mvn:org.fusesource.cloudmix/org.apache.activemq.consumer/1.0-SNAPSHOT/xml/feature")
            .depends(broker).maximumInstances("3");

        gridController.addFeatures(broker, producer, consumer);

        while (true) {
            Thread.sleep(5000);
            Collection<AgentDetails> agents = gridController.getClient().getAllAgentDetails();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(baos);
            for (AgentDetails agent : agents) {
                writer.println("Agent: " + agent.getId());
                ProvisioningHistory history = gridController.getClient().getAgentHistory(agent.getId());
                List<ProvisioningAction> list = history.getActions();
                for (ProvisioningAction action : list) {
                    writer.println(">>>> " + action.getCommand()
                                   + " " + action.getFeature()
                                   + " " + action.getResource());
                }
            }
            writer.println();
            writer.close();
            System.err.println(baos.toString());
        }
    }
}
