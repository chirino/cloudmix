/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.AgentPoller;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.agent.util.CompositeCallable;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A helper class for testing which represents a cluster of Agents to test out the provisioning
 *
 * @version $Revision$
 */
public class AgentCluster implements InitializingBean, DisposableBean {
    private static final transient Log LOG = LogFactory.getLog(AgentCluster.class);
    
    private List<InstallerAgent> agents = new ArrayList<InstallerAgent>();
    
    class TestInstallerAgent extends InstallerAgent {

        private Set<String> features;
        
        public TestInstallerAgent() {
            super();
            features = new HashSet<String>();
        }
        
        public Set<String> getInstalledFeatures() {
            return features;
        }
       
        @Override
        protected void onProvisioningHistoryChanged(ProvisioningHistory aProvisioningHistory) {
            super.onProvisioningHistoryChanged(aProvisioningHistory);
            List<ProvisioningAction> actions = aProvisioningHistory.getActions();
            if (actions != null) {
                for (ProvisioningAction action : actions) {
                    String featureId = action.getFeature();
                    if (featureId == null) {
                        LOG.warn("Igmored null feature ID for " + action);
                        continue;
                    }

                    if (ProvisioningAction.INSTALL_COMMAND.equals(action.getCommand())) {
                        features.add(featureId);
                        addAgentFeature(new Feature(featureId));
                    } else if (ProvisioningAction.UNINSTALL_COMMAND.equals(action.getCommand())) {
                        features.remove(featureId);
                        removeFeatureId(featureId);
                    } else {
                        throw new RuntimeException("Unknown action");
                    }
                }
            }
            //updateAgentDetails();
        }   

/*
        @Override
        public AgentDetails updateAgentDetails() {
            AgentDetails details = super.updateAgentDetails();
            details.setCurrentFeatures(getInstalledFeatures());

            try {
                getClient().updateAgentDetails(getAgentId(), details);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return details;
        }
*/
    }
    

    
    @SuppressWarnings("unchecked")
    private AgentPoller poller = new AgentPoller(new CompositeCallable(agents));

    /**
     * Creates install agents for the given host names
     *
     * @return the newly created agents
     */
    public List<InstallerAgent> createInstallAgents(String... hostnames) {
        List<InstallerAgent> answer = new ArrayList<InstallerAgent>();
        for (String hostname : hostnames) {
            InstallerAgent agent = new TestInstallerAgent();
            agent.setHostName(hostname);
            answer.add(agent);
        }
        agents.addAll(answer);
        return answer;
    }

    /**
     * Creates the given number of new install agents and returns the created agents
     *
     * @return the newly created agents
     */
    public List<InstallerAgent> createInstallAgents(int count) throws Exception {
        List<InstallerAgent> answer = new ArrayList<InstallerAgent>();
        for (int i = 0; i < count; i++) {
            InstallerAgent agent = new TestInstallerAgent();
            answer.add(agent);
        }
        agents.addAll(answer);
        return answer;
    }

    /** 
     * Creates install agents for the specified profiles. Profiles could be specified
     * multiple times to create multiple agents in the same profile.
     * 
     * @return the newly created agents
     */
    public Collection<InstallerAgent> createInstallAgentsInProfiles(String ... profiles) throws Exception {
        List<InstallerAgent> answer = new ArrayList<InstallerAgent>();
        for (String p : profiles) {
            InstallerAgent agent = new InstallerAgent();
            agent.setProfile(p);
            answer.add(agent);
        }
        agents.addAll(answer);
        return answer;        
    }
    
    /**
     * Returns a collection of all the agents in the cluster.
     */
    public Collection<InstallerAgent> getAgents() {
        return agents;
    }

    /**
     * Removes the given agent
     */
    public boolean removeAgent(InstallerAgent agent) {
        System.out.println("Killing agent: " + agent);
        return agents.remove(agent);
    }

    /**
     * Removes all of the given agents
     */
    public void removeAgents(Collection<InstallerAgent> someAgents) {
        this.agents.removeAll(someAgents);
        System.out.println("Killing agents: " + someAgents);
    }

    public void dumpAgents() throws Exception {
        for (InstallerAgent agent : agents) {
            dumpAgent(agent);
        }
    }

    public void dumpAgent(InstallerAgent agent) throws Exception {
        System.out.println("Agent: " + agent.getAgentId());
        ProvisioningHistory history = agent.getProvisioningHistory();

        Assert.assertNotNull("Should have a provision history for agent: " + agent, history);
        List<ProvisioningAction> list = history.getActions();
        for (ProvisioningAction action : list) {
            System.out.println(">>>> " + action.getCommand()
                               + " " + action.getFeature()
                               + " " + action.getResource());
        }
        System.out.println("Features installed: ");
        Set<String> currentFeatures = agent.getAgentDetails().getCurrentFeatures();
        if (currentFeatures != null) {
            for (String f : currentFeatures) {
                System.out.println("      " + f);
            }
        }
        System.out.println();
    }

    public void afterPropertiesSet() throws Exception {
        poller.afterPropertiesSet();
    }

    public void destroy() throws Exception {
        poller.destroy();
    }

    public List<InstallerAgent> agentsWithFeature(String featureId) {
        List<InstallerAgent> answer = new ArrayList<InstallerAgent>();
        for (InstallerAgent agent : agents) {
            if (ProvisioningTestSupport.agentFeatureCount(agent, featureId) > 0) {
                answer.add(agent);
            }
        }
        return answer;
    }


    public InstallerAgent firstAgentForFeature(String featureId) {
        List<InstallerAgent> list = agentsWithFeature(featureId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public String firstAgentHostNameForFeature(String featureId) {
        InstallerAgent agent = firstAgentForFeature(featureId);
        return (agent != null) ? agent.getHostName() : null;
    }

    public int featureInstanceCount(String featureId) {
        int actual = 0;
        for (InstallerAgent agent : agents) {
            actual += ProvisioningTestSupport.agentFeatureCount(agent, featureId);
        }
        return actual;
    }

    // Assertions
    //-------------------------------------------------------------------------
    public void assertMaximumFeaturesPerAgent(int expected) {
        for (InstallerAgent agent : agents) {
            assertMaximumFeatures(agent, expected);
        }
    }

    public void assertMaximumFeatures(InstallerAgent agent, int expected) {
        Collection<ProvisioningAction> actions = ProvisioningTestSupport.agentInstallActions(agent);        
        int actual = actions.size();
        Assert.assertTrue("Number of features for agent: " + agent
                              + " with actions: " + actions
                              + " expected maximum: " + expected
                              + " actual: " + actual,
                          expected >= actual);
    }

    public void assertFeatureInstances(String featureId, int expected) {
        int actual = featureInstanceCount(featureId);
        Assert.assertEquals("Number of instances of: " + featureId, expected, actual);
    }

    public void assertFeatureCount(InstallerAgent agent, String featureId, int expected) {
        int actual = ProvisioningTestSupport.agentFeatureCount(agent, featureId);
        Assert.assertTrue("Instances of feature: " + featureId
                              + " for agent: " + agent
                              + " expected maximum: " + expected
                              + " actual: " + actual,
                          expected >= actual);
    }

    public void assertFirstAgentHostNameForFeature(String featureId, String expected) {
        String actual = firstAgentHostNameForFeature(featureId);
        Assert.assertEquals("First host name for feature: " + featureId, expected, actual);
    }    
}