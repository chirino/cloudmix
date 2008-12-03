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
