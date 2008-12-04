package org.fusesource.cloudmix.common.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.fusesource.cloudmix.common.dto.Dependency;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;

public class ProfileControllerTest extends TestCase {
    public void testProfileController() {
        ProfileDetails details = new ProfileDetails();
        
        ProfileController pc = new ProfileController(null, details);
        assertSame(details, pc.getDetails());
    }
    
    public void testGetDeployableFeatures() {
        
        
        MockGridController cl = new MockGridController();
        ProfileController pc = new ProfileController(cl, new ProfileDetails("p1"));

        // basic feature: should be deployable...
        FeatureDetails fd = new FeatureDetails();
        fd.setMaximumInstances("1");
        fd.setId("f1");
        cl.addFeature(fd);
        Map<String, String> overrides = new HashMap<String, String>(2);
        overrides.put("prop11", "value11");
        overrides.put("prop12", "value12");
        pc.getDetails().addFeature("f1", overrides);
        
        // basic feature but that already reached its max install count: should NOT be deployable...
        fd = new FeatureDetails();
        fd.setMaximumInstances("1");
        fd.setId("f2");
        cl.addFeature(fd);
        cl.featureInstancesCount.put("f2", 12);
        overrides = new HashMap<String, String>(1);
        overrides.put("prop21", "value21");
        pc.getDetails().addFeature("f2", overrides);

        // basic feature but with installed dependencies: should be deployable...
        fd = new FeatureDetails();
        fd.setMaximumInstances("1");
        fd.setDependencies(Arrays.asList(new Dependency("f3Dep1"),
                                         new Dependency("f3Dep2")));
        fd.setId("f3");
        cl.addFeature(fd);
        cl.addFeature(new FeatureDetails("f3Dep1"));
        cl.addFeature(new FeatureDetails("f3Dep2"));
        cl.featureInstancesCount.put("f3Dep1", 1);
        cl.featureInstancesCount.put("f3Dep2", 1);
        pc.getDetails().addFeature("f3", new HashMap<String, String>(0));

        // basic feature but with un-installed dependencies: should NOT be deployable...
        fd = new FeatureDetails();
        fd.setMaximumInstances("1");
        fd.setDependencies(Arrays.asList(new Dependency("f4Dep1"),
                                         new Dependency("f4Dep2")));
        fd.setId("f4");
        cl.addFeature(fd);
        cl.addFeature(new FeatureDetails("f4Dep1"));
        cl.addFeature(new FeatureDetails("f4Dep2"));
        pc.getDetails().addFeature("f4", new HashMap<String, String>(0));
        
        
        List<FeatureController> deployable = pc.getDeployableFeatures();
        assertTrue(deployable.contains(cl.getFeatureController("f1")));
        assertFalse(deployable.contains(cl.getFeatureController("f2")));
        assertTrue(deployable.contains(cl.getFeatureController("f3")));
        assertFalse(deployable.contains(cl.getFeatureController("f4")));
        
    }
    
    public void testCompare() {
        
        // compare to nothing
        ProfileController nextOne = createMockProfile();
        assertFalse(nextOne.compare(null));
        assertTrue(nextOne.hasChanged());
        assertTrue(nextOne.getDetails().getFeatures().get(0).hasChanged());
        assertTrue(nextOne.getDetails().getFeatures().get(1).hasChanged());
        assertTrue(nextOne.getDetails().getFeatures().get(2).hasChanged());
        
        // compare same
        ProfileController original = createMockProfile();
        nextOne = createMockProfile();
        assertTrue(nextOne.compare(original));
        assertFalse(nextOne.hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(0).hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(1).hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(2).hasChanged());
        
        // compare with feature added
        original = createMockProfile();
        original.getDetails().getFeatures().remove(1);
        nextOne = createMockProfile();
        assertFalse(nextOne.compare(original));
        assertTrue(nextOne.hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(0).hasChanged());
        assertTrue(nextOne.getDetails().getFeatures().get(1).hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(2).hasChanged());

        // compare with feature removed
        original = createMockProfile();
        nextOne = createMockProfile();
        nextOne.getDetails().getFeatures().remove(1);
        assertFalse(nextOne.compare(original));
        assertTrue(nextOne.hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(0).hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(1).hasChanged());
        
        // compare with cfg override added
        original = createMockProfile();
        original.getDetails().getFeatures().get(2).getCfgUpdates().remove(0);
        nextOne = createMockProfile();
        assertFalse(nextOne.compare(original));
        assertTrue(nextOne.hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(0).hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(1).hasChanged());
        assertTrue(nextOne.getDetails().getFeatures().get(2).hasChanged());

        // compare with cfg override removed
        original = createMockProfile();
        nextOne = createMockProfile();
        nextOne.getDetails().getFeatures().get(2).getCfgUpdates().remove(0);
        assertFalse(nextOne.compare(original));
        assertTrue(nextOne.hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(0).hasChanged());
        assertFalse(nextOne.getDetails().getFeatures().get(1).hasChanged());
        assertTrue(nextOne.getDetails().getFeatures().get(2).hasChanged());

        // compare with cfg override changed
        original = createMockProfile();
        nextOne = createMockProfile();
        nextOne.getDetails().getFeatures().get(2).getCfgUpdates().get(0).setValue("another value");
        assertFalse(nextOne.compare(original));
        assertTrue(nextOne.hasChanged());
        assertTrue(nextOne.getDetails().getFeatures().get(2).hasChanged());

    }

    private ProfileController createMockProfile() {
        MockGridController cl = new MockGridController();
        ProfileController pc = new ProfileController(cl, new ProfileDetails("p1"));

        FeatureDetails fd = new FeatureDetails();
        fd.setMaximumInstances("1");
        fd.setId("f1");
        cl.addFeature(fd);
        Map<String, String> overrides = new HashMap<String, String>(2);
        overrides.put("prop11", "value11");
        overrides.put("prop12", "value12");
        pc.getDetails().addFeature("f1", overrides);
        
        fd = new FeatureDetails();
        fd.setMaximumInstances("1");
        fd.setId("f2");
        cl.addFeature(fd);
        pc.getDetails().addFeature("f2", null);
        
        fd = new FeatureDetails();
        fd.setMaximumInstances("1");
        fd.setId("f3");
        cl.addFeature(fd);
        overrides = new HashMap<String, String>(2);
        overrides.put("prop31", "value31");
        pc.getDetails().addFeature("f3", overrides);

        return pc;
    }


}
