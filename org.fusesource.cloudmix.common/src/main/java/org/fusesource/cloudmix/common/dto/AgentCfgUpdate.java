/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Used to represent a configuration update sent by the provisioning controller down to the agent.
 * they can be used to change to the agent Id, its assigned profile ID or any property (provided they are
 * editable).
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AgentCfgUpdate extends ConfigurationUpdate {    
    public static final String PROPERTY_AGENT_FORCE_REGISTER = "agent.force.register";
    public static final String PROPERTY_AGENT_NAME = "agent.name";
    public static final String PROPERTY_PROFILE_ID = "agent.profile";
    
    public AgentCfgUpdate() {
        super();
    }

    public AgentCfgUpdate(String property, String value) {
        super(property, value);
    }
}
