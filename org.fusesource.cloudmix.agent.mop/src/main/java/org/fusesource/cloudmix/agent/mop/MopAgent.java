/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent.mop;

import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.common.dto.ConfigurationUpdate;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * @version $Revision: 1.1 $
 */
public class MopAgent extends InstallerAgent {
    private static final transient Log LOG = LogFactory.getLog(MopAgent.class);

    @Override
    protected void installFeatures(ProvisioningAction action, String credentials, String resource) throws Exception {
        System.out.println("Installing FEATURES: " + resource);
        //super.installFeatures(action, credentials, resource);
    }

    @Override
    protected void installFeature(Feature feature, List<ConfigurationUpdate> featureCfgOverrides) {
        System.out.println("Installing FEATURE: " + feature);
        super.installFeature(feature, featureCfgOverrides);
    }

    @Override
    protected void uninstallFeature(Feature feature) {
        System.out.println("Uninstalling FEATURE: " + feature);
        super.uninstallFeature(feature);
    }
}
