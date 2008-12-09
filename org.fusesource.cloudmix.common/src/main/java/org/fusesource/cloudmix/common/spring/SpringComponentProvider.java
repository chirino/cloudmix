/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.spring;

import com.sun.jersey.spi.service.ComponentContext;
import com.sun.jersey.spi.service.ComponentProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @version $Revision: 1.1 $
 */
public class SpringComponentProvider implements ComponentProvider {
    private static final transient Log LOG = LogFactory.getLog(SpringComponentProvider.class);
    private final ConfigurableApplicationContext applicationContext;
    private int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;
    private boolean dependencyCheck;

    public SpringComponentProvider(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public <T> T getInjectableInstance(T value) {
        // TODO
        return value;
    }

    public <T> T getInstance(Scope scope, Class<T> type) throws InstantiationException, IllegalAccessException {
        String name = getBeanName(type);
        Object value;
        if (name != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found bean named: " + name);
            }
            value = applicationContext.getBean(name, type);

        } else {
            LOG.debug("No bean name so using BeanFactory.createBean");
            value = applicationContext.getBeanFactory().createBean(type, autowireMode, dependencyCheck);
        }
        return type.cast(value);
    }

    public <T> T getInstance(ComponentContext componentContext, Scope scope, Class<T> type) throws InstantiationException, IllegalAccessException {
        return getInstance(scope, type);
    }

    protected <T> String getBeanName(Class<T> type) {
        String[] names = applicationContext.getBeanNamesForType(type);
        String name = null;
        if (names.length == 1) {
            name = names[0];
        }
        return name;
    }

    public <T> T getInstance(Scope scope,
                             Constructor<T> constructor,
                             Object[] objects) throws InstantiationException,
            IllegalArgumentException,
            IllegalAccessException,
            InvocationTargetException {
        return constructor.newInstance(objects);
    }

    public void inject(Object object) {
        String beanName = getBeanName(object.getClass());
        if (beanName != null) {
            ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
            beanFactory.configureBean(object, beanName);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public int getAutowireMode() {
        return autowireMode;
    }

    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    public boolean isDependencyCheck() {
        return dependencyCheck;
    }

    public void setDependencyCheck(boolean dependencyCheck) {
        this.dependencyCheck = dependencyCheck;
    }
}
