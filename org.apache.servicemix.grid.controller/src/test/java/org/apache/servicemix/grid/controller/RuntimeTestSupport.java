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
package org.apache.servicemix.grid.controller;

import junit.framework.TestCase;

import org.apache.servicemix.grid.common.jetty.WebServer;

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
