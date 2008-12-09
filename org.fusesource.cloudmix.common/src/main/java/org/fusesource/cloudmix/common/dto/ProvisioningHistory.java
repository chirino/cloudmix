/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 61256 $
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProvisioningHistory {
    private static final transient Log LOG = LogFactory.getLog(ProvisioningHistory.class);

    @XmlElement(name = "config")
    private List<AgentCfgUpdate> cfgUpdates = new ArrayList<AgentCfgUpdate>();
    @XmlElement(name = "actions")
    private List<ProvisioningAction> actions = new ArrayList<ProvisioningAction>();
    private Date lastModified;
    @XmlTransient
    private String digest;
    @XmlTransient
    private long updateCounter;

    @Override
    public String toString() {
        return "ProvisioningHistory[" + cfgUpdates + " " + actions + "]";
    }

    public void addAction(ProvisioningAction action) {
        actions.add(action);        
        onUpdated();
        LOG.debug("Added action: " + action);
    }

    public boolean removeAction(ProvisioningAction action) {
        boolean answer = actions.remove(action);
        onUpdated();
        return answer;
    }

    public void addCfgUpdate(AgentCfgUpdate anUpdate) {
        cfgUpdates.add(anUpdate);        
        onUpdated();
        LOG.debug("Added configuration update: " + anUpdate);
    }

    public boolean removeCfgUpdate(AgentCfgUpdate anUpdate) {
        boolean answer = cfgUpdates.remove(anUpdate);
        onUpdated();
        return answer;
    }
    
    public boolean removeCfgUpdate(String aPropName) {
        if (aPropName == null || aPropName.trim().length() == 0) {
            return false;
        }
        
        for (Iterator<AgentCfgUpdate> iter = cfgUpdates.iterator(); iter.hasNext();) {
            AgentCfgUpdate anUpdate = iter.next();
            if (aPropName.equals(anUpdate.getProperty())) {
                boolean answer = cfgUpdates.remove(anUpdate);
                onUpdated();
                return answer;
            }
        }
        
        return false;
    }

    public void populate(ProcessList processList) {
        Process process = processList.process();
        for (ProvisioningAction action : actions) {
            if (action.isInstall()) {
                process.feature(action.getFeature(), action.getResource());
            }
        }
    }

    public void populate(Set<String> features) {
        for (ProvisioningAction action : actions) {
            if (action.isInstall()) {
                if (action.getFeature() == null) {
                    throw new IllegalArgumentException("action.getFeature() should not be null");
                }
                features.add(action.getFeature());
            }
        }
    }

    // Fluent API
    //-------------------------------------------------------------------------

    public ProvisioningHistory install(String featureId, String featureUrl) {
        addAction(new ProvisioningAction(featureId, featureUrl));
        return this;
    }

    // Properties
    //-------------------------------------------------------------------------

    public List<ProvisioningAction> getActions() {
        return actions;
    }

    public void setActions(List<ProvisioningAction> actions) {
        onUpdated();
        this.actions = actions;
    }
    
    public List<AgentCfgUpdate> getCfgUpdates() {
        return cfgUpdates;
    }

    public void setCfgUpdates(List<AgentCfgUpdate> someUpdates) {
        onUpdated();
        cfgUpdates = someUpdates;
    }

    public String getDigest() {
        if (digest == null) {
            digest = createDigest();
        }
        return digest;
    }

    public Date getLastModified() {
        if (lastModified == null) {
            onUpdated();
        }
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    protected void onUpdated() {
        lastModified = new Date();
        digest = null;
        ++updateCounter;
    }

    /**
     * Lets calculate a digest using the hash codes of the configuration updates, the actions, the last
     *  modified time and an update counter
     */
    protected String createDigest() {
        long hash = updateCounter;

        for (ConfigurationUpdate update : cfgUpdates) {
            hash *= 3;
            hash += update.hashCode();
        }
        
        for (ProvisioningAction action : actions) {
            hash *= 3;
            hash += action.hashCode();
        }
        
        hash ^= getLastModified().getTime();
        return "" + hash;
    }
}
