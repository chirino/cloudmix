/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlAttribute;

import junit.framework.TestCase;

public class AgentDetailsTest extends TestCase {
    public void testProfile() throws Exception {
        Class<AgentDetails> cls = AgentDetails.class;
        
        Field profile = cls.getDeclaredField("profile");
        profile.setAccessible(true);
        assertTrue(profile.getAnnotation(XmlAttribute.class).required());
        
        AgentDetails ad = new AgentDetails();
        assertEquals("default", ad.getProfile());
        ad.setProfile("production");
        assertEquals("production", ad.getProfile());
        assertEquals("production", profile.get(ad));
    }
}
