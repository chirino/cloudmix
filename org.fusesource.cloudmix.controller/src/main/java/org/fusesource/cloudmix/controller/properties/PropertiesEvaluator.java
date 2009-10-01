/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.PropertyDefinition;

import java.util.List;
import java.util.Properties;

/**
 * @version $Revision: 1.1 $
 */
public class PropertiesEvaluator {
    private static final transient Log LOG = LogFactory.getLog(PropertiesEvaluator.class);

    private final GridClient client;
    private PropertyDefinitionCache cache = new PropertyDefinitionCache();

    public PropertiesEvaluator(GridClient client) {
        this.client = client;
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
                PropertyEvaluator evaluator = cache.getEvaluator(property);
                if (evaluator != null) {
                    String id = property.getId();
                    if (answer.contains(id)) {
                        LOG.warn("Duplicate property definition id " + id + " from feature " + feature);
                    }
                    String value = evaluator.getValue();
                    if (value != null) {
                        answer.put(id, value);
                    }
                }
            }
        }
    }
}
