/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.fusesource.cloudmix.common.CloudmixHelper;
import org.fusesource.cloudmix.common.jaxrs.JAXBContextResolver;
import org.fusesource.cloudmix.agent.security.PasswordProvider;
import org.fusesource.cloudmix.agent.security.SecurityUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A useful base class for any RESTful client facacde.
 *
 * @version $Revision: 1.1 $
 */
public class RestClientSupport {
    private static final transient Log LOG = LogFactory.getLog(RestClientSupport.class);
    
    private Client client;
    private URI rootUri;
    private RestTemplate template = new RestTemplate();
    private String username;
    private PasswordProvider passwordProvider;
    private String credentials;
    private boolean loggedNoPassword;

    public RestClientSupport() {
    }

    @Override
    public String toString() {
        return "RestClient[rootUri: " + rootUri + "]";
    }

    public Client getClient(String credentials) {
        //TODO: find a way around the classloader magic to get this working in Karaf 
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(HeaderDelegate.class.getClassLoader());
            if (client == null) {
                DefaultClientConfig config = new DefaultClientConfig();
                config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, Boolean.FALSE);
                config.getClasses().add(JAXBContextResolver.class);

                client = Client.create(config);
                if (credentials != null) {
                    client.addFilter(new AuthClientFilter(credentials));
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);            
        }
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public RestTemplate getTemplate() {
        return template;
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }

    public URI getRootUri() throws URISyntaxException {
        if (rootUri == null) {
            setRootUri(new URI(CloudmixHelper.getDefaultRootUrl()));
        }
        return rootUri;
    }

    public void setRootUri(URI rootUri) throws URISyntaxException {
        setRootUri(rootUri, true);
    }

    public void setRootUri(URI rootUri, boolean appendSlash) throws URISyntaxException {
        if (appendSlash && !rootUri.toString().endsWith("/")) {
            rootUri = new URI(rootUri.toString() + "/");
        }
        this.rootUri = rootUri;
    }
    

    protected URI append(URI uri, String... s) throws URISyntaxException {
        StringBuffer buffer = new StringBuffer(uri.toString());
        for (String s1 : s) {
            if (s1.contains("/")) {
                buffer.append(s1);
            } else {
                try {
                    String urlEnString = URLEncoder.encode(s1, "UTF-8");
                    buffer.append(urlEnString);
                } catch (Exception e) {
                    throw new URISyntaxException(s1, e.toString());
                }
            }


        }
        return new URI(buffer.toString());
    }

    public void setUsername(String u) {
        username = u;
    }

    public String getUsername() {
        return username;
    }

    public void setPasswordProvider(PasswordProvider pp) {
        passwordProvider = pp;
    }

    public PasswordProvider getPasswordProvider() {
        return passwordProvider;
    }

    public void setCredentials(String c) {
        credentials = c;
    }

    public String getCredentials() {
        if (credentials == null) {
            // Determine credentials from username/password
            if (username == null) {
                return null;
            }
            LOG.debug("Getting credentials for user " + username);
            if (passwordProvider == null) {
                if (!loggedNoPassword) {
                    loggedNoPassword = true;
                    LOG.warn("cannot provide credentials for user \"" + username
                            + "\", no password provider");
                }

                return null;
            }
            char[] password = passwordProvider.getPassword();
            if (password == null) {
                if (!loggedNoPassword) {
                    loggedNoPassword = true;
                    LOG.warn("cannot provide credentials for user \"" + username
                            + "\", no password provided");
                }
                return null;
            }
            credentials = SecurityUtils.toBasicAuth(username, password);
        }

        return credentials;
    }

    protected WebResource resource(URI uri) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("about to use URI: " + uri);
        }
        return getClient(getCredentials()).resource(uri);
    }
}
