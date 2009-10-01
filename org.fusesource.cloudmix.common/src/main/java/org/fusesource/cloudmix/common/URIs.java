/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A helper class for working with URIs
 *
 * @version $Revision: 1.1 $
 */
public class URIs {

    /**
     * Create a new URI without a checked exception
     *
     * @param uri the URI to create
     * @return the newly created URI
     * @throws RuntimeURISyntaxException if the String could not be turned into a URI 
     */
    public static URI createURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeURISyntaxException(uri, e);
        }
    }
}
