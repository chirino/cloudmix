/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent;

import com.sun.jersey.api.client.WebResource;

import org.fusesource.cloudmix.common.ProcessClient;
import org.fusesource.cloudmix.common.URIs;


/**
 * A client API for working with a process RESTfully
 *
 * @version $Revision: 1.1 $
 */
public class RestProcessClient extends RestClientSupport implements ProcessClient {
    private String root;

    public RestProcessClient(String rootUri) {
        this.root = rootUri;
        setRootUri(URIs.createURI(rootUri));
    }

    public WebResource directoryResource(String uri) {
        return resource(append(getRootUri(), "directory/", uri));
    }

    @Override
    public String toString() {
        return "RestProcessClient[rootUri: " + root + "]";
    }

}
