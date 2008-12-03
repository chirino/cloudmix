/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
