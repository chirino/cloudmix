/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import junit.framework.TestCase;

import java.net.URI;

import org.fusesource.cloudmix.common.CloudmixHelper;

public class RestClientSupportTest extends TestCase {
    public void testRootURI() throws Exception {
        RestClientSupport rcs = new RestClientSupport();
        String rootUrl = CloudmixHelper.getDefaultRootUrl();

        assertEquals(rootUrl, rcs.getRootUri().toString());

        rcs.setRootUri(new URI(rootUrl + "controller"));
        assertEquals(rootUrl + "controller/", rcs.getRootUri().toString());

        rcs.setRootUri(new URI(rootUrl.substring(0, rootUrl.length() - 1)));
        assertEquals(rootUrl, rcs.getRootUri().toString());

        rcs.setRootUri(new URI(rootUrl));
        assertEquals(rootUrl, rcs.getRootUri().toString());

        rcs.setRootUri(new URI(rootUrl + "controller/"));
        assertEquals(rootUrl + "controller/", rcs.getRootUri().toString());
    }
}
