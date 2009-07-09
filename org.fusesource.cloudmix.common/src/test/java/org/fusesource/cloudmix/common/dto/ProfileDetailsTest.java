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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import junit.framework.TestCase;

public class ProfileDetailsTest extends TestCase {
    
    public void testID() {
        ProfileDetails pd = new ProfileDetails("abc");
        assertEquals("abc", pd.getId());
    }
    
    public void testDependencyAccessors() {
        ProfileDetails pd = new ProfileDetails();
        assertEquals(0, pd.getFeatures().size());
        
        List<Dependency> fs = new ArrayList<Dependency>();
        Dependency d = new Dependency();
        d.setFeatureId("f1");
        fs.add(d);
        pd.setFeatures(fs);
        
        assertEquals(1, pd.getFeatures().size());
        assertEquals(fs, pd.getFeatures());
        
    }
}
