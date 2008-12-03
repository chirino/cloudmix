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
package org.apache.servicemix.grid.agent.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.servicemix.grid.agent.resources.LifecycleObserver;
import org.apache.servicemix.grid.agent.resources.LifecycleObservable;
import org.apache.servicemix.grid.common.HttpAuthenticator;
import org.springframework.context.ApplicationContext;

import com.sun.jersey.api.core.ResourceConfig;
import org.springframework.context.ConfigurableApplicationContext;
import com.sun.jersey.spi.container.WebApplication;

/**
 * Provide support for lifecycle-aware beans, in lieu of the @PostConstruct
 * and @PreDestroy annotations.
 */
public class SpringServlet
    extends org.apache.servicemix.grid.common.spring.SpringServlet
    implements LifecycleObservable {
    
    private List<LifecycleObserver> lifecycleAwareResources = 
        new ArrayList<LifecycleObserver>();

    public SpringServlet() {
        super();
    }
    
    public SpringServlet(ApplicationContext ctx, HttpAuthenticator ca) {
        super(ctx, ca);
    }
  
    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        super.initiate(rc, wa);

        Iterator<LifecycleObserver> i = lifecycleAwareResources.listIterator();
        while (i.hasNext()) {
            i.next().init();
        }
    }

    @Override
    protected SpringComponentProvider getComponentProvider(ConfigurableApplicationContext ctx) {
        return new SpringComponentProvider(ctx, this);
    }

    @Override
    public synchronized void destroy() {
        Iterator<LifecycleObserver> i = lifecycleAwareResources.listIterator();
        while (i.hasNext()) {
            i.next().destroy();
        }
    }

    public synchronized void register(LifecycleObserver observer) {
        lifecycleAwareResources.add(observer);
    }

    public synchronized void deregister(LifecycleObserver observer) {
        lifecycleAwareResources.remove(observer);
    }
}
