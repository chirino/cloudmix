/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
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
public class Process {
    @XmlElement(name = "feature")
    private List<Feature> features = new ArrayList<Feature>();

    public Process() {
    }

    @Override
    public String toString() {
        return "Process" + features;
    }


    /**
     * Populates the history based on this list of features
     */
    public void populateHistory(ProvisioningHistory history) {
        for (Feature feature : features) {
            history.install(feature.getId(), feature.getUrl());
        }
    }


    // Fluent API
    //-------------------------------------------------------------------------

    public Feature feature(String id, String url) {
        Feature answer = new Feature();
        answer.setId(id);
        answer.setUrl(url);
        features.add(answer);
        return answer;
    }

    // Properties
    //-------------------------------------------------------------------------

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}

