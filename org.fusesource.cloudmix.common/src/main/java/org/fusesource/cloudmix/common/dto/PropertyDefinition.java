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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Defines a dynamic property which can then be used to configure features using dynamic environment variables
 *
 * @version $Revision$
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PropertyDefinition {
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    private String id;

    @XmlValue
    private String expression;

    public PropertyDefinition() {
    }

    public PropertyDefinition(String id) {
        this.id = id;
    }

    public PropertyDefinition(String id, String expression) {
        this(id);
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "PropertyDefinition[id: " + getId()
                + " expression: " + getExpression()
                + "]";
    }

    // Properties
    //-------------------------------------------------------------------------


    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    public String getExpression() {
        return expression;
    }

    /**
     * Sets the Scala expression used to create the property value String from the dynamic CloudMix model
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }
}