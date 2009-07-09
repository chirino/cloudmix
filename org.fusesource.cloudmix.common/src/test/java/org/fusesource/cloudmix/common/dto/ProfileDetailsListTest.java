/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import junit.framework.TestCase;

public class ProfileDetailsListTest extends TestCase {
    
    public void testConstructor() {
        ProfileDetailsList pdl = new ProfileDetailsList();
        assertEquals(0, pdl.profiles.size());
        assertEquals(pdl.profiles, pdl.getProfiles());
    }
    
    public void testConstructor2() {
        ProfileDetails pd = new ProfileDetails("testing");
        ProfileDetailsList pdl = new ProfileDetailsList(Arrays.asList(pd));
        assertEquals(1, pdl.profiles.size());        
        assertEquals(pdl.profiles, pdl.getProfiles());
    }
    
    public void testProfileAccessors() {
        ProfileDetails pd = new ProfileDetails("testing");
        ProfileDetailsList pdl = new ProfileDetailsList(Arrays.asList(pd));
        assertEquals(1, pdl.profiles.size());        

        pdl.setProfiles(new ArrayList<ProfileDetails>());
        assertEquals(0, pdl.profiles.size());
        assertEquals(pdl.profiles, pdl.getProfiles());
    }
}
