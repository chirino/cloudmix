/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileDetailsList {
    @XmlElement(name = "profiles")
    List<ProfileDetails> profiles;

    public ProfileDetailsList() {
        profiles = new ArrayList<ProfileDetails>();
    }
    
    public ProfileDetailsList(Collection<ProfileDetails> profs) {
        profiles = new ArrayList<ProfileDetails>(profs);
    }

    @Override
    public String toString() {
        return "ProfileDetails " + profiles;
    }
    
    // Properties
    //-------------------------------------------------------------------------

    public List<ProfileDetails> getProfiles() {
        return profiles;
    }
    
    public void setProfiles(List<ProfileDetails> profs) {
        profiles = profs;
    }
}
