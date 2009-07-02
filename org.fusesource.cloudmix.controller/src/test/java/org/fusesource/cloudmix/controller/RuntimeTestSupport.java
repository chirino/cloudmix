/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.jetty.WebServer;

/**
 * @version $Revision: 1.1 $
 */
public abstract class RuntimeTestSupport extends TestCase {
    protected WebServer webServer;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        webServer = new WebServer();
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
        // lets preserve the port!
        // we could instead force the recreation of each agent/client/controller to associate the new port
        int port = 0;
        if (webServer != null) {
            port = webServer.getLocalPort();
            System.out.println("Stopping the web server");
            webServer.stop();
        }
        webServer = new WebServer();
        if (port > 0) {
            webServer.setPort(port);
        }
        webServer.start();
        System.out.println("Web Server restarted");
    }

    protected int getPort() {
        return webServer.getLocalPort();
    }

    protected String getRootUrl() {
        return webServer.getRootUrl();
    }
}
