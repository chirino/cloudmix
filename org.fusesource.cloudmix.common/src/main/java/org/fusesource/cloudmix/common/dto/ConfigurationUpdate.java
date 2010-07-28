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
 * Used to represent a configuration update sent by the provisioning controller.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigurationUpdate extends IdentifiedType {    

    @XmlAttribute
    private String property;
    @XmlAttribute
    private String value;

    public ConfigurationUpdate() {
    }

    public ConfigurationUpdate(String property, String value) {
        this.property = property;
        this.value = value;
    }

    @Override
    public String toString() {
        return "ConfiguringUpdate[" + property + " " + value + "]";
    }

    // Properties
    //-------------------------------------------------------------------------
    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
