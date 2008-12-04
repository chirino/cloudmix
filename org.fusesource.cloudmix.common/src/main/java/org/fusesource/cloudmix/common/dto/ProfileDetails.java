/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    @XmlElement(name = "features")
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
