package org.fusesource.cloudmix.agent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AgentState implements Serializable {

    private static final long serialVersionUID = 2809283835323250588L;

    private Map<String, Object> agentProperties;
    private Map<String, Feature> agentFeatures;
    
    
    public Map<String, Object> getAgentProperties() {

        if (agentProperties == null) {
            agentProperties = new HashMap<String, Object>();
        }
        return agentProperties;
    }
    
    public Map<String, Feature> getAgentFeatures() {
        
        if (agentFeatures == null) {
            agentFeatures = new HashMap<String, Feature>();
        }
        return agentFeatures;
        
    }        
    
}
