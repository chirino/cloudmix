package org.apache.servicemix.grid.common.dto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import junit.framework.TestCase;

public class ProfileDetailsTest extends TestCase {
    public void testAnnotations() throws Exception {
        Class<ProfileDetails> cls = ProfileDetails.class;
        assertNotNull(cls.getAnnotation(XmlRootElement.class));
        assertEquals(XmlAccessType.FIELD, cls.getAnnotation(XmlAccessorType.class).value());
        
        Field features = cls.getDeclaredField("features");
        assertEquals("features", features.getAnnotation(XmlElement.class).name());
    }
    
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
