/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common.jetty;

import java.io.File;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * A simple bootstrap class for starting Jetty in your IDE using the local web
 * application.
 *
 * @version $Revision: 565003 $
 */
public final class WebServer {

    protected static final String defaultWebAppDir = "src/main/webapp";

    private Server server = new Server();
    private int port = 9091;

    private String webAppDir;

    private String webAppContext = "/";

    public WebServer() {
    }

    public static void main(String[] args) throws Exception {
        // now lets start the web server
        WebServer server = new WebServer();
        if (args.length > 0) {
            String text = args[0];
            server.setPort(Integer.parseInt(text));
        }
        server.start();
    }

    public void start() throws Exception {
        System.out.println("Starting Web Server on port: " + port);
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setServer(server);
        WebAppContext context = new WebAppContext();

        if (webAppDir == null) {
            // we might not be run from inside the right module, so lets check
            File file = new File(defaultWebAppDir);
            if (file.exists()) {
                webAppDir = defaultWebAppDir;
            }
            else {
                webAppDir = "org.fusesource.cloudmix.runtime/" + defaultWebAppDir;
            }
            System.out.println("Defauling the web app dir to: " + webAppDir);
        }
        context.setResourceBase(webAppDir);
        context.setContextPath(webAppContext);
        context.setServer(server);
        server.setHandlers(new Handler[] {
            context
        });
        server.setConnectors(new Connector[] {
            connector
        });
        server.start();

        System.out.println();
        System.out.println("==============================================================================");
        System.out.println("Started the CloudMix Runtime: point your web browser at http://localhost:" + port + "/");
        System.out.println("==============================================================================");

        System.out.println();
    }

    public void stop() throws Exception {
        server.stop();
    }

    // Properties
    //-------------------------------------------------------------------------

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getWebAppContext() {
        return webAppContext;
    }

    public void setWebAppContext(String webAppContext) {
        this.webAppContext = webAppContext;
    }

    public String getWebAppDir() {
        return webAppDir;
    }

    public void setWebAppDir(String webAppDir) {
        this.webAppDir = webAppDir;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
