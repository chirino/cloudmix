/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.resources.LifecycleObserver;
import org.fusesource.cloudmix.agent.resources.LifecycleObservable;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Provide support for lifecycle-aware beans, in lieu of the @PostConstruct
 * and @PreDestroy annotations.
 */
public class SpringComponentProvider 
    extends org.fusesource.cloudmix.common.spring.SpringComponentProvider {

    private static final transient Log LOG = 
        LogFactory.getLog(SpringComponentProvider.class);

    private LifecycleObservable lifecycleObservable;

    public SpringComponentProvider(ConfigurableApplicationContext appCtxt,
                                   LifecycleObservable lifecycleObservable) {
        super(appCtxt);
        this.lifecycleObservable = lifecycleObservable;
    }

    public <T> T getInstance(Scope scope, Class<T> type) 
        throws InstantiationException, IllegalAccessException {
        T value = super.getInstance(scope, type);
        if (value instanceof LifecycleObserver) {
            lifecycleObservable.register((LifecycleObserver)value);
        }
        return value;
    }
}
