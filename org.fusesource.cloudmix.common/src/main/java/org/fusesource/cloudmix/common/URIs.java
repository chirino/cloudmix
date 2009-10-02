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

    /**
     * Appends the given URI paths together so that there is a / between each
     * path
     * @param paths
     * @return
     */
    public static String appendPaths(String... paths) {
        StringBuilder buffer = new StringBuilder();
        int counter = 0;
        for (String path : paths) {
            if (counter++ > 0) {
                buffer.append("/");
            }
            // TODO maybe a regex would be faster than these 2 loops?
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
            while (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            buffer.append(path);
        }
        return buffer.toString();
    }
}
