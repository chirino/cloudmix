package org.apache.servicemix.grid.agent;

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
