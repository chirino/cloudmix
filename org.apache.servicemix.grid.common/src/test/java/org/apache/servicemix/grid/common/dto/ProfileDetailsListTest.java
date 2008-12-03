package org.apache.servicemix.grid.common.dto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import junit.framework.TestCase;

public class ProfileDetailsListTest extends TestCase {
    public void testAnnotations() throws Exception {
        Class<ProfileDetailsList> cls = ProfileDetailsList.class;
        
        assertNotNull(cls.getAnnotation(XmlRootElement.class));
        
        XmlAccessorType accType = cls.getAnnotation(XmlAccessorType.class);
        assertEquals(XmlAccessType.FIELD, accType.value());
        
        Field profilesField = cls.getDeclaredField("profiles");
        XmlElement xmlElem = profilesField.getAnnotation(XmlElement.class);
        assertEquals("profiles", xmlElem.name());
    }
    
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
