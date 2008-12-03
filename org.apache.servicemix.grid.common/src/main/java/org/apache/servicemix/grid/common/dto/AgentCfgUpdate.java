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
package org.apache.servicemix.grid.common.dto;

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
