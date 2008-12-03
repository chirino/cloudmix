package org.apache.servicemix.grid.common;

import javax.servlet.http.HttpServletRequest;

public interface HttpAuthenticator {

	boolean authenticate(HttpServletRequest request);

}
