/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent;

import java.net.URI;

import junit.framework.TestCase;
import org.fusesource.cloudmix.agent.RestClientSupport;

public class RestClientSupportTest extends TestCase {
    public void testRootURI() throws Exception {
        RestClientSupport rcs = new RestClientSupport();
        assertEquals("http://localhost:9091/", rcs.getRootUri().toString());
        
        rcs.setRootUri(new URI("http://localhost:9091/controller"));
        assertEquals("http://localhost:9091/controller/", rcs.getRootUri().toString());
        
        rcs.setRootUri(new URI("http://localhost:9091"));
        assertEquals("http://localhost:9091/", rcs.getRootUri().toString());

        rcs.setRootUri(new URI("http://localhost:9091/"));
        assertEquals("http://localhost:9091/", rcs.getRootUri().toString());

        rcs.setRootUri(new URI("http://localhost:9091/controller/"));
        assertEquals("http://localhost:9091/controller/", rcs.getRootUri().toString());
    }
}
