/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.properties;


import java.util.Map;

import org.fusesource.cloudmix.common.dto.PropertyDefinition;
import org.fusesource.cloudmix.controller.util.LRUCache;

/**
 * A cache of evaluators
 *
 * @version $Revision: 1.1 $
 */
public class ExpressionCache {

    private final ExpressionFactory expressionFactory;
    private final Map<String, Expression> cache;

    public ExpressionCache(ExpressionFactory expressionFactory) {
        this(5000, expressionFactory);
    }

    public ExpressionCache(int capacity, ExpressionFactory expressionFactory) {
        this.expressionFactory = expressionFactory;
        this.cache = new LRUCache<String, Expression>(capacity);
    }

    public Expression getExpression(PropertyDefinition property) {
        String key = property.getExpression();
        synchronized (cache) {
            Expression answer = cache.get(key);
            if (answer == null) {
                answer = expressionFactory.createExpression(key);
                cache.put(key, answer);
            }
            return answer;
        }
    }
}
