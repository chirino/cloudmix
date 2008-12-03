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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Revision$
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AgentDetailsList {
    @XmlElement(name = "agents")
    private List<AgentDetails> agents;

    public AgentDetailsList() {
        this.agents = new ArrayList<AgentDetails>();
    }

    public AgentDetailsList(Collection<AgentDetails> agents) {
        this.agents = new ArrayList<AgentDetails>(agents);
    }

    @Override
    public String toString() {
        return "AgentList" + agents;
    }

    // Properties
    //-------------------------------------------------------------------------

    public List<AgentDetails> getAgents() {
        return agents;
    }

    public void setAgents(List<AgentDetails> agents) {
        this.agents = agents;
    }

    // Fluent API
    //-------------------------------------------------------------------------
    public AgentDetails machine(String hostname) {
        AgentDetails answer = new AgentDetails();
        answer.setHostname(hostname);
        agents.add(answer);
        return answer;
    }
}