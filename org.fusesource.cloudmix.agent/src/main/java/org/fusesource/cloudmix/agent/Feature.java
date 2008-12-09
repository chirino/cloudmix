/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Feature implements Serializable {

    private static final long serialVersionUID = 1922479782169770368L;

    private String featureName;
	private FeatureList featureList;
    private List<Bundle> bundles;
    private Map<String, Properties> propertyMap;
    private Map<String, Object> agentProperties;

    public Feature(String name, FeatureList fl) {
        featureName = name;
        featureList = fl;
    }    
    
	public FeatureList getFeatureList() {
		return featureList;
	}
	
	public Map<String, Object> getAgentProperties() {
	    if (agentProperties == null) {
	        agentProperties = new HashMap<String, Object>();
	    }
	    return agentProperties;
	}
	
    public void addDependency(String aFeatureName) {
        // TODO
    }

    public void addProperties(String name, Properties properties) {
        getPropertyMap().put(name, properties);
    }        
    
    public Properties getProperties(String name) {
        return getPropertyMap().get(name);
    }
    
    public Collection<String> getPropertyNames() {
        return getPropertyMap().keySet();
    }

    public String getName() {
        return featureName;
    }
    
    public void addBundle(Bundle bundle) {
        getBundles().add(bundle);
    }

    public List<Bundle> getBundles() {
        if (bundles == null) {
            bundles = new ArrayList<Bundle>();
        }
        return bundles;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" feature \"").append(featureName).append("\" {\n");
        for (Bundle b : getBundles()) {
            sb.append("    ").append(b.toString()).append("\n");
        }
        sb.append(" }\n");
        return sb.toString();
    }
       
    private Map<String, Properties> getPropertyMap() {
        if (propertyMap == null) {
            propertyMap = new HashMap<String, Properties>();
        }
        return propertyMap;
    }



}
