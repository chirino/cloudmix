/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Revision$
 */
@XmlRootElement(name = "resources")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResourceList {
    @XmlElement(name = "resource")
    private List<Resource> resources;

    public ResourceList() {
        this.resources = new ArrayList<Resource>();
    }

    public ResourceList(Collection<Resource> resources) {
        this.resources = new ArrayList<Resource>(resources);
    }

    public static ResourceList newInstance(Iterable<Resource> resources) {
        List<Resource> list = new ArrayList<Resource>();
        for (Resource resource : resources) {
            list.add(resource);
        }
        return new ResourceList(list);
    }

/*
    public static ResourceList newInstance(Resource... resources) {
        List<Resource> list = new ArrayList<Resource>();
        for (Resource resource : resources) {
           list.add(resource);
        }
        return new ResourceList(list);
    }
*/



    @Override
    public String toString() {
        return "Resource" + resources;
    }

    public void addResource(String href, String name) {
        addResource(new Resource(href, name));
    }

    public void addResource(Resource resource) {
        resources.add(resource);
    }

    // Properties
    //-------------------------------------------------------------------------

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}