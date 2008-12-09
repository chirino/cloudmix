/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import org.fusesource.cloudmix.agent.FeatureList;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.agent.Bundle;

public class FeatureListTest extends TestCase {

    public void testFeatureList1() throws Exception {
        
        URL url = this.getClass().getResource("/features_1.xml");
        FeatureList flist = new FeatureList(url, null);
        assertNotNull(flist.toServiceMix4Doc());
        
        assertEquals(1, flist.getNumFeatures());        
        assertNull(flist.getFeature("unknown"));
        
        Feature f = flist.getFeature("f1");
        assertNotNull(f);        
        assertEquals("f1", f.getName());        
        assertSame(flist, f.getFeatureList());
        
        List<Bundle> bundles = f.getBundles();
        assertEquals(1, bundles.size());
        
        Bundle b = bundles.get(0);
        assertEquals("r_1.txt", b.getName());
        assertEquals("osgi", b.getType());
        assertEquals("http://example.com/r1.txt", b.getUri());
        
        Collection<String> names = f.getPropertyNames();
        assertNotNull(names);
        assertEquals(2, names.size());
        assertTrue(names.contains("ports"));
        assertTrue(names.contains("misc"));
        assertFalse(names.contains("unknown"));
        
        assertNull(f.getProperties("unknown"));
        Properties props = f.getProperties("ports");
        assertNotNull(props);
        assertEquals(2, props.size());
        assertEquals("1234", props.get("fooPort"));
        assertEquals("4321", props.get("barPort"));
        
        props = f.getProperties("misc");
        assertNotNull(props);
        assertEquals(2, props.size());
        assertEquals("Funky", props.get("name"));
        assertEquals("Monkey", props.get("desc"));
    }



	public void testFeatureList2() throws Exception {
        
        String url = this.getClass().getResource("/features_2.xml").toString();
        FeatureList flist = new FeatureList(url, null);

        assertEquals(1, flist.getNumFeatures());        
        assertNull(flist.getFeature("unknown"));
        
        Feature f = flist.getFeature("f2");
        assertNotNull(f);        
        assertEquals("f2", f.getName());        
        assertSame(flist, f.getFeatureList());
        
        List<Bundle> bundles = f.getBundles();
        assertEquals(3, bundles.size());
        
        Bundle b = bundles.get(0);
        assertEquals("r2.txt", b.getName());
        assertEquals("war", b.getType());
        assertEquals("http://example.com/r2.txt", b.getUri());

        b = bundles.get(1);
        assertEquals("", b.getName());
        assertEquals("", b.getType());
        assertEquals("http://example.com/r3.txt", b.getUri());

        b = bundles.get(2);
        assertEquals("", b.getName());
        assertEquals("", b.getType());
        assertEquals("http://example.com/r4.txt", b.getUri());
        
        Collection<String> names = f.getPropertyNames();
        assertNotNull(names);
        assertEquals(0, names.size());

    }    
	
    private void assertSameFeatures(FeatureList flist1, FeatureList flist2) {
		// TODO: need to complete this test.
		assertEquals(flist1.getNumFeatures(), flist2.getNumFeatures());
	}
}
