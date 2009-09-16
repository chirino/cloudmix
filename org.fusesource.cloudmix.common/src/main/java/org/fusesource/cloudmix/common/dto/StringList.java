/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Revision$
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StringList {
    @XmlElement(name = "values")
    private List<String> values = new ArrayList<String>(0);

    public StringList() {        
    }
    
    public StringList(List<String> values) {
        this.values = values;
    }
    
    
    // Properties
    //-------------------------------------------------------------------------

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> processes) {
        this.values = processes;
    }

}
