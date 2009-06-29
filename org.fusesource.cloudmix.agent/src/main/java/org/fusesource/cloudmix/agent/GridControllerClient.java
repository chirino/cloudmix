/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.net.URISyntaxException;

import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.FeatureDetails;
import org.fusesource.cloudmix.common.dto.ProfileDetails;

/**
 * The client API for installing/starting/stopping/uninstalling features on the grid
 *
 * @version $Revision: 1.1 $
 */
public class GridControllerClient {
    private GridClient client;


    public GridClient getClient() {
        if (client == null) {
            client = new RestGridClient();
        }
        return client;
    }

    public void setClient(GridClient client) {
        this.client = client;
    }

    public void addFeatures(FeatureDetails... features) throws URISyntaxException {
        for (FeatureDetails feature : features) {
            getClient().addFeature(feature);
        }
    }
    
    public void addFeatures(Iterable<FeatureDetails> features) throws URISyntaxException {
        for (FeatureDetails feature : features) {
            getClient().addFeature(feature);
        }
    }

    public void addProfiles(ProfileDetails... profiles) throws URISyntaxException {
        for (ProfileDetails profile : profiles) {
            getClient().addProfile(profile);
        }
    }

    public void addProfiles(Iterable<ProfileDetails> profiles) throws URISyntaxException {
        for (ProfileDetails profile : profiles) {
            getClient().addProfile(profile);
        }
    }
}
