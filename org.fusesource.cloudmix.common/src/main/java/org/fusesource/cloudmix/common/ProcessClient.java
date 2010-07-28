/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common;

import java.net.URISyntaxException;

import com.sun.jersey.api.client.WebResource;


/**
 * An API for interacting with a process created to execute a feature within an agent
 *
 * @version $Revision: 1.1 $
 */
public interface ProcessClient {
    WebResource directoryResource(String uri) throws URISyntaxException;
}
