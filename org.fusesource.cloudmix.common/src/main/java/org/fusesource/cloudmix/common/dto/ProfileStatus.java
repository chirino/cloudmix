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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileStatus extends IdentifiedType {
    @XmlElement(name = "features")
    private List<DependencyStatus> features = new ArrayList<DependencyStatus>();

    public ProfileStatus() {
    }

    public ProfileStatus(String id) {
        super(id);
    }

    @Override
    public String toString() {
        return "Profile[id: " + getId() + " features: " + features + "]";
    }


    // Properties
    //-------------------------------------------------------------------------

    public List<DependencyStatus> getFeatures() {
        return features;
    }

    public void setFeatures(List<DependencyStatus> features) {
        this.features = features;
    }
}