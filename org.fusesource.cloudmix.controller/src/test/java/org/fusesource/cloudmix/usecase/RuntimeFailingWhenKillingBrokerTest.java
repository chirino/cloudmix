/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.usecase;

/**
 * Tests the runtime (server side) failing over to a new instance and continuing to function
 *
 * @version $Revision$
 */
public class RuntimeFailingWhenKillingBrokerTest extends RuntimeFailingTest {

    @Override
    public void testProvidion() throws Exception {
        // lets kill the broker and runtime
        assertProvisioningAfterServerRestart(true);
    }
}