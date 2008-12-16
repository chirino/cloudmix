/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.smx4;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.Bundle;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;
import org.fusesource.cloudmix.common.util.FileUtils;
import org.apache.servicemix.gshell.features.FeaturesService;

public class ServiceMixAgent extends InstallerAgent {

    protected static final String VM_PROP_SMX_HOME = "servicemix.home";

    protected static final String AGENT_WORK_DIR = File.separator + "data" + File.separator + "cloudmix";
    protected static final String AGENT_PROPS_PATH_SUFFIX = "agent.properties";
    
    private static final Log LOGGER = LogFactory.getLog(ServiceMixAgent.class);

    private static final String FEATURE_FILE_KEY = ServiceMixAgent.class.getName() + ".featureFile";

    private static final String SMX4_CONTAINER_TYPE = "smx4";

    private static final String[] SMX4_PACKAGE_TYPES = {"osgi", "jbi"};
    
    private FeaturesService featuresService;

    
    public void setFeaturesService(FeaturesService featuresService) {
        this.featuresService = featuresService;
    }
    
    
    @Override
    public String getDetailsPropertyFilePath() {
        if (propertyFilePath == null) {
            propertyFilePath = defaultWorkFile(AGENT_PROPS_PATH_SUFFIX);
        }
        return propertyFilePath;    
    }


    @Override
    protected boolean validateAgent() {   
        return featuresService != null && getWorkDirectory() != null;
    }

    @Override
    protected void installFeature(org.fusesource.cloudmix.agent.Feature feature,
                                  List<ConfigurationUpdate> featureCfgOverrides) {

        boolean success = false;        
        URI reposUri = null;
        File featuresFile = null;
        try {
            
            featuresFile = File.createTempFile("features_", ".xml", getWorkDirectory());
            FileWriter writer = new FileWriter(featuresFile);
            writer.write(feature.getFeatureList().toServiceMix4Doc());
            writer.close();
            LOGGER.info("Wrote features document to " + featuresFile);

            reposUri = featuresFile.toURI();
            LOGGER.info("Adding features repository " + reposUri);
            featuresService.removeRepository(reposUri);
            featuresService.addRepository(reposUri);
            super.installFeature(feature, featureCfgOverrides);
            
            try {
                featuresService.installFeature(feature.getName());
                
                // TODO: check featuresService.listInstalledFeatures()?
            } catch (Exception e) {
                LOGGER.error("Error installing feature " + feature, e);
                LOGGER.debug(e);
            }

            success = true;
            LOGGER.info("installation of feature " + feature.getName() + " successful");
            feature.getAgentProperties().put(FEATURE_FILE_KEY, featuresFile);            
            
        } catch (Exception e) {
            LOGGER.error("Error installing feature " + feature + ", exception " + e);
            LOGGER.debug(e);
        } finally {
            if (!success) {
                LOGGER.warn("installFeature failed");
                try {
                    if (reposUri != null) {
                        featuresService.removeRepository(reposUri);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("error removing repository " + reposUri, t);
                }
                if (featuresFile != null) {
                    featuresFile.delete();
                }
            }
        }        
    }

    @Override
    protected void uninstallFeature(Feature feature) {
        String featureName = feature.getName();
        try {
            featuresService.uninstallFeature(featureName);
            // TODO: check featuresService.listInstalledFeatures()?
            super.uninstallFeature(feature);
            
            File featuresFile = (File) feature.getAgentProperties().get(FEATURE_FILE_KEY);
            if (featuresFile == null) {
                LOGGER.error("Cannot find features file for feature " + featureName);
            } else {
                URI reposUri = featuresFile.toURI();
                LOGGER.info("Removing features repository " + reposUri);
                featuresService.removeRepository(reposUri);
                
                LOGGER.info("Deleting features file " + featuresFile);
                featuresFile.delete();
            }
            feature.getAgentProperties().remove(FEATURE_FILE_KEY);

        } catch (Exception e) {
            LOGGER.error("Error uninstalling feature " + featureName + ", exception " + e);
            LOGGER.debug(e);
        }
    }

    @Override
    protected boolean installBundle(org.fusesource.cloudmix.agent.Feature feature, Bundle bundle) {
        return true;
    }


    @Override
    protected boolean uninstallBundle(Feature feature, Bundle bundle) {
        return false;
    }
    
    @Override
    public AgentDetails updateAgentDetails() {
        AgentDetails rc = super.updateAgentDetails();

        try {
            getClient().updateAgentDetails(getAgentId(), getAgentDetails());
        } catch (URISyntaxException e) {
            LOGGER.info("Problem updating agent information ", e);
            e.printStackTrace();
        }
        return rc;
    }
    
    @Override
    public void init() throws Exception {
        super.init();
      
        File dir = getWorkDirectory();
        if (dir != null) {            
            if (!dir.exists()) {
                FileUtils.createDirectory(dir);
            }
        }        
      
        if (getContainerType() == null) {
            setContainerType(SMX4_CONTAINER_TYPE);
        }
        if (getSupportPackageTypes() == null
            || getSupportPackageTypes().length == 0) {
            setSupportPackageTypes(SMX4_PACKAGE_TYPES);
        }
    }
    
       
    private String defaultWorkFile(String fileName) {
        
        // Default work files based off the ServiceMix data directory.
        
        String path = System.getProperty(VM_PROP_SMX_HOME);
        if (path == null) {
            LOGGER.error("cannot determing ServiceMix home directory");
            return null;
        }
        
        path += AGENT_WORK_DIR;
        if (fileName != null) {
            path += File.separator + fileName;
        }
        
        LOGGER.info("using work file " + path);
        return path;
    }



}
