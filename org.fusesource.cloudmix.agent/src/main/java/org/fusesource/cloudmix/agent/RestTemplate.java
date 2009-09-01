/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Date;

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
            if (response != null && response.getStatus() < 300) {
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


    public int put(final WebResource.Builder resource, final Object body) {
        return retryLoop(new RestOperation() {
            public ClientResponse invoke() {
                return resource.put(ClientResponse.class, body);
            }
        });
    }

    public int put(final WebResource.Builder resource) {
        return retryLoop(new RestOperation() {
            public ClientResponse invoke() {
                return resource.put(ClientResponse.class);
            }
        });
    }

    public int delete(final WebResource.Builder resource) {
        return retryLoop(new RestOperation() {
            public ClientResponse invoke() {
                return resource.delete(ClientResponse.class);
            }
        });
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


    /**
     * Performs the given operation {@link #getRetryAttempts()} times returning the final error code
     */
    protected int retryLoop(RestOperation operation) {
        int status = -1;
        for (int i = 0; i < retryAttempts; i++) {
            ClientResponse response = operation.invoke();
            if (response != null) {
                status = response.getStatus();
                if (status < 300) {
                    break;
                }
            }
        }
        return status;
    }
}
