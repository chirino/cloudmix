package org.fusesource.cloudmix.agent.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
    private static final String CONFIG_ID = InstallerAgent.PERSISTABLE_PROPERTY_AGENT_ID;
    private static final String CONFIG_NAME = InstallerAgent.PERSISTABLE_PROPERTY_AGENT_NAME;
    private static final String CONFIG_PROFILE = InstallerAgent.PERSISTABLE_PROPERTY_PROFILE_ID;
    private static final String CONFIG_AGENT_USER = "agent.user";
    private static final String CONFIG_AGENT_PASSWORD = "agent.password";
    private static final String CONFIG_AGENT_TYPE = "agent.type";
    private static final String CONFIG_AGENT_LINK = "agent.link";
    private static final String CONFIG_AGENT_PACKAGES = "agent.packages";
    private static final String CONFIG_INSTALL_DELAY = "agent.install.delay";
    

    private static final String DEFAULT_WORK_DIR = "agent.workdir";
    private static final String DEFAULT_CONTAINER_TYPE = "logging"; 
    private static final String DEFAULT_REPO_URI = "http://localhost:9091/controller";
    private static final String DEFAULT_PROFILE = "default";
    private static final String DEFAULT_AGENT_USER = "Agent";
    private static final String DEFAULT_AGENT_PASSWORD = "agent";
    private static final String DEFAULT_PACKAGES = "war, jbi, osgi, jar";
    private static final String DEFAULT_INSTALL_DELAY = "1";
    private static final int MAX_FEATURES = 25;
    private static final long INITIAL_POLLING_DELAY = 1000;
    private static final long POLLING_PERIOD = 1000;

    private RestGridClient gridClient = new RestGridClient();
    private AgentPoller poller = new AgentPoller();
    private int installDelay;

    public LoggingInstallerAgent() {
        
        System.out.println("\n");
        System.out.println("---------------------------------------------");
        System.out.println("Initializing Logging Installer Agent");
        
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
            
            int custInstallDelay = 0;
            try {
                custInstallDelay = Integer.parseInt(installDelayStr);
            } catch (Exception e) {
                System.out.println("Cannot parse agent.install.delay \"" + installDelayStr + "\"");
            }

            
            //PasswordProvider provider = new SimplePasswordProvider();
            //final String agentPassword = getConfig(properties, CONFIG_AGENT_PASSWORD, DEFAULT_AGENT_PASSWORD);
            //provider.setPassword("agent");
            
            DialogPasswordProvider provider = new DialogPasswordProvider();
            provider.setUsername(agentUser);
            
            gridClient.setRootUri(new URI(rootUri));
            gridClient.setUsername(agentUser);
            gridClient.setPasswordProvider(provider);
            
            LoggingInstallerAgent agent = this;

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

            System.out.println("\nConfiguration:\n");
            System.out.println("  Agent ID           ["
                               + (agentId == null ? "unassigned yet" : agentId)
                               + "]");
            System.out.println("  Agent Name         ["
                               + (agentName == null ? "unassigned yet" : agentName)
                               + "]");
            System.out.println("  agent property file [" + agentPropertiesFile + "]");
            System.out.println("  Agent profile      [" + agentProfile + "]");
            System.out.println("  Repository URI     [" + rootUri + "]");
            System.out.println("  Agent user         [" + agentUser + "]");
            System.out.println("  Agent type         [" + agent.getContainerType() + "]");
            System.out.println("  Agent link         [" + agentLink + "]");
            System.out.print("  Package types      [");
            String prefix = "";
            for (String packageType : supportPackageTypes) {
                System.out.print(prefix + "\"" + packageType + "\"");
                prefix = ", ";
            }
            System.out.print("]");
            System.out.println();

            agent.init();
            poller.setInitialPollingDelay(INITIAL_POLLING_DELAY);
            poller.setPollingPeriod(POLLING_PERIOD);
            poller.setAgent(agent);

            poller.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\nLogging Agent Installer Ready");
        System.out.println("---------------------------------------------");
        System.out.println("\n");
    }


	public static void main(String[] args) {
        LoggingInstallerAgent agent = new LoggingInstallerAgent();
        agent.block();
    }
    
    private synchronized void block() {
        try {
            wait();
        } catch (InterruptedException e) {
            // Complete
        }
    }

    public void setInstallDelay(int d) {
        installDelay = d;        
    }

    @Override
    protected String addToClient(AgentDetails details) throws URISyntaxException {

        boolean addedToClient2 = addedToClient;
        String newId = super.addToClient(details);
        if (!addedToClient2) {
            System.out.println("registered agent to depot : " + newId);
        }
        return newId;
    }
    
    @Override
    public Object call() throws Exception {
        //System.out.println("Calling Depot...");
        return super.call();
    }
    
    @Override
    public AgentDetails getAgentDetails() {
        //System.out.println("Going to fetch agent details : " + agentDetails);
        AgentDetails ads = super.getAgentDetails();
        //System.out.println("fetched agent details : " + agentDetails);
        return ads;

    }
    
    @Override
    public void setAgentName(String name) {
        System.out.println("updating Name : " + agentName + "-->" + name);
        super.setAgentName(name);
    }
    
    @Override
    public void setProfile(String p) {
        System.out.println("updating Profile : " + profile + "-->" + p);
        super.setProfile(p);
    }
    
    @Override
    public boolean loadPersistedAgentDetails() {
        boolean didLoad = super.loadPersistedAgentDetails();
        //System.out.println("loading persisted agent details : " + didLoad);
        return didLoad;
    }
    
    @Override
    public boolean persistAgentDetails() {
        System.out.println("persisting agent details to  : " + getDetailsPropertyFilePath());
        boolean didPersist = super.persistAgentDetails();
        System.out.println("persisted? : " + didPersist);
        return didPersist;
    }
    
    @Override
    protected void installFeature(Feature feature, List<ConfigurationUpdate> featureCfgOverrides) {
        logAgentDetails();
        System.out.println("installing feature " + feature.getName());
        for (String propName : feature.getPropertyNames()) {
            System.out.println("      properties " + propName + " {");
            
            Properties props = feature.getProperties(propName);
            for (Object o : props.keySet()) {
                String n = (String) o;
                System.out.println("        " + n + " = " + props.getProperty(n));                
            }            
            System.out.println("      }");
        }
        super.installFeature(feature, featureCfgOverrides);
        
        if (installDelay > 0) {
            System.out.println("Sleeping for " + installDelay + " seconds ...");
            try {
                Thread.sleep(installDelay * 1000);
            } catch (InterruptedException e) {
                // Complete
            }
            System.out.println("Continuing");
        }

    }

    @Override
    protected void uninstallFeature(Feature feature) {
        logAgentDetails();
        System.out.println("  uninstalling feature " + feature.getName());
        super.uninstallFeature(feature);
    }
    
    @Override
    protected boolean installBundle(Feature feature, Bundle bundle) {
        String name = bundle.getName();
        String uri = bundle.getUri();
        
        StringBuilder sb = new StringBuilder()
            .append("      bundle ");
        if (name == null || "".equals(name)) {
            sb.append(uri);
        } else {
            sb.append(name).append(" (").append(uri).append(")");
        }

        System.out.println(sb.toString());
        
        return true;
    }

    @Override
    protected boolean uninstallBundle(Feature feature, Bundle bundle) {
        String name = bundle.getName();
        String uri = bundle.getUri();
        
        StringBuilder sb = new StringBuilder()
            .append("      bundle ");
        if (name == null || "".equals(name)) {
            sb.append(uri);
        } else {
            sb.append(name).append(" (").append(uri).append(")");
        }

        System.out.println(sb.toString());
        return false;
    }

    @Override
    protected boolean validateAgent() {
        return true;
    }

    private void logAgentDetails() {
        AgentDetails details = getAgentDetails();
        System.out.println("\n* agent - id: " + details.getId()
                + ", name " + details.getName()
                + ", profile: " + details.getProfile());
        
    }

    private Properties loadProperties(String file) {

        try {
            if (file == null || "".equals(file)) {
                return null;
            }
            Properties properties = new Properties();
            System.out.println("\nLoading properties from file: " + file);
            properties.load(new FileInputStream(file));
            return properties;
        } catch (Exception e) {
            System.out.println("\nError loading properties file: " + file);
            System.out.println("\nException: " + e);
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

}
