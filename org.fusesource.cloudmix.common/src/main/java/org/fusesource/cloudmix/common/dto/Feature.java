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

/**
 * @version $Revision$
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Feature extends IdentifiedType {
    @XmlAttribute
    private String url;

    public Feature() {
    }

    @Override
    public String toString() {
        return "Feature[id: " + getId() + " url: " + getUrl() + "]";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}