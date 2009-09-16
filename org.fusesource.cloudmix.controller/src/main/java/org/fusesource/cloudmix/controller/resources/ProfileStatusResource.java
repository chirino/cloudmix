/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.resources;

import javax.ws.rs.GET;
import org.fusesource.cloudmix.common.dto.ProfileStatus;


/**
 * @version $Revision: 1.1 $
 */
public class ProfileStatusResource extends ResourceSupport {
    private ProfileResource profileResource;

    public ProfileStatusResource(ProfileResource profileResource) {
        this.profileResource = profileResource;
    }

    @GET
    public ProfileStatus getStatus() {
        return profileResource.getController().getProfileStatus(profileResource.getProfileId());
    }

}
