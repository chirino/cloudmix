/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.logging;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.fusesource.cloudmix.agent.AgentPoller;
import org.fusesource.cloudmix.agent.Bundle;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.agent.security.DialogPasswordProvider;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;

public class LoggingInstallerAgent extends InstallerAgent {

    private static final String CONFIG_PROP_FILE = "agent.properties.file";
    private static final String CONFIG_WORK_DIR = "agent.work.dir";
    private static final String CONFIG_REPO_URI = "agent.repository.uri";
    private static final String CONFIG_ID = "agent.id";
    private static final String CONFIG_NAME = "agent.name";
    private static final String CONFIG_PROFILE = "agent.profile";
    private static final String CONFIG_AGENT_USER = "agent.user";
    //private static final String CONFIG_AGENT_PASSWORD = "agent.password";
    private static final String CONFIG_AGENT_TYPE = "agent.type";
    private static final String CONFIG_AGENT_LINK = "agent.link";
    private static final String CONFIG_AGENT_PACKAGES = "agent.packages";
    private static final String CONFIG_INSTALL_DELAY = "agent.install.delay";
    private static final String CONFIG_SYS_PROPS = "agent.sys.props";
    
    private static final String DEFAULT_WORK_DIR = "agent.workdir";
    private static final String DEFAULT_CONTAINER_TYPE = "logging"; 
    private static final String DEFAULT_REPO_URI = "http://localhost:9091/controller";
    private static final String DEFAULT_PROFILE = "default";
    private static final String DEFAULT_AGENT_USER = "Agent";
    //private static final String DEFAULT_AGENT_PASSWORD = "agent";
    private static final String DEFAULT_PACKAGES = "war, jbi, osgi, jar";
    private static final String DEFAULT_INSTALL_DELAY = "1";
    private static final String DEFAULT_SYS_PROPS = "prop1, prop2, prop3";
    private static final int MAX_FEATURES = 25;
    private static final long INITIAL_POLLING_DELAY = 1000;
    private static final long POLLING_PERIOD = 1000;
    
    private static final DateFormat DATE_FORMAT = DateFormat.getTimeInstance();

    private RestGridClient gridClient = new RestGridClient();
    private AgentPoller poller = new AgentPoller();
    private LoggingInstallerAgent agent;
    private int installDelay;
    private String[] systemProps;
    private boolean doWait = true;
    

    public LoggingInstallerAgent() {
        
        log("\n");
        log("---------------------------------------------");
        log("Initializing Logging Installer Agent");
        
        try {

            Properties properties = System.getProperties();
            String agentPropertiesFile = getConfig(properties, CONFIG_PROP_FILE, null);
            Properties newProperties = loadProperties(agentPropertiesFile);
            if (newProperties != null) {
                properties = newProperties;
            }

            String id = getConfig(properties, CONFIG_ID, null);
            String agentName = getConfig(properties, CONFIG_NAME, null);
            String agentProfile = getConfig(properties, CONFIG_PROFILE, DEFAULT_PROFILE);

            String workDir = getConfig(properties, CONFIG_WORK_DIR, DEFAULT_WORK_DIR);
            String rootUri = getConfig(properties, CONFIG_REPO_URI, DEFAULT_REPO_URI);
            String agentUser = getConfig(properties, CONFIG_AGENT_USER, DEFAULT_AGENT_USER);
            String agentType = getConfig(properties, CONFIG_AGENT_TYPE, DEFAULT_CONTAINER_TYPE);
            String agentLink = getConfig(properties, CONFIG_AGENT_LINK, null);
            String[] supportPackageTypes = getConfigList(properties, CONFIG_AGENT_PACKAGES, DEFAULT_PACKAGES);
            String installDelayStr = getConfig(properties, CONFIG_INSTALL_DELAY, DEFAULT_INSTALL_DELAY);
            systemProps = getConfigList(properties, CONFIG_SYS_PROPS, DEFAULT_SYS_PROPS);
            
            int custInstallDelay = 0;
            try {
                custInstallDelay = Integer.parseInt(installDelayStr);
            } catch (Exception e) {
                log("Cannot parse agent.install.delay \"" + installDelayStr + "\"");
            }

            
            //PasswordProvider provider = new SimplePasswordProvider();
            //final String agentPassword = getConfig(properties, 
            //                                       CONFIG_AGENT_PASSWORD, DEFAULT_AGENT_PASSWORD);
            //provider.setPassword("agent");
            
            DialogPasswordProvider provider = new DialogPasswordProvider();
            provider.setUsername(agentUser);
            
            gridClient.setRootUri(new URI(rootUri));
            gridClient.setUsername(agentUser);
            gridClient.setPasswordProvider(provider);
            
            agent = this;

            agentId = id;
            agent.setAgentName(agentName);
            agent.setProfile(agentProfile);
            agent.setWorkDirectory(new File(workDir));


            agent.setDetailsPropertyFilePath(agentPropertiesFile); 
            
            agent.setMaxFeatures(MAX_FEATURES);
            agent.setClient(gridClient);
            agent.setContainerType(agentType);            
            agent.setSupportPackageTypes(supportPackageTypes);
            agent.setAgentLink(agentLink);
            agent.setInstallDelay(custInstallDelay);

            log("");
            log("Configuration:");
            log("  Agent ID           ["
                               + (agentId == null ? "unassigned yet" : agentId)
                               + "]");
            log("  Agent Name         ["
                               + (agentName == null ? "unassigned yet" : agentName)
                               + "]");
            log("  agent property file [" + agentPropertiesFile + "]");
            log("  Agent profile      [" + agentProfile + "]");
            log("  Repository URI     [" + rootUri + "]");
            log("  Agent user         [" + agentUser + "]");
            log("  Agent type         [" + agent.getContainerType() + "]");
            log("  Agent link         [" + agentLink + "]");
            log("  Package types:     [" + list2String(supportPackageTypes) + "]");
            log("  System props:      [" + list2String(systemProps) + "]");

            agent.init();
            poller.setInitialPollingDelay(INITIAL_POLLING_DELAY);
            poller.setPollingPeriod(POLLING_PERIOD);
            poller.setAgent(agent);

            poller.start();

            log("");
            log("Logging Agent Installer Ready");
            log("");

        } catch (Exception e) {
            e.printStackTrace();
            unblock();
            try {
                agent.setClient(null);
                poller.destroy();
            } catch (Exception e1) {
                // Complete
            }
        }
    }



    public static void main(String[] args) {
        LoggingInstallerAgent agent = new LoggingInstallerAgent();
        agent.block();
    }

    private synchronized void block() {
        try {
            if (!doWait) {
                wait();
            }
        } catch (InterruptedException e) {
            // Complete
        }
    }
    
    private synchronized void unblock() {
        doWait = false;
        notifyAll();            
    }
    
    public void setInstallDelay(int d) {
        installDelay = d;        
    }

    @Override
    protected String addToClient(AgentDetails details) throws URISyntaxException {

        boolean addedToClient2 = addedToClient;
        String newId = super.addToClient(details);
        if (!addedToClient2) {
            log("agent id: \"" + newId + "\"");
        }
        return newId;
    }
    
    
    @Override
    public void setAgentName(String name) {
        if (!name.equals(agentName)) {
            log("updating agent name: \"" + name + "\" (was \"" + agentName + "\")");
        }
        super.setAgentName(name);
    }
    
    @Override
    public void setProfile(String p) {
        if (!p.equals(profile)) {
            log("updating profile name: \"" + p + "\" (was \"" + profile + "\")");
        }
        super.setProfile(p);
    }
        
    @Override
    protected void installFeature(Feature feature, 
                                  List<ConfigurationUpdate> featureCfgOverrides) throws Exception {

        log("");
        log("installing feature \"" + feature.getName() + "\" {");
        for (String propName : feature.getPropertyNames()) {
            log("  properties \"" + propName + "\" {");
            
            Properties props = feature.getProperties(propName);
            for (Object o : props.keySet()) {
                String n = (String) o;
                log("    " + n + " = " + props.getProperty(n));                
            }            
            log("  }");
        }
        super.installFeature(feature, featureCfgOverrides);
        
        log("}");
        log("");
        log("system properties {"); 
        for (String s : systemProps) {
            String v = System.getProperty(s);
            if (v != null) {
                log("  " + s + " = \"" + v + "\"");
            } else {
                log("  " + s + " is not set");
            }
        }
        log("}");
        
        log("");
        if (installDelay > 0) {
            log("sleeping for " + installDelay + " seconds ...");
            try {
                Thread.sleep(installDelay * 1000);
            } catch (InterruptedException e) {
                // Complete
            }
            log("continuing");
        }
    }

    @Override
    protected void uninstallFeature(Feature feature) throws Exception {
        
        log("");
        log("uninstalling feature \"" + feature.getName() + "\" {");
        super.uninstallFeature(feature);
        log("}");
    }
    
    @Override
    protected boolean installBundle(Feature feature, Bundle bundle) {
        String name = bundle.getName();
        String uri = bundle.getUri();
        
        StringBuilder sb = new StringBuilder()
            .append("  bundle ");
        if (name == null || "".equals(name)) {
            sb.append(uri);
        } else {
            sb.append("\"").append(name).append("\" (").append(uri).append(")");
        }

        log(sb.toString());
        
        return true;
    }

    @Override
    protected boolean uninstallBundle(Feature feature, Bundle bundle) {
        String name = bundle.getName();
        String uri = bundle.getUri();
        
        StringBuilder sb = new StringBuilder()
            .append("  bundle ");
        if (name == null || "".equals(name)) {
            sb.append(uri);
        } else {
            sb.append("\"").append(name).append("\" (").append(uri).append(")");
        }

        log(sb.toString());
        return false;
    }

    @Override
    protected boolean validateAgent() {
        return true;
    }

    private Properties loadProperties(String file) {

        try {
            if (file == null || "".equals(file)) {
                return null;
            }
            Properties properties = new Properties();
            log("Loading properties from file: " + file);
            properties.load(new FileInputStream(file));
            return properties;
        } catch (Exception e) {
            log("Error loading properties file: " + file);
            log("Exception: " + e);
            return null;
        }
    }

    private String getConfig(Properties properties, String name, String defaultValue) {
        
        String value = properties.getProperty(name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    private String[] getConfigList(Properties properties, String name, String defaultValue) {

        String value = properties.getProperty(name);
        if (value == null) {
            value = defaultValue;
        }

        StringTokenizer tokeniser = new StringTokenizer(value, ",");
        int arraySize = tokeniser.countTokens();
        String[] array = new String[arraySize];
        for (int i = 0; i < arraySize; i++) {
            String item = (String) tokeniser.nextElement();
            array[i] = item.trim();
        }
        return array;
    }

    private String list2String(String... list) {
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (String s : list) {
            sb.append(prefix + "\"" + s + "\"");
            prefix = ", ";
        }
        return sb.toString();
    }

    
    private void log(String s) {
        StringBuilder sb = new StringBuilder()
            .append("[")
            .append(DATE_FORMAT.format(new Date()))
            .append("] ")
            .append(s);
        System.out.println(sb.toString());
    }

}
