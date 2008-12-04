/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent.jbi;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.AgentPoller;
import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.agent.security.DialogPasswordProvider;
import org.fusesource.cloudmix.agent.security.FilePasswordProvider;
import org.fusesource.cloudmix.agent.security.PasswordProvider;

public class AgentComponent implements ComponentLifeCycle, Component {

    private static final Log LOGGER = LogFactory.getLog(AgentComponent.class);

    private static final String CONFIG_PROP_FILE = "agent.properties.file";
    private static final String CONFIG_REPO_URI = "agent.repository.uri";
    private static final String CONFIG_ID = InstallerAgent.PERSISTABLE_PROPERTY_AGENT_ID;
    private static final String CONFIG_NAME = InstallerAgent.PERSISTABLE_PROPERTY_AGENT_NAME;
    private static final String CONFIG_PROFILE = InstallerAgent.PERSISTABLE_PROPERTY_PROFILE_ID;
    private static final String CONFIG_MBEAN_NAME = "agent.deployservice.mbean";
    private static final String CONFIG_AGENT_USER = "agent.user";
    private static final String CONFIG_AGENT_PASSWORD_PROV = "agent.password.provider";
    private static final String CONFIG_AGENT_PASSWORD_FILE = "agent.password.file";

    private static final String CONFIG_AGENT_TYPE = "agent.type";
    private static final String CONFIG_AGENT_LINK = "agent.link";
    private static final String CONFIG_AGENT_PACKAGES = "agent.packages";
    private static final String CONFIG_AGENT_MBEAN_CONTAINER = "agent.mbean.container";

    private static final String DEFAULT_PROP_FILE = "conf/agent.properties";
    private static final String DEFAULT_CONTAINER_TYPE = "smx3"; 
    private static final String DEFAULT_REPO_URI = "http://localhost:9091/controller";
    private static final String DEFAULT_PROFILE = "default";
    private static final String DEFAULT_AGENT_USER = "Agent";
    private static final String DEFAULT_AGENT_PASSWORD = "agent";
    private static final String DEFAULT_AGENT_PASSWORD_PROV = "file";
    private static final String DEFAULT_AGENT_PASSWORD_FILE = "conf/agent.password";
    private static final String FILE_PROVIDER = "file";
    private static final String DIALOG_PROVIDER = "dialog";

    private static final String DEFAULT_MBEAN_NAME = "org.apache.servicemix:"
                                                      + "ContainerName=ServiceMix,"
                                                      + "Type=SystemService,"
                                                      + "Name=DeploymentService";
    private static final String DEFAULT_PACKAGES = "jbi, osgi"; // Adding osgi as a workaround
    private static final String DEFAULT_AGENT_MBEAN_CONTAINER = "ServiceMix";
    
    private static final int MAX_FEATURES = 25;
    private static final long INITIAL_POLLING_DELAY = 5000;
    private static final long POLLING_PERIOD = 1000;

    private RestGridClient gridClient = new RestGridClient();
    private JBIInstallerAgent agent = new JBIInstallerAgent();
    private AgentPoller poller = new AgentPoller();

    private MBeanServer mbeanServer;

    private ObjectName mbeanName;

    public AgentComponent() {
        // Complete
    }
    
    public ObjectName getExtensionMBeanName() {
        // Complete
        return null;
    }

    public void init(ComponentContext ctx) throws JBIException {

        LOGGER.info("initialising agent");
        
        StringBuilder sb =
            new StringBuilder()
                .append("\n")
                .append("CloudMix Agent JBI Service Assembly Deployer\n");

        try {
            Properties properties = System.getProperties();
            String agentPropertiesFile = getConfig(System.getProperties(),
                                                   CONFIG_PROP_FILE,
                                                   DEFAULT_PROP_FILE);
            Properties newProperties = loadProperties(agentPropertiesFile);
            if (newProperties != null) {
                properties = newProperties;
            }
            String agentUser = getConfig(properties, CONFIG_AGENT_USER, DEFAULT_AGENT_USER);
            
            PasswordProvider provider = null;
            String providerType = getConfig(properties, CONFIG_AGENT_PASSWORD_PROV, DEFAULT_AGENT_PASSWORD_PROV);
            if (FILE_PROVIDER.equals(providerType)) {
                FilePasswordProvider fpp = new FilePasswordProvider();
                fpp.setPasswordFile(getConfig(properties, CONFIG_AGENT_PASSWORD_FILE, DEFAULT_AGENT_PASSWORD_FILE));
                provider = fpp;
            } else if (DIALOG_PROVIDER.equals(providerType)) {
                DialogPasswordProvider dpp = new DialogPasswordProvider();
                dpp.setUsername(agentUser);
                provider = dpp;
            } else {
                throw new RuntimeException("Unknown password provider " + providerType);
            }
    
            String agentId = getConfig(properties, CONFIG_ID, null);
            String agentName = getConfig(properties, CONFIG_NAME, null);
            String agentProfile = getConfig(properties, CONFIG_PROFILE, DEFAULT_PROFILE);
            
            String anMbeanName = getConfig(properties, CONFIG_MBEAN_NAME, DEFAULT_MBEAN_NAME);
            String rootUri = getConfig(properties, CONFIG_REPO_URI, DEFAULT_REPO_URI);
            String agentType = getConfig(properties, CONFIG_AGENT_TYPE, DEFAULT_CONTAINER_TYPE);
            String agentLink = getConfig(properties, CONFIG_AGENT_LINK, null);
            String[] supportPackageTypes = getConfigList(properties, CONFIG_AGENT_PACKAGES, DEFAULT_PACKAGES);
            String mbeanContainer = getConfig(properties,
                                              CONFIG_AGENT_MBEAN_CONTAINER,
                                              DEFAULT_AGENT_MBEAN_CONTAINER);
            
            mbeanServer = ctx.getMBeanServer();
            ObjectName oname = validateMbean(mbeanServer, anMbeanName);
            if (oname == null) {
                throw new JBIException("DeploymentService MBean not available using name " + anMbeanName);
            }

            gridClient.setRootUri(new URI(rootUri));
            gridClient.setUsername(agentUser);
            gridClient.setPasswordProvider(provider);

            agent.setAgentId(agentId);
            agent.setAgentName(agentName);
            
            agent.setDetailsPropertyFilePath(agentPropertiesFile); 
            
            agent.setMBeanServer(mbeanServer);
            agent.setMBeanName(oname);
            agent.setMaxFeatures(MAX_FEATURES);
            agent.setProfile(agentProfile);
            agent.setClient(gridClient);
            agent.setContainerType(agentType);            
            agent.setSupportPackageTypes(supportPackageTypes);
            agent.setAgentLink(agentLink);
            agent.init();
            
            sb.append("\nConfiguration:")
                .append("\n  Agent ID:     " + agentId == null ? "unassigned yet" : agentId)
                .append("\n  Agent Name:     " + agentName == null ? "unassigned yet" : agentName)
                .append("\n  Agent profile:     " + agentProfile)
                .append("\n  Repository URI:    " + rootUri)
                .append("\n  Agent user:        " + agentUser)
                .append("\n  Agent type:        " + agent.getContainerType())
                .append("\n  Agent link:        " + agentLink)
                .append("\n  Agent mbean name:  " + anMbeanName)
                .append("\n  Package types:     ");
            for (String packageType : supportPackageTypes) {
                sb.append(packageType).append(" ");
            }
            
            poller.setInitialPollingDelay(INITIAL_POLLING_DELAY);
            poller.setPollingPeriod(POLLING_PERIOD);
            poller.setAgent(agent);

            mbeanServer = ctx.getMBeanServer();
            
            AgentMBean agentMBean = new Agent(agent, gridClient, poller);
            registerMBean(agentMBean, ctx.getMBeanNames().getJmxDomainName(),
                        mbeanContainer);

            sb.append("\n");
            LOGGER.info(sb.toString());

        } catch (JBIException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Failed to initialise agent.  Exception " + e);
            throw new JBIException(e);
        }
    }
    

    public void shutDown() throws JBIException {
        
        LOGGER.info("shutting down agent");
        
        if (mbeanName != null) {
            try {
                mbeanServer.unregisterMBean(mbeanName);
            } catch (Exception e) {
                LOGGER.warn("Exception unregistering agent mbean " + e);
            }
        }
    }

    public void start() throws JBIException {

        LOGGER.info("starting agent");
        
        // TODO: need to support restarting agent.
                  
        try {
            poller.afterPropertiesSet();
        } catch (Exception e) {
            LOGGER.warn("Exception " + e + " starting agent poller");
        }
    }

    public void stop() throws JBIException {

        LOGGER.info("stopping agent");
        
        try {
            LOGGER.info("destroying poller");
            agent.setClient(null);
            poller.destroy();
        } catch (Exception e) {
            LOGGER.warn("Exception destroying poller; " + e);
        }
    }
    
    public ComponentLifeCycle getLifeCycle() {
        return this;
    }
    
    public Document getServiceDescription(ServiceEndpoint se) {
        // Complete
        return null;
    }
    
    public ServiceUnitManager getServiceUnitManager() {
        // Complete
        return null;
    }
    
    public boolean isExchangeWithConsumerOkay(ServiceEndpoint se,
                                              MessageExchange me) {
        // Complete
        return false;
    }
    
    public boolean isExchangeWithProviderOkay(ServiceEndpoint se,
                                              MessageExchange me) {
        // Complete
        return false;
    }
    
    public ServiceEndpoint resolveEndpointReference(DocumentFragment df) {
        // Complete
        return null;
    } 
    
    private Properties loadProperties(String file) {

        try {
            if (file == null || "".equals(file)) {
                return null;
            }
            File f = new File(file);
            if (!f.exists()) {
                LOGGER.warn("properties file " + file + " does not exist");
                return null;
            }
            Properties properties = new Properties();
            LOGGER.info("Loading properties from file " + f);
            properties.load(new FileInputStream(f));
            return properties;
        } catch (Exception e) {
            LOGGER.warn("error loading properties file " + file + ", exception " + e);
            return null;
        }
    }

    private String getConfig(Properties properties, String name, String defaultValue) {

        String value = properties.getProperty(name);
        if (value == null) {
            value = defaultValue;
        }
        LOGGER.debug("  Property " + name + " = " + defaultValue);
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

    private void registerMBean(AgentMBean agentMBean, String domainName, 
                               String containerName) {
        
        try {
            String name = domainName
                          + ":ContainerName=" + containerName 
                          + ",Type=ServiceGrid,Name=Agent";

            mbeanName = new ObjectName(name);
            LOGGER.info("registering agent mbean with name " + mbeanName);
            mbeanServer.registerMBean(agentMBean, mbeanName);
        } catch (Exception e) {
            LOGGER.warn("Exception registering mbean " + e);
        }        
    }

    private ObjectName validateMbean(MBeanServer anMbeanServer, String anMbeanName) {
        try {
            ObjectName oname = new ObjectName(anMbeanName);
            MBeanInfo info = anMbeanServer.getMBeanInfo(oname);
            if (info != null) {
                LOGGER.info("Successfully accesses Deployment Service mbean");
                LOGGER.info("Description: " + info.getDescription());
                return oname;
            }
        } catch (Exception e) {
            LOGGER.debug("Exception getting DeploymentService mbean " + e);
        }
        LOGGER.error("Cannot resolve DeploymentService MBean using name " + anMbeanName);
        return null;
    }

}
