/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * JAXB ObjectFactory for CloudMix DTO 
 */
@XmlRegistry
public class ObjectFactory {

    public AgentDetails createAgentDetails() {
        return new AgentDetails();
    }
    
    public ProvisioningHistory createProvisioningHistory() {
        return new ProvisioningHistory();
    }
    
    public FeatureDetails createFeatureDetails() {
        return new FeatureDetails();
    }
    
    public ProfileDetails createProfileDetails() {
        return new ProfileDetails();
    }

    public ProfileStatus createProfileStatus() {
        return new ProfileStatus();
    }
    
    public FeatureDetailsList createFeatureDetailsList() {
        return new FeatureDetailsList();
    }
    
    public AgentDetailsList createAgentDetailsLis() {
        return new AgentDetailsList();
    }
    
    public ProfileDetailsList createDetailsList() {
        return new ProfileDetailsList();
    }
    
    public StringList createStringList() {
        return new StringList();
    }
}
