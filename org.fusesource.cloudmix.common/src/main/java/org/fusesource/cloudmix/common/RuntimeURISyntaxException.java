/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common;


/**
 * Avoid checked exceptions for bad URIs in the API
 *
 * @version $Revision: 1.1 $
 */
public class RuntimeURISyntaxException extends RuntimeException {
    private String uri;

    public RuntimeURISyntaxException(String uri, Throwable cause) {
        super("Could not parse uri '" + uri + "'. Reason: " + cause, cause);
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}
