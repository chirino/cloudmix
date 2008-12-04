/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.jetty.WebServer;

/**
 * @version $Revision: 1.1 $
 */
public abstract class RuntimeTestSupport extends TestCase {
    protected WebServer webServer = new WebServer();


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        webServer.start();
    }

    @Override
    protected void tearDown() throws Exception {
        webServer.stop();
    }

    /**
     * Let stop and restart a clean webserver from scratch to simulate failover
     */
    protected void restartWebServer() throws Exception {
        if (webServer != null) {
            System.out.println("Stopping the web server");
            webServer.stop();
        }
        webServer = new WebServer();
        webServer.start();
        System.out.println("Web Server restarted");
    }
}
