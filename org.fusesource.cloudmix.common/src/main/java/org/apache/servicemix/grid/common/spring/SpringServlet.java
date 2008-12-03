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
