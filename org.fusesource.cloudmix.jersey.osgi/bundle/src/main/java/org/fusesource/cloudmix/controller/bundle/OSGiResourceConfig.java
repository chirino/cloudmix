/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.server.impl.container.config.AnnotatedClassScanner;

public class OSGiResourceConfig extends DefaultResourceConfig {
    public static final String CLASS_NAMES = "jersey_osgi.classnames";
    
    public OSGiResourceConfig(String [] classes) {
        if (classes == null || classes.length == 0) {
            throw new IllegalArgumentException("Array of packages must not be null or empty");
        }
        
        init(classes);        
    }
    
    public OSGiResourceConfig(Map<String, Object> props) {
        this(getClasses(props));

        getProperties().putAll(props);
    }

    private void init(String[] classNames) {
        List<Class<?>> classes = loadClasses(classNames);
        
        AnnotatedClassScanner scanner = new AnnotatedClassScanner(Path.class, Provider.class);
        // prime the scanner manually, we can't use it searching 
        // functionality as it isn't compatible with the OSGi 
        // classloader.
        Set<Class<?>> matchingClasses = scanner.getMatchingClasses();
        matchingClasses.addAll(classes);

        getClasses().addAll(scanner.getMatchingClasses());
    }

    private List<Class<?>> loadClasses(String[] classNames) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (String name : classNames) {
            try {
                Class<?> clazz = getClass().getClassLoader().loadClass(name);
                classes.add(clazz);
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException("Could not load class " + name);
            }
        }
        return classes;
    }

    private static String [] getClasses(Map<String, Object> props) {
        Object v = props.get(CLASS_NAMES);
        if (v == null) {
            throw new IllegalArgumentException(CLASS_NAMES + " property is missing");
        }
        
        String [] classes = getClasses(v);
        if (classes.length == 0) {
            throw new IllegalArgumentException(CLASS_NAMES + " contains no visible classes");
        }
        return classes;
    }

    private static String[] getClasses(Object param) {
        if (param instanceof String) {
            return getClasses((String) param);
        } else if (param instanceof String []) {
            return getClasses((String []) param);            
        } else {
            throw new IllegalArgumentException(CLASS_NAMES + " must have a property " +
            		"value of type String or String []");
        }
    }
    
    private static String [] getClasses(String [] elements) {
        List<String> paths = new ArrayList<String>();
        for (String element : elements) {
            if (element == null || element.length() == 0) {
                continue;
            }
            
            paths.add(element);
        }
        return paths.toArray(new String [0]);
    }

    private static String [] getClasses(String paths) {
        return paths.split(";");
    }
}
