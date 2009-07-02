/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.io.IOException;

import junit.framework.TestCase;

public class PidUtilsTest extends TestCase {
    public void testGetPid() {
        int pid = PidUtils.getPid();
        assertTrue("Pid Utils should return a meaningful pid", pid > 0);
        assertEquals("The pid should remain the same", pid, PidUtils.getPid());
    }
    
    public void testKillPid() throws IOException {
    	// Need to figure out a good way to start a process get it's pid and then kill it.
//    	PidUtils.killPid(1972, 0, 1);
    }
}
