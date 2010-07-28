/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.util.ObjectHelper;

/**
 * @version $Revision$
 */
public final class CloudmixHelper {
    public static final String ROOT_URL_PROPERTY = "cloudmix.url";
    public static final String DEFAULT_ROOT_URL_VALUE = "http://localhost:8181/";

    private static String defaultRootUrl;
    
    private static final transient Log LOG = LogFactory.getLog(CloudmixHelper.class);

    private CloudmixHelper() {
        //utility class
    }

    /**
     * Returns the default URL used to connect to CloudMix controller using the
     * {@link #ROOT_URL_PROPERTY} system property (cloudmix.url) if its set
     */
    public static String getDefaultRootUrl() {
        if (ObjectHelper.isNullOrBlank(defaultRootUrl)) {
            String systemProperty = null;
            try {
                systemProperty = System.getProperty(ROOT_URL_PROPERTY);
                defaultRootUrl = systemProperty;
            } catch (Exception e) {
                LOG.warn("Could not look up system property " + ROOT_URL_PROPERTY + ". Reason: " + e, e);
            }
            if (ObjectHelper.isNullOrBlank(defaultRootUrl)) {
                defaultRootUrl = DEFAULT_ROOT_URL_VALUE;
            }
            LOG.info("Using default CloudMix URL: " + defaultRootUrl 
                     + " system property: " + ROOT_URL_PROPERTY + " = " + systemProperty);
        }
        return defaultRootUrl;
    }

    /**
     * Sets the default root URL.
     * Typically this method is only used in test cases where we create multiple web
     * servers with different ports/URLS
     */
    public static void setDefaultRootUrl(String defaultRootUrl) {
        CloudmixHelper.defaultRootUrl = defaultRootUrl;
    }
}
