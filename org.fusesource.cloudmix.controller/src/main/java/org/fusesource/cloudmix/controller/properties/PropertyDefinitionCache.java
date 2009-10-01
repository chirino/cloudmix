/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.properties;

import org.fusesource.cloudmix.common.dto.PropertyDefinition;
import org.fusesource.cloudmix.controller.util.LRUCache;

import java.util.Map;

/**
 * A cache of evaluators
 *
 * @version $Revision: 1.1 $
 */
public class PropertyDefinitionCache {

    private Map<String,PropertyEvaluator> cache;

    public PropertyDefinitionCache() {
        this(5000);
    }

    public PropertyDefinitionCache(int capacity) {
        this.cache = new LRUCache<String,PropertyEvaluator>(capacity);
    }

    public PropertyEvaluator getEvaluator(PropertyDefinition property) {
        String key = property.getExpression();
        synchronized (cache) {
            PropertyEvaluator answer = cache.get(key);
            if (answer == null) {
                answer = new PropertyEvaluator(key);
                cache.put(key, answer);
            }
            return answer;
        }
    }
}
