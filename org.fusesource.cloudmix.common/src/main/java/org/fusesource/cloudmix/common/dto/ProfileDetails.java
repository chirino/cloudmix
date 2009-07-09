/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileDetails extends IdentifiedType {    
    @XmlElement(name = "dependency")
    private List<Dependency> features = new ArrayList<Dependency>();
    
    public ProfileDetails() { }
    
    public ProfileDetails(String id) {
        super(id);
    }

    @Override
    public String toString() {
        return "Profile[id: " + getId() + " features: " + features + "]";
    }

    // Fluent API
    //-------------------------------------------------------------------------

    public ProfileDetails addFeature(String featureId, Map<String, String> cfgOverrideProps) {
        Dependency answer = new Dependency();
        answer.setFeatureId(featureId);
        
        if (cfgOverrideProps != null && cfgOverrideProps.size() > 0) {
            for (String key : cfgOverrideProps.keySet()) {
                answer.addCfgOverride(new ConfigurationUpdate(key, cfgOverrideProps.get(key)));
            }
        }
        
        features.add(answer);
        return this;
    }
    
    // Properties
    //-------------------------------------------------------------------------

    public List<Dependency> getFeatures() {
        return features;
    }

    public void setFeatures(List<Dependency> features) {
        this.features = features;
    }       
}
