/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

public class FeatureDetailsTest extends TestCase {
    public void testDigest() {
        FeatureDetails fd1 = new FeatureDetails();
        FeatureDetails fd2 = new FeatureDetails();
        
        assertEquals(fd1.getDigest(), fd2.getDigest());
        fd1.setMinimumInstances("5");
        assertFalse(fd1.getDigest() == fd2.getDigest());
        fd2.setMinimumInstances("5");
        assertEquals(fd1.getDigest(), fd2.getDigest());
        
        fd1.setMaximumInstances("20");
        assertFalse(fd1.getDigest() == fd2.getDigest());
        fd2.setMaximumInstances("20");
        assertEquals(fd1.getDigest(), fd2.getDigest());

        fd1.setOwnsMachine(true);
        assertFalse(fd1.getDigest() == fd2.getDigest());
        fd2.setOwnsMachine(true);
        assertEquals(fd1.getDigest(), fd2.getDigest());

        fd1.setPreferredMachines(new HashSet<String>(Arrays.asList("a", "b")));
        fd2.setPreferredMachines(new HashSet<String>(Arrays.asList("c", "a")));
        assertFalse(fd1.getDigest() == fd2.getDigest());
        fd2.setPreferredMachines(new HashSet<String>(Arrays.asList("a", "b")));
        assertEquals(fd1.getDigest(), fd2.getDigest());

        fd1.setDependencies(Arrays.asList(new Dependency("aa"), new Dependency("bb")));
        fd2.setDependencies(Arrays.asList(new Dependency[] {}));
        assertFalse(fd1.getDigest() == fd2.getDigest());
        fd2.setDependencies(Arrays.asList(new Dependency("aa"), new Dependency("bb")));
        assertEquals(fd1.getDigest(), fd2.getDigest());
        
        fd1.setValidContainerTypes(Arrays.asList("aa", "bb"));
        fd2.setValidContainerTypes(Arrays.asList(new String[] {}));
        assertFalse(fd1.getDigest() == fd2.getDigest());
        fd2.setValidContainerTypes(Arrays.asList("aa", "bb"));
        assertEquals(fd1.getDigest(), fd2.getDigest());
        

    }
}
