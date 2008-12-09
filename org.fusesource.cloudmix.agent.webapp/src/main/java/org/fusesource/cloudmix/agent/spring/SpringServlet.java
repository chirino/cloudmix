/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fusesource.cloudmix.agent.resources.LifecycleObserver;
import org.fusesource.cloudmix.agent.resources.LifecycleObservable;
import org.fusesource.cloudmix.common.HttpAuthenticator;
import org.springframework.context.ApplicationContext;

import com.sun.jersey.api.core.ResourceConfig;
import org.springframework.context.ConfigurableApplicationContext;
import com.sun.jersey.spi.container.WebApplication;

/**
 * Provide support for lifecycle-aware beans, in lieu of the @PostConstruct
 * and @PreDestroy annotations.
 */
public class SpringServlet
    extends org.fusesource.cloudmix.common.spring.SpringServlet
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
