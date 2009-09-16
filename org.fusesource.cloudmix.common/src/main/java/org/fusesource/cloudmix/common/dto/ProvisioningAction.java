/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to represent a command sent by the provisioning controller down to the agent.
 * Normally these are install/uninstall commands.
 *
 * @version $Revision$
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProvisioningAction extends IdentifiedType {    

    public static final String INSTALL_COMMAND = "install";
    public static final String UNINSTALL_COMMAND = "uninstall";
    
    private static final transient Log LOG = LogFactory.getLog(ProvisioningAction.class);
    
    @XmlAttribute
    private String command = INSTALL_COMMAND;
    @XmlAttribute
    private String feature;
    @XmlAttribute
    private String resource;
    
    @XmlElement(name = "configOverrides")
    private List<ConfigurationUpdate> cfgOverrides = new ArrayList<ConfigurationUpdate>();


    public ProvisioningAction() {
    }

    public ProvisioningAction(String feature, String resource) {
        this.feature = feature;
        this.resource = resource;
    }

    public ProvisioningAction(String command, String feature, String resource) {
        this.command = command;
        this.feature = feature;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "ProvisioningAction[" + command + " " + feature + " " + cfgOverrides  + " " + resource + "]";
    }

    // Properties
    //-------------------------------------------------------------------------
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public boolean isInstall() {
        return INSTALL_COMMAND.equals(getCommand());
    }
    
    public List<ConfigurationUpdate> getCfgUpdates() {
        return cfgOverrides;
    }
    
    public void addCfgOverride(ConfigurationUpdate anUpdate) {
        cfgOverrides.add(anUpdate);        
        LOG.debug("Added configuration override: " + anUpdate);
    }

    public boolean removeCfgOverride(ConfigurationUpdate anUpdate) {
        boolean answer = cfgOverrides.remove(anUpdate);
        return answer;
    }
    
    public boolean removeCfgOverride(String aPropName) {
        if (aPropName == null || aPropName.trim().length() == 0) {
            return false;
        }
        
        for (Iterator<ConfigurationUpdate> iter = cfgOverrides.iterator(); iter.hasNext();) {
            ConfigurationUpdate anUpdate = iter.next();
            if (aPropName.equals(anUpdate.getProperty())) {
                boolean answer = cfgOverrides.remove(anUpdate);
                return answer;
            }
        }
        
        return false;
    }

}
