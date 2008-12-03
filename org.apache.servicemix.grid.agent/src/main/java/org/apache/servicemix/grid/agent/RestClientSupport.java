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
package org.apache.servicemix.grid.agent;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import com.sun.jersey.api.client.Client;

/**
 * A useful base class for any RESTful client facacde.
 *
 * @version $Revision: 1.1 $
 */
public class RestClientSupport {

    private Client client;
    private URI rootUri;
    private RestTemplate template = new RestTemplate();
    
    public Client getClient(String credentials) {
        if (client == null) {
            client = Client.create();
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
            setRootUri(new URI("http://localhost:9091"));
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
            if(s1.contains("/")) {
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
