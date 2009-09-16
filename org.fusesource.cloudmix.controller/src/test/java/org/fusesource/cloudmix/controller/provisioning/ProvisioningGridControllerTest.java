/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.controller.AgentController;
import org.fusesource.cloudmix.common.controller.FeatureController;
import org.fusesource.cloudmix.common.controller.ProfileController;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;
import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;

public class ProvisioningGridControllerTest extends TestCase {
    public void testStartupDelay() {
        ProvisioningGridController pgc = new ProvisioningGridController();
        
        pgc.setStartupProvisioningDelay(12345);
        assertEquals(12345, pgc.getStartupProvisioningDelay());
    }
    
    public void testPoller() throws Exception {
        ProvisioningGridController pgc = new ProvisioningGridController();
        long initialDelay = 10000000000L;
        pgc.setStartupProvisioningDelay(initialDelay); // never actually run it.

        assertNull("Precondition failed", pgc.poller);
        try {
            pgc.afterPropertiesSet();
            assertNotNull(pgc.poller);
            assertEquals(initialDelay, pgc.poller.getInitialPollingDelay());
        } finally {
            pgc.destroy();             
        }
    }
    
    public void testDestroyBeforePollerCreated() throws Exception {
        ProvisioningGridController pgc = new ProvisioningGridController();
        pgc.setStartupProvisioningDelay(10000000000L); // never actually run it.
        
        pgc.destroy(); // should not produce an error.        
    }

    @SuppressWarnings("unchecked")
    public void testPollCall() throws Exception {
        // have 2 profiles (testing & production)
        // have 3 agents (one for production two for testing)
        // have 3 features (two assigned to testing none to production) 
        
        ProvisioningGridController pgc = new ProvisioningGridController() {
            @Override
            public long getAgentTimeout() {
                return 1000000;
            }
        };
        
        Map<String, Map<String, String>> cfgOverrides = new HashMap<String, Map<String, String>>(2);
        Map<String, String> props = new HashMap<String, String>(2);
        props.put("prop11", "val11");
        props.put("prop12", "val12");
        cfgOverrides.put("f1", props);
        
        addProfileToController(pgc, "testing", new String[] {"f1", "f3" }, cfgOverrides);
        addProfileToController(pgc, "production", new String[0], null);
        
        FeatureDetails f2FD = new FeatureDetails("f2");
        pgc.getDataProvider().addFeature("f2", new FeatureController(pgc, f2FD));

        AgentDetails a1AD = new AgentDetails();
        a1AD.setId("a1");
        a1AD.setProfile("testing");
        AgentController a1AC = new AgentController(pgc, a1AD);
        a1AC.setHistory(new ProvisioningHistory());
        a1AC.markActive();
        pgc.getDataProvider().addAgent("a1", a1AC);

        AgentDetails a2AD = new AgentDetails();
        a2AD.setId("a2");
        a2AD.setProfile("production");
        AgentController a2AC = new AgentController(pgc, a2AD);
        a2AC.setHistory(new ProvisioningHistory());
        a2AC.markActive();
        pgc.getDataProvider().addAgent("a2", a2AC);
        
        AgentDetails a3AD = new AgentDetails();
        a3AD.setId("a3");
        a3AD.setProfile("testing");
        AgentController a3AC = new AgentController(pgc, a3AD);
        a3AC.setHistory(new ProvisioningHistory());
        a3AC.markActive();
        pgc.getDataProvider().addAgent("a3", a3AC);

        List<ProvisioningAction> result = (List<ProvisioningAction>) pgc.call(); // do da bizniz
        
        assertEquals(2, result.size());
        for (int i = 0; i < result.size(); i++) {
            ProvisioningAction pa = result.get(i);
            assertEquals("install", pa.getCommand());
            
            switch (i) {
            case 0:
                assertEquals("f1", pa.getFeature());
                assertEquals(2, pa.getCfgUpdates().size());
                assertEquals("prop11", pa.getCfgUpdates().get(0).getProperty());
                assertEquals("val11", pa.getCfgUpdates().get(0).getValue());
                assertEquals("prop12", pa.getCfgUpdates().get(1).getProperty());
                assertEquals("val12", pa.getCfgUpdates().get(1).getValue());
                break;
            case 1:
                assertEquals("f3", pa.getFeature());
                assertEquals(0, pa.getCfgUpdates().size());
                break;
            default:
            }
        }
        
        assertEquals(1, pgc.getAgentsAssignedToFeature("f1").size());
        assertEquals(0, pgc.getAgentsAssignedToFeature("f2").size());
        assertEquals(1, pgc.getAgentsAssignedToFeature("f3").size());   
        
        assertEquals("a3", pgc.getAgentsAssignedToFeature("f1").get(0));
        assertEquals("a1", pgc.getAgentsAssignedToFeature("f3").get(0));
        
    }
    
    public void testProfileDeletion() throws Exception {
        // have 2 profiles (testing & production)
        // have 3 agents (one for production two for testing)
        // have 3 features (two assigned to testing 1 to production) 
        
        ProvisioningGridController pgc = new ProvisioningGridController() {
            @Override
            public long getAgentTimeout() {
                return 1000000;
            }
            
        };
        
        Map<String, Map<String, String>> cfgOverrides = new HashMap<String, Map<String, String>>(2);
        Map<String, String> props = new HashMap<String, String>(2);
        props.put("prop11", "val11");
        props.put("prop12", "val12");
        cfgOverrides.put("f1", props);
        
        ProfileController p1Pc = addProfileToController(pgc, "testing", new String[] {"f1", "f3" }, null);
        addProfileToController(pgc, "production", new String[]  {"f2"}, null);
        
        FeatureDetails f2FD = new FeatureDetails("f2");
        pgc.getDataProvider().addFeature("f2", new FeatureController(pgc, f2FD));

        AgentDetails a1AD = new AgentDetails();
        a1AD.setId("a1");
        a1AD.setProfile("testing");
        AgentController a1AC = new AgentController(pgc, a1AD);
        a1AC.setHistory(new ProvisioningHistory());
        a1AC.markActive();
        pgc.getDataProvider().addAgent("a1", a1AC);

        AgentDetails a2AD = new AgentDetails();
        a2AD.setId("a2");
        a2AD.setProfile("production");
        AgentController a2AC = new AgentController(pgc, a2AD);
        a2AC.setHistory(new ProvisioningHistory());
        a2AC.markActive();
        pgc.getDataProvider().addAgent("a2", a2AC);
        
        AgentDetails a3AD = new AgentDetails();
        a3AD.setId("a3");
        a3AD.setProfile("testing");
        AgentController a3AC = new AgentController(pgc, a3AD);
        a3AC.setHistory(new ProvisioningHistory());
        a3AC.markActive();
        pgc.getDataProvider().addAgent("a3", a3AC);

        pgc.call(); // do da bizniz
        
        assertEquals(1, pgc.getAgentsAssignedToFeature("f1").size());
        assertEquals(1, pgc.getAgentsAssignedToFeature("f2").size());
        assertEquals(1, pgc.getAgentsAssignedToFeature("f3").size());   
        
        assertEquals(1, a1AC.getFeatures().size());
        assertEquals(1, a2AC.getFeatures().size());
        assertEquals(1, a3AC.getFeatures().size());
        
        assertEquals(1, a1AC.getHistory().getActions().size());
        assertEquals(1, a2AC.getHistory().getActions().size());
        assertEquals(1, a3AC.getHistory().getActions().size());
        
        assertEquals("install", a1AC.getHistory().getActions().get(0).getCommand());
        assertEquals("install", a2AC.getHistory().getActions().get(0).getCommand());
        assertEquals("install", a3AC.getHistory().getActions().get(0).getCommand());

        
        // now dump 1 profile and see what happens
        pgc.getDataProvider().getProfiles().remove(p1Pc);
        
        pgc.call(); // do da bizniz... again...
        
        assertEquals(0, pgc.getAgentsAssignedToFeature("f1").size());
        assertEquals(1, pgc.getAgentsAssignedToFeature("f2").size());
        assertEquals(0, pgc.getAgentsAssignedToFeature("f3").size());   
        
        assertEquals(0, a1AC.getFeatures().size());
        assertEquals(1, a2AC.getFeatures().size());
        assertEquals(0, a3AC.getFeatures().size());
        
        assertEquals(2, a1AC.getHistory().getActions().size());
        assertEquals(1, a2AC.getHistory().getActions().size());
        assertEquals(2, a3AC.getHistory().getActions().size());
        
        assertEquals("uninstall", a1AC.getHistory().getActions().get(1).getCommand());
        assertEquals("uninstall", a3AC.getHistory().getActions().get(1).getCommand());
        
        assertEquals(a1AC.getHistory().getActions().get(0).getFeature(),
                     a1AC.getHistory().getActions().get(1).getFeature());
        assertEquals(a3AC.getHistory().getActions().get(0).getFeature(),
                     a3AC.getHistory().getActions().get(1).getFeature());
    }

    //CHECKSTYLE:OFF - this testcase is verylong, probably should be broken up a bit
    public void testProfileUpdate() throws Exception {
        // have 2 profiles (testing & production)
        // have 3 agents (one for production two for testing)
        // have 3 features (two assigned to testing 1 to production) 
        
        ProvisioningGridController pgc = new ProvisioningGridController() {
            @Override
            public long getAgentTimeout() {
                return 1000000;
            }
            
        };
        
        Map<String, Map<String, String>> cfgOverrides = new HashMap<String, Map<String, String>>(2);
        Map<String, String> props = new HashMap<String, String>(2);
        props.put("prop11", "val11");
        props.put("prop12", "val12");
        cfgOverrides.put("f1", props);
        
        addProfileToController(pgc, "testing", new String[] {"f1", "f3" }, null);
        ProfileController p2Pc = addProfileToController(pgc, "production", new String[]  {"f2"}, null);
        
        FeatureDetails f2FD = new FeatureDetails("f2");
        pgc.getDataProvider().addFeature("f2", new FeatureController(pgc, f2FD));

        AgentDetails a1AD = new AgentDetails();
        a1AD.setId("a1");
        a1AD.setProfile("testing");
        AgentController a1AC = new AgentController(pgc, a1AD);
        a1AC.setHistory(new ProvisioningHistory());
        a1AC.markActive();
        pgc.getDataProvider().addAgent("a1", a1AC);

        AgentDetails a2AD = new AgentDetails();
        a2AD.setId("a2");
        a2AD.setProfile("production");
        AgentController a2AC = new AgentController(pgc, a2AD);
        a2AC.setHistory(new ProvisioningHistory());
        a2AC.markActive();
        pgc.getDataProvider().addAgent("a2", a2AC);
        
        AgentDetails a3AD = new AgentDetails();
        a3AD.setId("a3");
        a3AD.setProfile("testing");
        AgentController a3AC = new AgentController(pgc, a3AD);
        a3AC.setHistory(new ProvisioningHistory());
        a3AC.markActive();
        pgc.getDataProvider().addAgent("a3", a3AC);

        pgc.call(); // do da bizniz
        
        assertEquals(1, pgc.getAgentsAssignedToFeature("f1").size());
        assertEquals(1, pgc.getAgentsAssignedToFeature("f2").size());
        assertEquals(1, pgc.getAgentsAssignedToFeature("f3").size());   
        
        assertEquals(1, a1AC.getFeatures().size());
        assertEquals(1, a2AC.getFeatures().size());
        assertEquals(1, a3AC.getFeatures().size());
        
        assertEquals("f2", a2AC.getFeatures().iterator().next());

        assertEquals(1, a1AC.getHistory().getActions().size());
        assertEquals(1, a2AC.getHistory().getActions().size());
        assertEquals(1, a3AC.getHistory().getActions().size());
        
        assertEquals("install", a1AC.getHistory().getActions().get(0).getCommand());
        assertEquals("install", a2AC.getHistory().getActions().get(0).getCommand());
        assertEquals("install", a3AC.getHistory().getActions().get(0).getCommand());
        
        // now modify a profile and see what happens
        p2Pc.getDetails().getFeatures().remove(0);
        p2Pc.getDetails().getFeatures().add(new Dependency("f4"));
        p2Pc.setChanged(true);
        pgc.getDataProvider().addFeature("f4", new FeatureController(pgc, new FeatureDetails("f4")));
        
        pgc.call(); // do da bizniz... again...
        
        // the 1st pass should only clear the redundant feature (f4 will be installed in a subsequent pass)
        assertEquals(1, pgc.getAgentsAssignedToFeature("f1").size());
        assertEquals(0, pgc.getAgentsAssignedToFeature("f2").size());
        assertEquals(1, pgc.getAgentsAssignedToFeature("f3").size());   
        assertEquals(0, pgc.getAgentsAssignedToFeature("f4").size()); 
        
        assertEquals(1, a1AC.getFeatures().size());
        assertEquals(0, a2AC.getFeatures().size());
        assertEquals(1, a3AC.getFeatures().size());
        
        assertEquals(0, a2AC.getFeatures().size());
        
        assertEquals(1, a1AC.getHistory().getActions().size());
        assertEquals(2, a2AC.getHistory().getActions().size());
        assertEquals(1, a3AC.getHistory().getActions().size());
        
        assertEquals("uninstall", a2AC.getHistory().getActions().get(1).getCommand());
        
        assertEquals("f2", a2AC.getHistory().getActions().get(0).getFeature());
        assertEquals("f2", a2AC.getHistory().getActions().get(1).getFeature());
        
        pgc.call(); // do da bizniz... one last time...
        
        // the 2nd pass should install f4
        assertEquals(1, pgc.getAgentsAssignedToFeature("f1").size());
        assertEquals(0, pgc.getAgentsAssignedToFeature("f2").size());
        assertEquals(1, pgc.getAgentsAssignedToFeature("f3").size());   
        assertEquals(1, pgc.getAgentsAssignedToFeature("f4").size());   
        
        assertEquals(1, a1AC.getFeatures().size());
        assertEquals(1, a2AC.getFeatures().size());
        assertEquals(1, a3AC.getFeatures().size());
        
        assertEquals("f4", a2AC.getFeatures().iterator().next());
        
        assertEquals(1, a1AC.getHistory().getActions().size());
        assertEquals(3, a2AC.getHistory().getActions().size());
        assertEquals(1, a3AC.getHistory().getActions().size());
        
        assertEquals("uninstall", a2AC.getHistory().getActions().get(1).getCommand());
        assertEquals("install", a2AC.getHistory().getActions().get(2).getCommand());
        
        assertEquals("f2", a2AC.getHistory().getActions().get(1).getFeature());
        assertEquals("f4", a2AC.getHistory().getActions().get(2).getFeature());
    }
    //CHECKSTYLE:ON

    public void testAgentDeactivation() throws Exception {
        // have 1 profile
        // have 2 agents
        // have 2 features
        
        ProvisioningGridController pgc = new ProvisioningGridController() {
            @Override
            public long getAgentTimeout() {
                return 1000000;
            }
        };
        
        addProfileToController(pgc, "testing", new String[] {"f1", "f2" }, null);
        
        FeatureDetails f2FD = new FeatureDetails("f2");
        pgc.getDataProvider().addFeature("f2", new FeatureController(pgc, f2FD));

        AgentDetails a1AD = new AgentDetails();
        a1AD.setId("a1");
        a1AD.setProfile("testing");
        AgentController a1AC = new AgentController(pgc, a1AD);
        a1AC.setHistory(new ProvisioningHistory());
        a1AC.markActive();
        pgc.getDataProvider().addAgent("a1", a1AC);

        AgentDetails a2AD = new AgentDetails();
        a2AD.setId("a2");
        a2AD.setProfile("testing");
        AgentController a2AC = new AgentController(pgc, a2AD);
        a2AC.setHistory(new ProvisioningHistory());
        a2AC.markActive();
        pgc.getDataProvider().addAgent("a2", a2AC);

        pgc.call(); // do da bizniz

        assertEquals(1, pgc.getAgentsAssignedToFeature("f1").size());
        assertEquals(1, pgc.getAgentsAssignedToFeature("f2").size());   
        
        String featureThatShouldEndUpUninstalled = "f1";
        if ("a1".equals(pgc.getAgentsAssignedToFeature("f1").get(0))) {
            assertEquals("a2", pgc.getAgentsAssignedToFeature("f2").get(0));   
            featureThatShouldEndUpUninstalled = "f2";
        } else if ("a2".equals(pgc.getAgentsAssignedToFeature("f1").get(0))) {
            assertEquals("a1", pgc.getAgentsAssignedToFeature("f2").get(0));    
            featureThatShouldEndUpUninstalled = "f1";
        } else {
            fail();
        }
        
        
        
        // now de-activate 1 agent and see what happens
        a2AC.deActivate();
        
        pgc.call(); // do da bizniz... again...
        
        assertEquals(0, pgc.getAgentsAssignedToFeature(featureThatShouldEndUpUninstalled).size());
        String stillInstalledFeature = "f2".equals(featureThatShouldEndUpUninstalled) ? "f1" : "f2";
        assertEquals(1, pgc.getAgentsAssignedToFeature(stillInstalledFeature).size());   
        
        AgentController relevantAc =
            "a1".equals(pgc.getAgentsAssignedToFeature(stillInstalledFeature).get(0)) ? a2AC : a1AC;
        
        ProvisioningAction pa =
            relevantAc.getHistory().getActions().get(relevantAc.getHistory().getActions().size() - 1);
        assertEquals("uninstall", pa.getCommand());
        assertEquals(featureThatShouldEndUpUninstalled, pa.getFeature());
        assertEquals(1, relevantAc.getHistory().getCfgUpdates().size());
        assertEquals("agent.force.register", relevantAc.getHistory().getCfgUpdates().get(0).getProperty());
        assertEquals("true", relevantAc.getHistory().getCfgUpdates().get(0).getValue());
    }

    @SuppressWarnings("unchecked")
    public void testRedeployAfterProfileUpdateCall() throws Exception {
        
        ProvisioningGridController pgc = new ProvisioningGridController() {
            @Override
            public long getAgentTimeout() {
                return 1000000;
            }
        };
        
        Map<String, Map<String, String>> cfgOverrides = new HashMap<String, Map<String, String>>(2);
        Map<String, String> props = new HashMap<String, String>(2);
        props.put("prop11", "val11");
        props.put("prop12", "val12");
        cfgOverrides.put("f1", props);
        
        
        ProfileController pc = addProfileToController(pgc,
                                                      "testing",
                                                      new String[] {"f1", "f3" },
                                                      cfgOverrides);
        
        FeatureDetails f2FD = new FeatureDetails("f2");
        pgc.getDataProvider().addFeature("f2", new FeatureController(pgc, f2FD));

        AgentDetails a1AD = new AgentDetails();
        a1AD.setId("a1");
        a1AD.setProfile("testing");
        a1AD.setMaximumFeatures(10);
        AgentController a1AC = new AgentController(pgc, a1AD);
        a1AC.setHistory(new ProvisioningHistory());
        a1AC.markActive();
        pgc.getDataProvider().addAgent("a1", a1AC);
        
        List<ProvisioningAction> result = (List<ProvisioningAction>) pgc.call(); // do da bizniz
        
        assertEquals(2, result.size());
        
        assertEquals(2, a1AC.getHistory().getActions().size());
        
        ProvisioningAction pa = a1AC.getHistory().getActions().get(0);
        assertEquals("install", pa.getCommand());
        assertEquals("f1", pa.getFeature());
        assertEquals(2, pa.getCfgUpdates().size());
        assertEquals("prop11", pa.getCfgUpdates().get(0).getProperty());
        assertEquals("val11", pa.getCfgUpdates().get(0).getValue());
        assertEquals("prop12", pa.getCfgUpdates().get(1).getProperty());
        assertEquals("val12", pa.getCfgUpdates().get(1).getValue());

        pa = a1AC.getHistory().getActions().get(1);
        assertEquals("install", pa.getCommand());
        assertEquals("f3", pa.getFeature());
        assertEquals(0, pa.getCfgUpdates().size());
        
        pc.setChanged(true);
        Dependency dep = pc.getDetails().getFeatures().get(1);
        dep.setChanged(true);
        dep.addCfgOverride(new ConfigurationUpdate("prop31", "val31"));
        
        result = (List<ProvisioningAction>) pgc.call(); // do da bizniz
        
        assertEquals(1, result.size());
        
        assertEquals(4, a1AC.getHistory().getActions().size());
        
        pa = a1AC.getHistory().getActions().get(2);
        assertEquals("uninstall", pa.getCommand());
        assertEquals("f3", pa.getFeature());
        assertEquals(0, pa.getCfgUpdates().size());

        pa = a1AC.getHistory().getActions().get(3);
        assertEquals("install", pa.getCommand());
        assertEquals("f3", pa.getFeature());
        assertEquals(1, pa.getCfgUpdates().size());
        assertEquals("prop31", pa.getCfgUpdates().get(0).getProperty());
        assertEquals("val31", pa.getCfgUpdates().get(0).getValue());
        
        assertFalse(pc.hasChanged());
        assertFalse(dep.hasChanged());
        
        pc.setChanged(true);
        result = (List<ProvisioningAction>) pgc.call(); // do da bizniz
        assertEquals(0, result.size());
        assertEquals(4, a1AC.getHistory().getActions().size());
        
        pc.getDetails().getFeatures().remove(1);
        result = (List<ProvisioningAction>) pgc.call(); // do da bizniz
        assertEquals(0, result.size());
        assertEquals(5, a1AC.getHistory().getActions().size());

        pa = a1AC.getHistory().getActions().get(4);
        assertEquals("uninstall", pa.getCommand());
        assertEquals("f3", pa.getFeature());
        assertEquals(0, pa.getCfgUpdates().size());

    }
    
    public void testFeatureUpdate() throws Exception {
        // have 1 profile, the default one
        // the agent has 1 pre-deployed feature (called f1)
        // there is a new feature being deployed f2, which obsoletes f0 & f1
        // the agent should get f1 uninstalled and f2 installed
        
        ProvisioningGridController pgc = new ProvisioningGridController();
        
        ProfileDetails defPD = new ProfileDetails("default");
        defPD.getFeatures().add(new Dependency("f2"));
        ProfileController defPC = new ProfileController(pgc, defPD);
        pgc.getDataProvider().addProfile("default", defPC);
        
        FeatureDetails f2FD = new FeatureDetails("f2");
        FeatureController f2FC = new FeatureController(pgc, f2FD);
//        f2FC.setReplacesFeatures("f0", "f1");
        pgc.getDataProvider().addFeature("f2", f2FC);
        
        AgentDetails a1AD = new AgentDetails();
        a1AD.setId("a1");
        a1AD.setMaximumFeatures(1);
        AgentController a1AC = new AgentController(pgc, a1AD);
        a1AC.getFeatures().add("f1");
        ProvisioningHistory a1PH = new ProvisioningHistory();
        ProvisioningAction a1PA = new ProvisioningAction(ProvisioningAction.INSTALL_COMMAND, "f1", null);
        a1PH.addAction(a1PA);
        a1AC.setHistory(a1PH);
        a1AC.markActive();
        pgc.getDataProvider().addAgent("a1", a1AC);
        
        pgc.call(); // Do the uninstall work, won't do the install since only 1 feature can be installed
        
        assertEquals("There should be no features installed, since the old feature should have been "
                         + "uninstalled, but the new feature should only be installed in the next scan",
                     0,
                     a1AC.getFeatures().size());
        
        pgc.call(); // At this point the new features should be installed        
        
        List<ProvisioningAction> history = a1PH.getActions();
        assertEquals(3, history.size());
        assertEquals("f1", history.get(0).getFeature());
        assertEquals(ProvisioningAction.INSTALL_COMMAND, history.get(0).getCommand());
        assertEquals("f1", history.get(1).getFeature());
        assertEquals(ProvisioningAction.UNINSTALL_COMMAND, history.get(1).getCommand());
        assertEquals("f2", history.get(2).getFeature());
        assertEquals(ProvisioningAction.INSTALL_COMMAND, history.get(2).getCommand());

        assertEquals(0, pgc.getAgentsAssignedToFeature("f0").size());
        assertEquals(0, pgc.getAgentsAssignedToFeature("f1").size());
        assertEquals(1, pgc.getAgentsAssignedToFeature("f2").size());
    }    
    
    public void testRemoveFeatureFromProfile() throws Exception {
        ProvisioningGridController pgc = new ProvisioningGridController();
        
        addProfileToController(pgc, "prof1", new String[] {"f1", "f3" }, null);
        
        
        AgentController ac = registerAgentWithController(pgc, "a1", "prof1");
        ac.getFeatures().add("f2");
        ac.getFeatures().add("f3");        
        
        pgc.call(); // Do the actual work
        
        
        assertEquals(2, ac.getFeatures().size());
        boolean f1Found = false;
        boolean f3Found = false;
        for (String fid : ac.getFeatures()) {
            if (fid.equals("f1")) {
                f1Found = true;
            } else if (fid.equals("f3")) {
                f3Found = true;
            }
        }
        assertTrue(f1Found);
        assertTrue(f3Found);        
    }

    public void testRemoveAllFeaturesFromProfile() throws Exception {
        ProvisioningGridController pgc = new ProvisioningGridController();
        
        addProfileToController(pgc, "prof1", new String[0], null);
        
        AgentController ac = registerAgentWithController(pgc, "a1", "prof1");
        ac.getFeatures().add("f2");
        ac.getFeatures().add("f3");        

        pgc.call(); // Do the actual work        
        
        assertEquals("Since the profile has no features associated, both features should have been removed",
                     0,
                     ac.getFeatures().size());

        List<ProvisioningAction> history = ac.getHistory().getActions();
        assertEquals(2, history.size());
        List<String> actionFeatures = new ArrayList<String>(2);
        actionFeatures.add("f2");
        actionFeatures.add("f3");
        assertTrue(actionFeatures.contains(history.get(1).getFeature()));
        assertEquals(ProvisioningAction.UNINSTALL_COMMAND, history.get(0).getCommand());
        assertTrue(actionFeatures.contains(history.get(0).getFeature()));
        assertEquals(ProvisioningAction.UNINSTALL_COMMAND, history.get(1).getCommand());
    }
    
    public void testSwitchProfile() throws Exception {
        ProvisioningGridController pgc = new ProvisioningGridController();
        
        addProfileToController(pgc, "prof1", new String[] {"f11", "f12" }, null);
        addProfileToController(pgc, "prof2", new String[] {"f21", "f22", "f23" }, null);
        
        AgentController ac = registerAgentWithController(pgc, "a1", "prof1");
        
        pgc.call(); // Do the actual work
        
        assertEquals(2, ac.getFeatures().size());
        
        assertTrue(ac.getFeatures().contains("f11"));
        assertTrue(ac.getFeatures().contains("f12"));
        assertFalse(ac.getFeatures().contains("f21"));
        assertFalse(ac.getFeatures().contains("f22"));
        assertFalse(ac.getFeatures().contains("f23"));
        
        Map<String, String> ftToCmdMap = new HashMap<String, String>(ac.getHistory().getActions().size());
        for (ProvisioningAction action : ac.getHistory().getActions()) {
            ftToCmdMap.put(action.getFeature(), action.getCommand());
        }
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f11"));
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f12"));
        assertNull(ftToCmdMap.get("f21"));
        assertNull(ftToCmdMap.get("f22"));
        assertNull(ftToCmdMap.get("f23"));
        
        
        // now switch...
        ac.getDetails().setProfile("prof2");
        
        pgc.call(); // Do the actual work
        
        assertEquals(3, ac.getFeatures().size());
        
        assertFalse(ac.getFeatures().contains("f11"));
        assertFalse(ac.getFeatures().contains("f12"));
        assertTrue(ac.getFeatures().contains("f21"));
        assertTrue(ac.getFeatures().contains("f22"));
        assertTrue(ac.getFeatures().contains("f23"));
        
        ftToCmdMap = new HashMap<String, String>(ac.getHistory().getActions().size());
        for (ProvisioningAction action : ac.getHistory().getActions()) {
            ftToCmdMap.put(action.getFeature(), action.getCommand());
        }
        assertEquals(ProvisioningAction.UNINSTALL_COMMAND, ftToCmdMap.get("f11"));
        assertEquals(ProvisioningAction.UNINSTALL_COMMAND, ftToCmdMap.get("f12"));
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f21"));
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f22"));
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f23"));
        
    }

    public void testRemoveProfileFromAgent() throws Exception {
        ProvisioningGridController pgc = new ProvisioningGridController();
        
        addProfileToController(pgc, "prof1", new String[] {"f11", "f12" }, null);
        
        AgentController ac = registerAgentWithController(pgc, "a1", "prof1");
        
        pgc.call(); // Do the actual work
        
        assertEquals(2, ac.getFeatures().size());
        
        assertTrue(ac.getFeatures().contains("f11"));
        assertTrue(ac.getFeatures().contains("f12"));
        
        Map<String, String> ftToCmdMap = new HashMap<String, String>(ac.getHistory().getActions().size());
        for (ProvisioningAction action : ac.getHistory().getActions()) {
            ftToCmdMap.put(action.getFeature(), action.getCommand());
        }
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f11"));
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f12"));
        
        // now switch...
        ac.getDetails().setProfile(null);
        
        pgc.call(); // Do the actual work
        
        assertEquals(0, ac.getFeatures().size());
        
        assertFalse(ac.getFeatures().contains("f11"));
        assertFalse(ac.getFeatures().contains("f12"));
        
        ftToCmdMap = new HashMap<String, String>(ac.getHistory().getActions().size());
        for (ProvisioningAction action : ac.getHistory().getActions()) {
            ftToCmdMap.put(action.getFeature(), action.getCommand());
        }
        assertEquals(ProvisioningAction.UNINSTALL_COMMAND, ftToCmdMap.get("f11"));
        assertEquals(ProvisioningAction.UNINSTALL_COMMAND, ftToCmdMap.get("f12"));
        
    }

    public void testSwitchProfileWithPartiallySameFeatures() throws Exception {
        ProvisioningGridController pgc = new ProvisioningGridController();
        
        addProfileToController(pgc, "prof1", new String[] {"f11", "f12", "f13", "f14", "f15", "f16"}, null);
        addProfileToController(pgc, "prof2", new String[] {"f12", "f22", "f23" }, null);
        
        AgentController ac1 = registerAgentWithController(pgc, "a1", "prof1");
        
        pgc.call(); // Do the actual work
        
        assertEquals(6, ac1.getFeatures().size());
        
        assertTrue(ac1.getFeatures().contains("f11"));
        assertTrue(ac1.getFeatures().contains("f12"));
        assertFalse(ac1.getFeatures().contains("f21"));
        assertFalse(ac1.getFeatures().contains("f22"));
        assertFalse(ac1.getFeatures().contains("f23"));
        
        Map<String, String> ftToCmdMap = new HashMap<String, String>(ac1.getHistory().getActions().size());
        for (ProvisioningAction action : ac1.getHistory().getActions()) {
            ftToCmdMap.put(action.getFeature(), action.getCommand());
        }
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f11"));
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f12"));
        assertNull(ftToCmdMap.get("f21"));
        assertNull(ftToCmdMap.get("f22"));
        assertNull(ftToCmdMap.get("f23"));
        
        // add a new agent and set them both to the new profile...
        AgentController ac2 = registerAgentWithController(pgc, "a2", "prof2");
        ac1.getDetails().setProfile("prof2");
        
        pgc.call(); // Do the actual work
        
        assertEquals(2, ac2.getFeatures().size());
        assertTrue(ac2.getFeatures().contains("f22"));
        assertTrue(ac2.getFeatures().contains("f23"));
        
        assertEquals(1, ac1.getFeatures().size());
        assertFalse(ac1.getFeatures().contains("f11"));
        assertFalse(ac1.getFeatures().contains("f22"));
        assertFalse(ac1.getFeatures().contains("f23"));
        
        assertTrue(ac1.getFeatures().contains("f12"));
        
        ftToCmdMap = new HashMap<String, String>(ac1.getHistory().getActions().size());
        for (ProvisioningAction action : ac1.getHistory().getActions()) {
            ftToCmdMap.put(action.getFeature(), action.getCommand());
        }
        assertEquals(ProvisioningAction.UNINSTALL_COMMAND, ftToCmdMap.get("f11"));
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f12"));
        assertNull(ftToCmdMap.get("f22"));
        assertNull(ftToCmdMap.get("f23"));
        
        ftToCmdMap = new HashMap<String, String>(ac2.getHistory().getActions().size());
        for (ProvisioningAction action : ac2.getHistory().getActions()) {
            ftToCmdMap.put(action.getFeature(), action.getCommand());
        }
        assertNull(ftToCmdMap.get("f12"));
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f22"));
        assertEquals(ProvisioningAction.INSTALL_COMMAND, ftToCmdMap.get("f23"));
        
        // now remove the feature that was in both profiles
        pgc.getProfileDetails("prof2").getFeatures().remove(0);

        pgc.call(); // Do the actual work
        
        assertEquals(0, ac1.getFeatures().size());
        assertFalse(ac1.getFeatures().contains("f12"));
        
        ftToCmdMap = new HashMap<String, String>(ac1.getHistory().getActions().size());
        for (ProvisioningAction action : ac1.getHistory().getActions()) {
            ftToCmdMap.put(action.getFeature(), action.getCommand());
        }
        assertEquals(ProvisioningAction.UNINSTALL_COMMAND, ftToCmdMap.get("f12"));
    }

    private AgentController registerAgentWithController(ProvisioningGridController pgc,
                                                        String agentId,
                                                        String agentProfile) {
        AgentDetails ad = new AgentDetails();
        ad.setId(agentId);
        ad.setProfile(agentProfile);
        ad.setMaximumFeatures(100);
        AgentController ac = new AgentController(pgc, ad);
        ProvisioningHistory ph = new ProvisioningHistory();
        ac.setHistory(ph);
        ac.markActive();
        pgc.getDataProvider().addAgent(agentId, ac);
        return ac;
    }

    private ProfileController addProfileToController(ProvisioningGridController pgc,
                                                     String profileId,
                                                     String[] featureIds,
                                                     Map<String, Map<String, String>> cfgOverrides) {

        
        ProfileDetails pd = new ProfileDetails(profileId);

        for (String fId : featureIds) {
            FeatureDetails fd = new FeatureDetails(fId);
            pgc.getDataProvider().addFeature(fId, new FeatureController(pgc, fd));

            Dependency dep = new Dependency(fId);
            
            if (cfgOverrides != null && cfgOverrides.get(fId) != null) {
                Map<String, String> depOverrides = cfgOverrides.get(fId);
                for (String key : depOverrides.keySet()) {
                    dep.addCfgOverride(new ConfigurationUpdate(key, depOverrides.get(key)));
                }
            }

            pd.getFeatures().add(dep);
        }
        
        ProfileController pc = new ProfileController(pgc, pd);
        pgc.getDataProvider().addProfile(profileId, pc);
        
        return pc;
    }

}
