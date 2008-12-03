package org.apache.servicemix.grid.controller.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicemix.grid.common.HttpAuthenticator;

public class NoOpAuthenticator implements HttpAuthenticator {

    public boolean authenticate(HttpServletRequest request) {
        return true;
    }
}
