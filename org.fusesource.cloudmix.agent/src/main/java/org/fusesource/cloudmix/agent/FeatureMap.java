/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureMap {

    private Map<String, List<Bundle>> featureMap = new HashMap<String, List<Bundle>>();

    public FeatureMap() {
        // Complete
    }

    public synchronized void addBundle(String featureName, Bundle bundle) {
        List<Bundle> bundles = featureMap.get(featureName);
        if (bundles == null) {
            bundles = new ArrayList<Bundle>();
            bundles.add(bundle);
            featureMap.put(featureName, bundles);
        } else {
            if (!bundles.contains(bundle)) {
                bundles.add(bundle);
            }
        }
    }

    public void removeBundle(String featureName, Bundle bundle) {
        List<Bundle> bundles = featureMap.get(featureName);
        if (bundles == null) {
            return;
        }
        bundles.remove(bundle);
        if (bundles.isEmpty()) {
            featureMap.remove(featureName);
        }
    }

    public synchronized List<Bundle> getBundles(String featureName) {
        List<Bundle> bundles = featureMap.get(featureName);
        if (bundles == null) {
            bundles = new ArrayList<Bundle>();
        }
        return bundles;
    }

    public synchronized String[] getFeatures() {
        Set<String> f = featureMap.keySet();
        return f.toArray(new String[f.size()]);
    }
}
