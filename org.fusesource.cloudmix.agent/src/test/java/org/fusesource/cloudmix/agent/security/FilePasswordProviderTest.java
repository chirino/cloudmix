/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.security;

import junit.framework.TestCase;

public class FilePasswordProviderTest extends TestCase {
    
    public void testFilePasswordProvider() throws Exception {
        
        FilePasswordProvider provider = new FilePasswordProvider();
        assertNull(provider.getPassword());
        
        provider.setPasswordFile("unknown");
        assertNull(provider.getPassword());
        
        provider.setPasswordFile(getResourceFile("password1.txt"));
        assertEquals("hunter1", String.valueOf(provider.getPassword()));

        provider.setPasswordFile(getResourceFile("password2.txt"));
        assertEquals("hunter2", String.valueOf(provider.getPassword()));

        provider.setPasswordFile(getResourceFile("password3.txt"));
        assertEquals("hunter3", String.valueOf(provider.getPassword()));
        
        provider.setPasswordFile(getResourceFile("password4.txt"));
        assertNull(provider.getPassword());
    }

    public void testSimplePasswordProvider() throws Exception {
        SimplePasswordProvider provider = new SimplePasswordProvider();
        assertNull(provider.getPassword());
        
        String expected = "secret";
        provider.setRawPassword(expected);
        assertEquals("secret", String.valueOf(provider.getPassword()));

    }
    private String getResourceFile(String name) {
        return this.getClass().getResource("/security/" + name).getFile();
    }

}
