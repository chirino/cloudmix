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
 * @version $Revision: 1.1 $
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FeatureDetailsList {
    @XmlElement(name = "features")
    private List<FeatureDetails> features;


    public FeatureDetailsList() {
        this.features = new ArrayList<FeatureDetails>();

    }

    public FeatureDetailsList(Collection<FeatureDetails> features) {
        this.features = new ArrayList<FeatureDetails>(features);
    }

    @Override
    public String toString() {
        return "FeatureDetails" + features;
    }

    // Properties
    //-------------------------------------------------------------------------

    public List<FeatureDetails> getFeatures() {
        return features;
    }

    public void setFeatures(List<FeatureDetails> features) {
        this.features = features;
    }
}