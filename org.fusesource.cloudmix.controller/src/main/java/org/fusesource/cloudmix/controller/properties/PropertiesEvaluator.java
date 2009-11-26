/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.properties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.GridClients;
import org.fusesource.cloudmix.common.dto.AgentDetails;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;
import org.fusesource.cloudmix.common.dto.PropertyDefinition;


/**
 * @version $Revision: 1.1 $
 */
public class PropertiesEvaluator {
    private static final transient Log LOG = LogFactory.getLog(PropertiesEvaluator.class);

    private final GridClient client;
    private final ExpressionCache cache;

    public PropertiesEvaluator(GridClient client, ExpressionFactory expressionFactory) {
        this.client = client;
        this.cache = new ExpressionCache(expressionFactory);
    }

    public Properties evaluateProperties() {
        Properties answer = new Properties();
        List<FeatureDetails> list = client.getFeatures();
        if (list != null) {
            List<FeatureDetails> features = list;
            evaluateProperties(answer, features);
        }
        return answer;
    }


    /**
     * Evaluate the properties for the given profile
     */
    public Properties evaluateProperties(ProfileDetails profile) {
        List<FeatureDetails> features = GridClients.getFeatureDetails(client, profile);
        return evaluateProperties(features);
    }

    /**
     * Evaluate the properties for the given profile ID
     */
    public Properties evaluateProperties(String profileId) {
        ProfileDetails details = client.getProfile(profileId);
        if (details == null) {
            throw new IllegalArgumentException("No ProfileDetails for profileId: " + profileId);
        }
        return evaluateProperties(details);
    }


    public Properties evaluateProperties(List<FeatureDetails> features) {
        Properties answer = new Properties();
        evaluateProperties(answer, features);
        return answer;
    }

    public void evaluateProperties(Properties answer, List<FeatureDetails> features) {
        for (FeatureDetails feature : features) {
            evaluateProperties(answer, feature);
        }
    }

    public void evaluateProperties(Properties answer, FeatureDetails feature) {
        List<PropertyDefinition> list = feature.getProperties();
        if (list != null) {
            for (PropertyDefinition property : list) {
                Expression evaluator = cache.getExpression(property);
                if (evaluator != null) {
                    String id = property.getId();
                    if (answer.contains(id)) {
                        LOG.warn("Duplicate property definition id " + id + " from feature " + feature);
                    }
                    Map<String, Object> variables = createVariables(feature);
                    Object value = evaluator.evaluate(variables);
                    if (value != null) {
                        answer.put(id, value);
                    }
                }
            }
        }
    }

    protected Map<String, Object> createVariables(FeatureDetails feature) {
        Map<String, Object> answer = new HashMap<String, Object>();

        List<AgentDetails> agents = GridClients.getAgentDetailsAssignedToFeature(client, feature.getId());
        answer.put("agents", agents);

        return answer;
    }
}
