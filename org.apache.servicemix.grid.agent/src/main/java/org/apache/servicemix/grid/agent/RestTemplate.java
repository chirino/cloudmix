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

import java.util.Date;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A helper class to wrap up retry logic.
 *
 * @version $Revision: 1.1 $
 */
public class RestTemplate {
    private static final transient Log LOG = LogFactory.getLog(RestTemplate.class);
    private int retryAttempts = 10;
    private long delayBetweenAttempts = 100L;
    private Date pollLastModifiedDate;
    private EntityTag pollETag;

    public <T> T get(WebResource.Builder resource, Class<T> resultType) {
        T answer = null;
        for (int i = 0; i < retryAttempts && answer == null; i++) {
            ClientResponse response = resource.get(ClientResponse.class);
            if (response.getStatus() < 300) {
                answer = response.getEntity(resultType);
            }
        }
        return answer;
    }

    /**
     * Polls the given resource only returning the value if its changed since the last time we asked for it
     */
    public <T> T poll(WebResource.Builder request, Class<T> resultType) {
        for (int i = 0; i < retryAttempts; i++) {

            /* I think we should ignore the If-Modified-Since header...
            if (pollLastModifiedDate != null) {
                request = request.header("If-Modified-Since", pollLastModifiedDate);
            } */
            if (pollETag != null) {
                request = request.header("If-None-Match", pollETag);
            }

            ClientResponse response = request.get(ClientResponse.class);
            int status = response.getStatus();

            // is the response unmodified
            if (status == 304) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unmodified results for: " + request);
                }
                return null;
            
            } else if (status == 200) {
                
                MultivaluedMap<String, String> metadata = response.getMetadata();
                Date lastModified = response.getLastModified();
                if (lastModified != null) {
                    pollLastModifiedDate = lastModified;
                }
                EntityTag etag = response.getEntityTag();
                if (etag != null) {
                    pollETag = etag;
                }
                T value = response.getEntity(resultType);
                if (value != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("New provisioning instructions: " + value);
                    }
                    return value;
                
                } else {
                    LOG.error("Could not find an entity for the response: " + response);
                }
            
            } else {
                LOG.warn("Unknown status code: " + status + " when polling: " + request);
                throw new NotFoundException("Status: " + status);
            }
        }
        return null;
    }

    public void put(WebResource.Builder resource, Object body) {
        for (int i = 0; i < retryAttempts; i++) {
            ClientResponse response = resource.put(ClientResponse.class, body);
            if (response.getStatus() < 300) {
                break;
            }
        }
    }

    public void put(WebResource.Builder resource) {
        for (int i = 0; i < retryAttempts; i++) {
            ClientResponse response = resource.put(ClientResponse.class);
            if (response.getStatus() < 300) {
                break;
            }
        }
    }

    public void delete(WebResource.Builder resource) {
        for (int i = 0; i < retryAttempts; i++) {
            ClientResponse response = resource.delete(ClientResponse.class);
            if (response.getStatus() < 300) {
                break;
            }
        }
    }

    public long getDelayBetweenAttempts() {
        return delayBetweenAttempts;
    }

    public void setDelayBetweenAttempts(long delayBetweenAttempts) {
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
}
