/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.ws.wsaddressing.W3CEndpointReference;


/**
 * @version $Revision: 1.1 $
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AgentDetails extends IdentifiedType {
    @XmlAttribute
    private String name;
    @XmlAttribute(required = true)
    private String profile = "default";
    @XmlAttribute
    private String hostname = "localhost";
    @XmlAttribute
    private int pid = -1;
    @XmlAttribute
    private String containerType;
    @XmlAttribute
    private String agentLink;
    @XmlAttribute
    private String [] supportPackageTypes;
    @XmlAttribute
    private String os;
    @XmlAttribute
    private int maximumFeatures = 1;
    @XmlElement(required = false)
    private String [] currentFeatures;
    @XmlElement(required = false)
    private ProcessList processes;
//  @XmlElement(required = false) davidb TODO why do I have to comment this out to make it work?
    private Map<String, String> systemProperties;
    @XmlElement(required = false)
    private ArrayList<String> endpointNames = new ArrayList<String>();
    @XmlElement(required = false)
    private ArrayList<W3CEndpointReference> endpointReferences = new ArrayList<W3CEndpointReference>();


    @Override
    public String toString() {
        return "AgentDetails[id: " + getId()
                         + " hostname: " + hostname
                         + " pid: " + pid
                         + " profile: " + profile
                         + "]";
    }

    // Fluent API
    //-------------------------------------------------------------------------

    /**
     * Creates a new process that can then be configured using a fluent API
     */
    public Process process() {
        if (processes == null) {
            processes = new ProcessList();
        }
        return processes.process();
    }


    // Properties
    //-------------------------------------------------------------------------

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public String [] getCurrentFeatures() {
        return currentFeatures;        
    }
    
    public void setCurrentFeatures(String [] features) {
        this.currentFeatures = features;
    }

    public int getMaximumFeatures() {
        return maximumFeatures;
    }

    public void setMaximumFeatures(int maximumFeatures) {
        this.maximumFeatures = maximumFeatures;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public String getAgentLink() {
        return agentLink;
    }

    public void setAgentLink(String agentLink) {
        this.agentLink = agentLink;
    }

    public String [] getSupportPackageTypes() {
        return supportPackageTypes;        
    }
    
    public void setSupportPackageTypes(String [] supportPackageTypes) {
        this.supportPackageTypes = supportPackageTypes;
    }
    
    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }
    
    public int getPid() {
        return pid;
    }
    
    public void setPid(int pid) {
        this.pid = pid;
    }    

    public ProcessList getProcesses() {
        return processes;
    }

    public void setProcesses(ProcessList processes) {
        this.processes = processes;
    }
    
    public String getProfile() {
        return profile;
    }
    
    public void setProfile(String p) {
        profile = p;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String aLabel) {
        name = aLabel;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    public void addEndpoint(String key, W3CEndpointReference value) {
    	int pos = endpointNames.indexOf(key);
    	if (pos < 0) {
    	    endpointNames.add(key);
    	    endpointReferences.add(value);
    	} else {
    		endpointReferences.remove(pos);
    	    endpointReferences.add(pos, value);
    	}
    }

    public boolean removeEndpoint(String key) {
    	int pos;
    	boolean exists = (pos = endpointNames.indexOf(key)) >= 0;
    	if (exists) {
            endpointNames.remove(pos);
            endpointReferences.remove(pos);
    	}
    	return exists;
    }
}
