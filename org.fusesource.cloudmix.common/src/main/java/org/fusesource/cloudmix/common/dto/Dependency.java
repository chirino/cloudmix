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
 * @version $Revision: 1.1 $
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Dependency {
    
    private static final transient Log LOG = LogFactory.getLog(Dependency.class);
    
    @XmlAttribute
    private String featureId;
    
    private boolean hasChanged;
    
    @XmlElement(name = "configOverrides")
    private List<ConfigurationUpdate> cfgOverrides = new ArrayList<ConfigurationUpdate>();
    
    public Dependency() { }

    public Dependency(String id) {
        featureId = id;
    }
    
    public int getDigest() {
        if (featureId != null) {
            return featureId.hashCode();
        } else {
            return 0;
        }
    }
    
    @Override
    public String toString() {
        return "Dependency[" + getFeatureId() + " " + cfgOverrides + "]";
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String aFeatureId) {
        featureId = aFeatureId;
    }
    
    public void setCfgUpdates(List<ConfigurationUpdate> someUpdates) {
        cfgOverrides = someUpdates;
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
    
    public void setChanged(boolean hasIt) {
        hasChanged = hasIt;
    }

    public boolean hasChanged() {
        return hasChanged;
    }
}
