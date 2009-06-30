/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Revision: 1.1 $
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DependencyStatus {

    private static final transient Log LOG = LogFactory.getLog(DependencyStatus.class);

    @XmlAttribute
    private String featureId;

    @XmlAttribute
    private boolean provisioned;

    public DependencyStatus() {
    }

    public DependencyStatus(String id) {
        featureId = id;
    }

    public int getDigest() {
        if (featureId != null) {
            return featureId.hashCode();
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "DependencyStatus[" + getFeatureId() + " provisioned: " + provisioned + "]";
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String aFeatureId) {
        featureId = aFeatureId;
    }

    public boolean isProvisioned() {
        return provisioned;
    }

    public void setProvisioned(boolean provisioned) {
        this.provisioned = provisioned;
    }
}