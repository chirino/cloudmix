package org.fusesource.cloudmix.agent;

import java.util.HashMap;
import java.util.Map;

public class AgentState {

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
