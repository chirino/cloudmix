package org.fusesource.cloudmix.agent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Bundle implements Serializable {
    
    private static final long serialVersionUID = 6758071045250220478L;

    private String name;
    private String type;
    private String uri;
    private Map<String, Object> agentProperties;
    
    public Bundle(String name, String type, String uri) {
        this.name = name;
        this.type = type;
        this.uri = uri;
    }
    
    public String getName() {
        return name;        
    }
    
    public Bundle setName(String aName) {
        name = aName;
        return this;
    }
    
    public String getType() {
        return type;
    }
    
    public Bundle setType(String t) {
        type = t;
        return this;
    }
    
    public String getUri() {
        return uri;
    }
    
    public Bundle setUri(String anUri) {
        uri = anUri;
        
        return this;
    }
    
    public Map<String, Object> getAgentProperties() {
        if (agentProperties == null) {
            agentProperties = new HashMap<String, Object>();
        }
        return agentProperties;
    }

    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{name: \"").append(name)
          .append("\", type: \"").append(type)
          .append("\", uri: \"").append(uri).append("\"}");
        return sb.toString();        
    }

}
