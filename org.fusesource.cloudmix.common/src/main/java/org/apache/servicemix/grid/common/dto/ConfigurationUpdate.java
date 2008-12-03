/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
