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
    
    public void addProfiles(ProfileDetails... profiles) throws URISyntaxException {
        for (ProfileDetails profile : profiles) {
            getClient().addProfile(profile);
        }
    }
}
