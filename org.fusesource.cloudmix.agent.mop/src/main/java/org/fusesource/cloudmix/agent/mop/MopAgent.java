/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent.mop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.mop.common.collect.ImmutableMap;
import org.fusesource.mop.common.collect.Maps;

import java.util.Map;

/**
 * @version $Revision: 1.1 $
 */
public class MopAgent extends InstallerAgent {
    private static final transient Log LOG = LogFactory.getLog(MopAgent.class);

    private Map<String, MopProcess> processes = Maps.newHashMap();
    public static final String MOP_URI_PREFIX = "mop:";

    /**
     * Returns the currently active processes this agent is running
     */
    public ImmutableMap<String, MopProcess> getProcesses() {
        synchronized (processes) {
            return ImmutableMap.copyOf(processes);
        }
    }

    @Override
    protected void installFeatures(ProvisioningAction action, String credentials, String resource) throws Exception {
        System.out.println("Installing FEATURES: " + resource);

        if (resource.startsWith(MOP_URI_PREFIX)) {
            String commandLine = resource.substring(MOP_URI_PREFIX.length());
            MopProcess process = new MopProcess(action, credentials, commandLine);

            String id = process.getId();
            synchronized (processes) {
                MopProcess oldProcess = processes.get(id);
                if (oldProcess != null) {
                    oldProcess.stop();
                }
                processes.put(id, process);
            }

            process.start();


            // now lets update the current features

            Feature feature = new Feature(id);
            addAgentFeature(feature);

            // TODO also need to update the current features!
            
        }
    }


    @Override
    protected void uninstallFeature(Feature feature) throws Exception {
        System.out.println("Uninstalling FEATURE: " + feature);

        String id = feature.getName();
        synchronized (processes) {
            MopProcess oldProcess = processes.get(id);
            if (oldProcess != null) {
                oldProcess.stop();
            }
        }

        super.uninstallFeature(feature);
        removeFeatureId(id);
    }
}
