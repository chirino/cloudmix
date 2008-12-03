package org.fusesource.cloudmix.common;

import javax.servlet.http.HttpServletRequest;

public interface HttpAuthenticator {

	boolean authenticate(HttpServletRequest request);

}
