/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent;

import com.sun.jersey.api.client.ClientResponse;

/**
 * A kind of closure used with the {@link RestTemplate} to allow retry operations to be done
 *
 * @version $Revision: 1.1 $
 */
public interface RestOperation {
    ClientResponse invoke();
}
