/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

/**
 * Represents an arbitrary resource used for example when asking for what files are in a directory
 * or what URIs are available within the web application
 *
 * @version $Revision: 1.1 $
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Resource {
    @XmlAttribute
    private String href;
    @XmlElement
    private String name;

    public Resource() {
    }

    public Resource(String href, String name) {
        this.href = href;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Resource[href: " + href
                + " name: " + name
                + "]";
    }

    // Properties
    //-------------------------------------------------------------------------

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}