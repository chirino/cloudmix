package org.fusesource.cloudmix.controller.security;

import javax.servlet.http.HttpServletRequest;

import org.fusesource.cloudmix.common.HttpAuthenticator;

public class NoOpAuthenticator implements HttpAuthenticator {

    public boolean authenticate(HttpServletRequest request) {
        return true;
    }
}
