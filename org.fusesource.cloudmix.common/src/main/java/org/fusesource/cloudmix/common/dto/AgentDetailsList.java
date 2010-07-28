/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

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
@XmlRootElement(name = "agents")
@XmlAccessorType(XmlAccessType.FIELD)
public class AgentDetailsList {
    @XmlElement(name = "agent")
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