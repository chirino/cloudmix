package org.apache.servicemix.grid.agent;

import junit.framework.TestCase;

public class PidUtilsTest extends TestCase {
    public void testGetPid() {
        int pid = PidUtils.getPid();
        assertTrue("Pid Utils should return a meaningful pid", pid > 0);
        assertEquals("The pid should remain the same", pid, PidUtils.getPid());
    }
}
