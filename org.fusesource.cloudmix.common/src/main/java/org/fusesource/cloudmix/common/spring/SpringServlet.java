/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.spring;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.HttpAuthenticator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @version $Revision: 1.1 $
 */
public class SpringServlet extends ServletContainer {
	
    private static final transient Log LOG = LogFactory.getLog(SpringServlet.class);

    private ApplicationContext applicationContext;
	private HttpAuthenticator authenticator;
	private boolean initialized = false;

    public SpringServlet() {
        super();
    }
    
    public SpringServlet(ApplicationContext ctx, HttpAuthenticator ca) {
        applicationContext = ctx;
        authenticator = ca;
    }
    
    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        ServletContext servletContext = getServletContext();
        ApplicationContext ctx;

        if (applicationContext != null) {
            ctx = applicationContext;
        } else {
            ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        }

        wa.initiate(rc, getComponentProvider((ConfigurableApplicationContext) ctx));
        initialized = true;
    }

    protected SpringComponentProvider getComponentProvider(ConfigurableApplicationContext ctx) {
        return new SpringComponentProvider(ctx);
    }
    
    
    /**
     * Overriding service method to provide a authentication filter.  This calls the authenticator
     * if there is one to perform authentication.
     */
    @Override
    public void service(final ServletRequest request, final ServletResponse response) throws ServletException, IOException {
    
        if (authenticator != null) {
            if (!(request instanceof HttpServletRequest)) {
                throw new ServletException("Need a HttpServletRequest for authentication");
            }
            if (!(response instanceof HttpServletResponse)) {
                throw new ServletException("Need a HttpServletRequest for authentication");
            }
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            // Check for race condition where are request arrives before this 
            // application context has been initialized.
            if (!initialized) {
                LOG.info("Application context not initialized; returning 503 for request " + httpRequest.getRequestURI());                
                httpResponse.sendError(503, "Service not initialized");
                httpResponse.addHeader("Retry-After", "10");
                return;
            }

    		
    		try {
    			LOG.info("Controller request " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());
    			if (authenticator.authenticate(httpRequest)) {
        			super.service(request, response);
    			} else {
        			LOG.error("Authentication failed for request " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());
        			httpResponse.sendError(401, "Cannot authenticate request " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());
    			}
    		} finally {
    			// TODO - would like to log the response status here.
    		}
    		
    	} else {
    		super.service(request, response);	
    	}

    }
}
