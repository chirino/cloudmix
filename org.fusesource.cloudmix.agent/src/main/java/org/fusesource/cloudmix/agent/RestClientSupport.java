/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.config.ClientConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.CloudmixHelper;
import org.fusesource.cloudmix.common.jaxrs.JAXBContextResolver;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

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

    public RestClientSupport() {
    }

    @Override
    public String toString() {
        return "RestClient[rootUri: " + rootUri + "]";

    }

    public Client getClient(String credentials) {
        if (client == null) {
            DefaultClientConfig config = new DefaultClientConfig();
            config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, Boolean.FALSE);
            config.getClasses().add(JAXBContextResolver.class);

            client = Client.create(config);
            if (credentials != null) {
                client.addFilter(new AuthClientFilter(credentials));
            }
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
        if (!rootUri.toString().endsWith("/")) {
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
}
