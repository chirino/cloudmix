/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import java.util.ArrayList;
import java.util.List;

public class AuthClientFilter extends ClientFilter {

    private String credentials;
    public AuthClientFilter(String c) {
        credentials = c;
    }
    
    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        
        // Upgrade Jersey version to get HttpHeaders.AUTHORIZATION            
        
        List<Object> header = new ArrayList<Object>();
        header.add(credentials);
        request.getMetadata().put("Authorization", header);
        return getNext().handle(request);
    }

}
