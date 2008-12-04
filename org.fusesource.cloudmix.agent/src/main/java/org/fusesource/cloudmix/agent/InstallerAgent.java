/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.jersey.api.NotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.AgentCfgUpdate;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;
import org.fusesource.cloudmix.common.dto.ProcessList;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.common.dto.ProvisioningHistory;
import org.fusesource.cloudmix.common.util.FileUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * Polls for features that should be installed/uninstalled and executes those installations.
 *
 * @version $Revision: 1.1 $
 */
public class InstallerAgent implements Callable<Object>, InitializingBean  {


    public static final String PERSISTABLE_PROPERTY_AGENT_ID = "agent.id";
    public static final String PERSISTABLE_PROPERTY_AGENT_NAME = "agent.name";
    public static final String PERSISTABLE_PROPERTY_PROFILE_ID = "agent.profile";

    public static final String PROP_ACCESS_LOCK = "agent.profile";

    private static final transient Log LOGGER = LogFactory.getLog(InstallerAgent.class);
    private static final String AGENT_STATE_FILE = "agent.state";
    
    // Persistent properties.
    protected Map<String, Object> agentProperties = new HashMap<String, Object>();

    
    protected String propertyFilePath;
 
    protected String agentId;
    protected String agentName;
    protected String profile = "default";
    protected String agentType;
    protected String[] supportPackageTypes = {};
    protected String agentLink;    


    protected AgentDetails agentDetails;

    protected boolean addedToClient;
     
    private File workDirectory;
    
    private GridClient client;
    private String hostName;
    private int maxFeatures = 1;
    
    private ProvisioningHistory provisioningHistory;
    private Date lastAppliedHistory;
    private int lastActionsCount;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private BlockingQueue<ProvisioningHistory> historyQueue = new LinkedBlockingQueue<ProvisioningHistory>();

    
    public InstallerAgent() {
        
        lastActionsCount = 0;
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    while (true) {
                        // TODO: could ignore all but the last entry in the queue.
                        ProvisioningHistory history = historyQueue.take();
                        onProvisioningHistoryChanged(history);
                    } 
                } catch (InterruptedException e) {
                    // TODO: do something?
                }                
            }
        };
        executor.execute(r);
    }
    
    @Override
    public String toString() {
        return "InstallerAgent[id: " + agentId + " hostName: " + hostName + " profile: " + profile + "]";
    }

    public Object call() throws Exception {
        String theAgentId = getAgentId();
        
        addToClient(getAgentDetails());
        
        if (theAgentId != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Polling agent: " + theAgentId);
            }
            try {
                ProvisioningHistory value = getClient().pollAgentHistory(theAgentId);
                if (value != null) {
                    asyncOnProvisioningHistoryChanged(value);
                    // This may take some time so its done in a thread.
                }
            } catch (NotFoundException e) {
                forceAgentReregistration();
            }
        }
        return null;
    }

    protected void asyncOnProvisioningHistoryChanged(ProvisioningHistory value) {
        try {
            historyQueue.put(value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }    

    
    public void setWorkDirectory(File d) {
        workDirectory = d;
    }
    
    public File getWorkDirectory() {
        return workDirectory;
    }


    // Properties
    //-------------------------------------------------------------------------
    public AgentDetails getAgentDetails() {
        if (agentDetails == null) {
            agentDetails = new AgentDetails();
            loadPersistedAgentDetails();
            populateInitialAgentDetails(agentDetails);
        }
        return agentDetails;
    }

    public void setAgentDetails(AgentDetails agentDetails) {
        this.agentDetails = agentDetails;
    }
    
    public AgentDetails updateAgentDetails() {
        
        agentDetails = null;
        agentDetails = getAgentDetails();

        try {
            getClient().updateAgentDetails(getAgentId(), getAgentDetails());
        } catch (URISyntaxException e) {
            LOGGER.info("Problem updating agent information ", e);
            e.printStackTrace();
        }
        return agentDetails;
    }

    public GridClient getClient() {
        if (client == null) {
            client = new RestGridClient();
        }
        return client;
    }

    public void setClient(GridClient gridControlerClient) {
        this.client = gridControlerClient;
    }
    
    public int getMaxFeatures() {
        return maxFeatures;
    }
    
    public void setMaxFeatures(int max) {
        maxFeatures = max;
    }
    
    public String getProfile() {
        return profile;
    }
    
    public void setProfile(String p) {
        profile = p;
        if (agentDetails != null) {
            agentDetails.setProfile(p);
        }
    }

    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String aName) {
        agentName = aName;
        if (agentDetails != null) {
            agentDetails.setName(aName);
        }
    }
    
    public ProvisioningHistory getProvisioningHistory() {
        return provisioningHistory;
    }

    public String getAgentId() throws URISyntaxException {
        if (agentId == null) {
            agentId = addToClient(getAgentDetails());
        }
        return agentId;
    }

    
    protected synchronized String addToClient(AgentDetails details) throws URISyntaxException {
        if (addedToClient) {
            return details.getId();
        }
        
        if (provisioningHistory != null) {
            ProcessList processList = new ProcessList();
            provisioningHistory.populate(processList);
            details.setProcesses(processList);
        }
        
        String generatedId = getClient().addAgentDetails(details);
        agentId = generatedId;
        details.setId(generatedId);
        
        persistAgentDetails();
        
        addedToClient = true;
        return generatedId;
    }

    protected void forceAgentReregistration() {
        agentId = null;
        addedToClient = false;
    }

    public String getHostName() {
        if (hostName == null) {
            hostName = createHostName();
        }
        return hostName;
    }

    public void setHostName(String hn) {
        hostName = hn;
    }
    
    /**
     * sets the path to the property file storing the properties of the agent modifiable remotely
     * @param path to the Java property file
     */
    public void setDetailsPropertyFilePath(String path) {
        propertyFilePath = path;
    }
    
    /**
     * gets the path to the property file storing the properties of the agent modifiable remotely
     */
    public String getDetailsPropertyFilePath() {
        return propertyFilePath;
    }
    
    // Implementation methods
    //-------------------------------------------------------------------------
    protected void populateInitialAgentDetails(AgentDetails details) {
        details.setHostname(getHostName());
        details.setPid(PidUtils.getPid());
        details.setMaximumFeatures(getMaxFeatures());
        details.setProfile(getProfile());
        details.setName(getAgentName());
        details.setOs(System.getProperty("os.name"));
        
        details.setAgentLink(null);
        details.setContainerType(null);
        String [] supportPackageTypes = {};
        details.setSupportPackageTypes(supportPackageTypes);
        
        Map<String, String> m = new HashMap<String, String>(System.getProperties().size());
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            m.put(entry.getKey().toString(), entry.getValue().toString());
        }
        details.setSystemProperties(m);
        
        
        details.setContainerType(getContainerType());
        details.setSupportPackageTypes(getSupportPackageTypes());
        details.setAgentLink(getAgentLink());
        
        String[] features = getFeatures();
        details.setCurrentFeatures(features);

    }

    public void setAgentLink(String agentLink) {
        this.agentLink = agentLink;
    }

    public String getAgentLink() {
        return agentLink;
    }


    public void setContainerType(String anAgentType) {
        agentType = anAgentType;
    }

    public String getContainerType() {
        return agentType;
    }
    
    public void setSupportPackageTypes(String[] supportPackageTypes) {
        this.supportPackageTypes = supportPackageTypes;
    }

    public String[] getSupportPackageTypes() {
        return supportPackageTypes;
    }

    protected String[] getFeatures() {

        Set<String> features = new HashSet<String>();
        for (String k : agentProperties.keySet()) {
            Object o = agentProperties.get(k);
            if (o instanceof Feature) {
                features.add(((Feature) o).getName());
            }
        }
        return features.toArray(new String[features.size()]);
    }


    /**
     * persist the agents details locally so they can be retrieved later after a shutdown
     * @param details the details to persist
     * @return true if the details were persisted successfully, false otherwise
     */
    public boolean persistAgentDetails() {
        
        if (getDetailsPropertyFilePath() == null) {
            return false;
        }
        
        synchronized (PROP_ACCESS_LOCK) {
            Properties props = loadProperties(getDetailsPropertyFilePath());
            if (props == null) {
                props = new Properties();
            }

            if (agentName != null) {
                props.setProperty(PERSISTABLE_PROPERTY_AGENT_NAME, agentName);
            }
            
            if (profile != null) {
                props.setProperty(PERSISTABLE_PROPERTY_PROFILE_ID, profile);
            }
            
            if (agentId != null && agentId.trim().length() > 0) {
                props.setProperty(PERSISTABLE_PROPERTY_AGENT_ID, agentId);
            }
            
            return persistProperties(props, getDetailsPropertyFilePath());
        }
    }

    /**
     * load the persisted agents details from local storage
     * @param details the details object to populate with the retrieved data
     * @return true if the details were loaded successfully, false otherwise
     */
    public boolean loadPersistedAgentDetails() {
        if (agentDetails == null) {
            agentDetails = new AgentDetails();
        }
        
        Properties props = loadProperties(getDetailsPropertyFilePath());
        if (props == null) {
            return false;
        }

        synchronized (PROP_ACCESS_LOCK) {    
            String prop = props.getProperty(PERSISTABLE_PROPERTY_AGENT_ID);
            agentId = prop != null ? prop : agentId;
            agentDetails.setId(prop != null ? prop : agentDetails.getId());
    
            prop = readProp(props, PERSISTABLE_PROPERTY_AGENT_NAME);
            setAgentName(prop != null ? prop : agentName);
    
            prop = readProp(props, PERSISTABLE_PROPERTY_PROFILE_ID);
            setProfile(prop != null ? prop : profile);
    
            return true;
        }
    }

    private String readProp(Properties props, String propName) {
        String value = props.getProperty(propName);
        return (value == null || value.trim().length() == 0) ? null : value; 
    }

    protected void onProvisioningHistoryChanged(ProvisioningHistory aProvisioningHistory) {
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("timestamp of previous provisioning history: " + lastAppliedHistory);
            LOGGER.debug("timestamp of current provisioning history: "
                         + (aProvisioningHistory != null ? aProvisioningHistory.getLastModified() : "???"));
        }
        
        if (aProvisioningHistory != null
            && (lastAppliedHistory == null
                || (lastAppliedHistory.getTime() != aProvisioningHistory.getLastModified().getTime()))) {
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("provisioning instructions changed since last poll: " + aProvisioningHistory);
            }
            
            if (!validateAgent()) {
                return;
            }
    
            provisioningHistory = aProvisioningHistory;
            
            List<AgentCfgUpdate> cfgUpdates = aProvisioningHistory.getCfgUpdates();
            if (cfgUpdates != null && cfgUpdates.size() > 0) {
                
                boolean cfgUpdated = false;
                for (AgentCfgUpdate update : cfgUpdates) {
                    if (AgentCfgUpdate.PROPERTY_AGENT_NAME.equals(update.getProperty())
                            && changed(getAgentDetails().getName(), update.getValue())) {
                        setAgentName(update.getValue());
                        getAgentDetails().setName(update.getValue());
                        cfgUpdated = true;
                        
                    } else if (AgentCfgUpdate.PROPERTY_PROFILE_ID.equals(update.getProperty())
                            && changed(getAgentDetails().getProfile(), update.getValue())) {
                        setProfile(update.getValue());
                        getAgentDetails().setProfile(update.getValue());
                        cfgUpdated = true;
                    
                    } else if (AgentCfgUpdate.PROPERTY_AGENT_FORCE_REGISTER.equals(update.getProperty())
                            && "true".equals(update.getValue())) {
                        forceAgentReregistration();
                        cfgUpdated = true;
                    }
                    
                }
                
                if (cfgUpdated) {
                    persistAgentDetails();
                }
            }
        
        
            if (lastActionsCount != aProvisioningHistory.getActions().size()) {
                
                LOGGER.debug("provisioning actions changed since last poll (was " + lastActionsCount
                             + " and now " + aProvisioningHistory.getActions().size());
                try {
                    Map<String, ProvisioningAction> installActions =
                        new HashMap<String, ProvisioningAction>();
                    Map<String, ProvisioningAction> uninstallActions =
                        new HashMap<String, ProvisioningAction>();
                    getEffectiveActions(installActions, uninstallActions);
                    
                    LOGGER.debug("onProvisioningHistoryChanged - uninstall " + uninstallActions);
                    for (ProvisioningAction action : uninstallActions.values()) {
                        String featureName = action.getFeature();
                        Object o = agentProperties.get(featureName);
                        if (o instanceof Feature) {
                            uninstallFeature((Feature) o);
                        } else {
                            LOGGER.warn("Cannot find installed feature " + featureName + " to uninstall");
                        }
                    }
                    
                    LOGGER.debug("onProvisioningHistoryChanged - install: " + installActions);
                    for (ProvisioningAction action : installActions.values()) {
                        String credentials = null;
                        if (getClient() instanceof RestGridClient) {
                            credentials = ((RestGridClient) getClient()).getCredentials();
                        }
                        FeatureList features = new FeatureList(action.getResource(), credentials);
                        Feature feature = features.getFeature(action.getFeature());
                        if (feature != null) {
                            // Uninstall old version of feature if it still exists.
                            Object o = agentProperties.get(feature.getName());
                            if (o instanceof Feature) {
                                uninstallFeature((Feature) o);    
                            }
                            
                            installFeature(feature, action.getCfgUpdates());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error executing provisioning instructions", e);
                } finally {
                    persistState();
                    updateAgentDetails();
                }
                
                lastActionsCount = aProvisioningHistory.getActions().size();
            }
            lastAppliedHistory = aProvisioningHistory.getLastModified();
        
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No new instructions... ");
            }
        }
        
    }

    protected void installFeature(Feature feature, List<ConfigurationUpdate> featureCfgOverrides) {

        LOGGER.info("Installing feature " + feature.getName());
        installProperties(feature, featureCfgOverrides);
        Map<String, Object> featureProperties = feature.getAgentProperties();
        for (Bundle bundle : feature.getBundles()) {
            Map<String, Object> bundleProperties = bundle.getAgentProperties();
            if (installBundle(feature, bundle)) {
                LOGGER.info("Successfully added bundle " + bundle);
                bundleProperties.put("timestamp", new Date());
            }
        }
        featureProperties.put("timestamp", new Date());
        agentProperties.put(feature.getName(), feature);
    }


    protected void uninstallFeature(Feature feature) {
        LOGGER.info("Uninstalling feature " + feature);
        
        for (Bundle bundle : feature.getBundles()) {
            if (uninstallBundle(feature, bundle)) {
                LOGGER.info("Successfully removed bundle " + bundle);
            }
        }
        agentProperties.remove(feature.getName());
    }

    protected void installProperties(Feature feature, List<ConfigurationUpdate> featureCfgOverrides) {
        Properties applicationProperties = feature.getProperties("applicationProperties");
        Properties systemProperties = System.getProperties();
        boolean changed = false;
        
        // time to apply the application's configuration overrides
        if (featureCfgOverrides != null && featureCfgOverrides.size() > 0) {
            if (applicationProperties == null) {
                applicationProperties = new Properties();
            }
            for (ConfigurationUpdate cfgUpdate : featureCfgOverrides) {
                applicationProperties.put(cfgUpdate.getProperty(), cfgUpdate.getValue());
            }
        }
        
        if (applicationProperties != null) {
            systemProperties.putAll(applicationProperties);
            changed = true;
            LOGGER.info(" ===>> adding app props to system");
        }
        
        if (changed) {
            System.setProperties(systemProperties);
            LOGGER.info(" ===>> applying props updates");
        }
        
    }

    public List<Bundle> getFeatureBundles(String featureName) {

        Object o = agentProperties.get(featureName);
        if (o instanceof Feature) {
            return ((Feature) o).getBundles();
        } else {
            return new ArrayList<Bundle>();
        }
    }

    protected String createHostName() {
        try {
            return Inet4Address.getLocalHost().getCanonicalHostName();
        
        } catch (UnknownHostException e) {
            LOGGER.warn("Could not find out the host name: " + e, e);
            return "localhost";
        }
    }

    /**
     * Calculates the effective actions to be taken by the agent. This is based on the
     * history. The history could instruct to install a feature and then uninstall it
     * later, in that case the net effect is 0. This method walks the history and computes
     * the effective actions for install and uninstall.
     * @param installActions This map will be filled with the install actions to perform
     * @param uninstallActions This map will be fille with the uninstall actions to perform
     */
    public void getEffectiveActions(Map<String, ProvisioningAction> installActions, 
                                    Map<String, ProvisioningAction> uninstallActions) {
        
        ProvisioningHistory history = getProvisioningHistory();
        if (history == null) {
            return;
        }
        
        List<ProvisioningAction> actions = history.getActions();
        if (actions == null) {
            return;
        }
        
        for (ProvisioningAction action : actions) {
            if (ProvisioningAction.INSTALL_COMMAND.equals(action.getCommand())) {
                LOGGER.debug("install action :" + action.getFeature());
                installActions.put(action.getFeature(), action);
                uninstallActions.remove(action.getFeature());
            } else if (ProvisioningAction.UNINSTALL_COMMAND.equals(action.getCommand())) {
                LOGGER.debug("uninstall action :" + action.getFeature());
                uninstallActions.put(action.getFeature(), action);
                installActions.remove(action.getFeature());
            }
        }
    }  
    
    // convenience
    private synchronized Properties loadProperties(String filePath) {

        try {
            if (filePath == null || "".equals(filePath) || !(new File(filePath)).exists()) {
                return null;
            }
            Properties properties = new Properties();
            properties.load(new FileInputStream(filePath));
            return properties;
        } catch (Exception e) {
            LOGGER.warn("error loading properties file " + filePath + ", exception " + e);
            return null;
        }
    }

    private synchronized boolean persistProperties(Properties props, String filePath) {

        try {
            if (props == null || filePath == null || "".equals(filePath)) {
                return false;
            }
            File propFile = new File(filePath);
            if (!propFile.exists()) {
                if (propFile.getParentFile() != null && !propFile.getParentFile().exists()) {
                    FileUtils.createDirectory(propFile.getParentFile());
                }
                propFile.createNewFile();
            }
                        
            props.store(new FileOutputStream(propFile), "agent details as of "  + new Date());
            return true;
        } catch (Exception e) {
            LOGGER.warn("error storing properties file " + filePath, e);
            return false;
        }
    }

    protected boolean installBundle(Feature feature, Bundle bundle) {
        return true;
    }

    protected boolean uninstallBundle(Feature feature, Bundle bundle) {
        return true;
    }
    
    protected boolean validateAgent() {
        return true;
    }
    

    protected boolean changed(String oldValue, String newValue) {
        return !((oldValue == null && newValue == null) || (oldValue != null && oldValue.equals(newValue)));
    }

    public void afterPropertiesSet() throws Exception {
        init();
    }
    
    public void init() throws Exception {

        File dir = getWorkDirectory();
        if (dir == null) {
            LOGGER.warn("No work directory specified.  Not persisting agent state.");
            return;
        } else {
            if (FileUtils.createDirectory(dir) == null) {
                LOGGER.error("Cannot create work directory " + dir);
                throw new RuntimeException("Cannot create work directory " + dir);
            }
            loadState();
            agentProperties.put("started", new Date());
        }
    }

    protected void persistState() {
        try {
            File dir = getWorkDirectory();
            if (dir == null || !dir.exists()) {
                // Persistence is not enabled.
                return;
            }

            File stateFile = new File(dir, AGENT_STATE_FILE);
            LOGGER.info("Saving agent state to " + stateFile);
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(stateFile));
            try {
                os.writeObject(agentProperties);
            } finally {
                os.close();
            }
        } catch (Throwable t) {
            LOGGER.error("Error persisting agent state " + t);
            LOGGER.debug(t);
        }        
    }
    
    @SuppressWarnings("unchecked")
    protected void loadState() throws IOException {

        File dir = getWorkDirectory();
        if (dir == null || !dir.exists()) {
            // Persistence is not enabled.
            return;
        }
       
        File stateFile = new File(dir, AGENT_STATE_FILE);
        if (!stateFile.exists()) {
            LOGGER.info("agent state file " + stateFile + " does not exist");
            agentProperties.put("created", new Date());
            return;
        }
        
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(stateFile));
        Object o;
        try {
            o = is.readObject();
            agentProperties = (Map<String, Object>) o;
            
            showAgentProperties();
        } catch (ClassNotFoundException e) {
            LOGGER.error("error loading state - exception " + e);
            throw new IOException("ClassNotFoundException during readObject()");
        } finally {
            is.close();
        }
    }
    
    private void showAgentProperties() {
        if (LOGGER.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nAgent properties...\n");
            addProperties(sb, agentProperties, "  ");

            LOGGER.info(sb.toString());
        }
    }
    
    private void addProperties(StringBuilder sb, Map<String, Object> properties, String prefix) {
        
        for (String k : properties.keySet()) {
            sb.append(prefix).append(k).append(" = ");
            Object o = properties.get(k);
            if (o instanceof Feature) {
                Feature f = (Feature) o;
                sb.append("feature\n");
                sb.append(prefix).append("  Feature properties...\n");
                addProperties(sb, f.getAgentProperties(), prefix + "  ");
                for (Bundle b : f.getBundles()) {
                    sb.append(prefix).append("  bundle = ").append(b).append("\n");
                    sb.append(prefix).append("    Bundle properties...\n");
                    addProperties(sb, b.getAgentProperties(), prefix + "    ");
                }
                sb.append("\n");
            } else {
                sb.append(properties.get(k)).append("\n");
            }
        }        
    }


}
