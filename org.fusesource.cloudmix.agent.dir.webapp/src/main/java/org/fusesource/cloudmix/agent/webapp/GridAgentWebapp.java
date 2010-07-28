/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.webapp;

import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.AgentPoller;
import org.fusesource.cloudmix.agent.Bundle;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.agent.dir.DirectoryInstallerAgent;
import org.fusesource.cloudmix.common.dto.AgentDetails;


public class GridAgentWebapp {

    public static final String IMAGES_ROOT = "images/";
    public static final String STYLESHEET_HREF = "css/main.css";

    private static final Log LOGGER = LogFactory.getLog(GridAgentWebapp.class);

    private RestGridClient gridClient;
    private DirectoryInstallerAgent agent;
    private AgentPoller poller;
    private String servletName;
    private String serverName;
    
    public GridAgentWebapp() {
        // Complete
    }
    
    public void destroy() {
        try {
            LOGGER.info("destroying poller");
            agent.setClient(null);
            poller.destroy();
        } catch (Exception e) {
            LOGGER.warn("Exception destroying poller; " + e);
        }
    }
    
    public void init(ServletConfig config) throws ServletException {
        
        LOGGER.debug("---------------------------------------------");
        LOGGER.debug("Initializing Grid Agent");


        LOGGER.info("initialising agent");
        try {
            
            servletName = config.getServletContext().getServletContextName();
            serverName = config.getServletContext().getServerInfo();

            LOGGER.debug("\nConfiguration:\n");
            LOGGER.debug("  Name:              " + agent.getAgentName());
            LOGGER.debug("  ID:                " + agent.getAgentId());
            LOGGER.debug("  Profile:           " + agent.getProfile());
            LOGGER.debug("  Properties File:   " + agent.getDetailsPropertyFilePath());
            LOGGER.debug("  Max Features:      " + agent.getMaxFeatures());
            LOGGER.debug("  Link:              " + agent.getAgentLink());
            LOGGER.debug("  Package Types:     " + getAgentPackageTypes());
            LOGGER.debug("  Type:              " + agent.getContainerType());
            LOGGER.debug("  Install Directory: " + agent.getInstallDirectory());
            LOGGER.debug("  Repository URI:    " + gridClient.getRootUri());
            LOGGER.debug("  Agent Username:    " + gridClient.getUsername());
            LOGGER.debug("  Password Provider: " + gridClient.getPasswordProvider()
                             .getClass().getSimpleName());
            LOGGER.debug("  Polling Dealay:    " + poller.getInitialPollingDelay());
            LOGGER.debug("  Polling Period:    " + poller.getPollingPeriod());

            LOGGER.debug("\nGrid Agent Ready");
            LOGGER.debug("---------------------------------------------");
            LOGGER.debug("\n");
            //poller.start();
            
        } catch (Exception e) {
            LOGGER.error("Excepton " + e, e);
        }

    }


    public String getStatus() {
        // TODO this code is a bit sick! :)
        // its better to use a template engine instead for doing HTML representions!
        // see Jersey implicit views or the controller web application's HTML templates

        AgentDetails details = agent.getAgentDetails();
        StringBuilder sb = new StringBuilder()
            .append("<html><head>\n")
            .append("<link href=\"")
            .append(STYLESHEET_HREF)
            .append("\" rel=\"stylesheet\" type=\"text/css\">\n")
            .append("<title>").append(servletName).append("</title>\n")
            .append("</head>\n")
            .append("<body><img src=\"images/logo.gif\"/>\n")
            .append("<h1>").append(servletName).append("</h1>\n");
        try {
            sb.append("<h2>Properties</h2>\n")
                .append("<table>\n")
                .append("<tr><td><b>Agent Profile</b></td><td><i>\n")
                .append(agent.getProfile()).append("</i></td></tr>\n")
                .append("<tr><td><b>Agent Host</b></td><td><i>\n")
                .append(agent.getHostName()).append("</i></td></tr>\n")
                .append("<tr><td><b>Agent OS</b></td><td><i>\n")
                .append(details.getOs()).append("</i></td></tr>\n")
                .append("<tr><td><b>Agent PID</b></td><td><i>\n")
                .append(details.getPid()).append("</i></td></tr>\n")
                .append("<tr><td><b>Agent Link</b></td><td><i>\n")
                .append(details.getAgentLink()).append("</i></td></tr>\n")
                .append("<tr><td><b>Agent Container</b></td><td><i>\n")
                .append(details.getContainerType()).append("</i></td></tr>\n")
                .append("<tr><td><b>Package types</b></td><td><i>\n");
            
            for (String packageType : details.getSupportPackageTypes()) {
                sb.append(packageType + " ");
            }            
            sb.append("</i></td></tr>\n")
                .append("<tr><td><b>Install Directory</b></td><td><i>")
                .append(agent.getInstallDirectory()).append("</i></td></tr>\n")
                .append("<tr><td><b>Temp Suffix</b></td><td><i>")
                .append(agent.getTempSuffix()).append("</i></td></tr>\n")
                .append("<tr><td><b>Max Features</b></td><td><i>")
                .append(agent.getMaxFeatures()).append("</i></td></tr>\n")
                .append("<tr><td><b>Repository URI</b></td><td><i>")
                .append(gridClient.getRootUri()).append("</i></td></tr>\n")
                .append("<tr><td><b>Polling Period</b></td><td><i>")
                .append(poller.getPollingPeriod()).append("</i></td></tr>\n")
                .append("<tr><td><b>Initial Polling Delay</b></td><td><i>")
                .append(poller.getInitialPollingDelay()).append("</i></td></tr>\n")
                .append("</table>\n")
                .append("<h2>Features</h2>");
                        
            Set<String> features = details.getCurrentFeatures();
            if (features == null || features.isEmpty()) {
                sb.append("<i>No features installed</i>\n");
            } else {
                sb.append("<table>\n");
                for (String f : features) {
                    sb.append("<tr><td valign=\"top\"><div id=\"application\">")
                        .append(f)
                        .append("</div></td><td><ul id=\"artifact\">");
                    for (Bundle bundle : agent.getFeatureBundles(f)) {
                        sb.append("<li>");
                        String name = bundle.getName();
                        if (name != null && !"".equals(name)) {
                            sb.append(name).append(", ");
                        }
                                                
                        String uri = bundle.getUri();
                        sb.append("<a href=\"").append(uri).append("\">")
                          .append(uri).append("</a>")
                          .append("</li>\n");
                    }
                    sb.append("</ul></td></tr>\n");                    
                }
                sb.append("</table>");
            }
            
            sb.append("<hr noshade><i>").append(serverName).append("</i></hr>\n");
            
            // TODO : add link for history?
            
        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Error! " + e);
        }
        sb.append("</body>\n").append("</html>\n");
        
        return sb.toString();
    }

    
    public void setClient(RestGridClient gridclient) {
        this.gridClient = gridclient;
    }


    public synchronized RestGridClient getClient() {
        if (gridClient == null) {
            gridClient = new RestGridClient();
        }
        return gridClient;
    }

    public void setAgent(DirectoryInstallerAgent agent) {
        this.agent = agent;
    }

    public DirectoryInstallerAgent getAgent() {
        return agent != null
               ? agent
               : new DirectoryInstallerAgent();
    }

    public void setPoller(AgentPoller poller) {
        this.poller = poller;
    }

    private String getAgentPackageTypes() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String t : agent.getSupportPackageTypes()) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append(t);
        }
        return sb.toString();        
    }
    
}
