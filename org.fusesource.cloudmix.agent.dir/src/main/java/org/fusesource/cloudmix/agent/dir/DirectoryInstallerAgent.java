/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.dir;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.agent.AgentPoller;
import org.fusesource.cloudmix.agent.Bundle;
import org.fusesource.cloudmix.agent.Feature;
import org.fusesource.cloudmix.agent.InstallerAgent;
import org.fusesource.cloudmix.agent.RestGridClient;
import org.fusesource.cloudmix.agent.security.SecurityUtils;
import org.fusesource.cloudmix.common.util.FileUtils;
import org.fusesource.cloudmix.common.dto.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DirectoryInstallerAgent extends InstallerAgent {

    private static final Log LOGGER = LogFactory.getLog(DirectoryInstallerAgent.class);

    private static final String BUNDLE_FILE_KEY = DirectoryInstallerAgent.class.getName() + ".file";

    private String installDir;
    private String tmpSuffix;

    public static void main(String[] args) {
        try {
            String controllerUrl = "http://localhost:8181/";
            String directory = "target/provisioning";
            String profile = Constants.WILDCARD_PROFILE_NAME;

            if (args.length > 0) {
                String arg0 = args[0];
                if (arg0.startsWith("?") || arg0.startsWith("-")) {
                    System.out.println("Usage: DirectoryInstallerAgent [controllerURL] [provisionDirectory]");
                    return;
                } else {
                    controllerUrl = arg0;
                }
                if (args.length > 1) {
                    directory = args[1];
                }
            }
            LOGGER.info("Connecting to Cloudmix controller at: " + controllerUrl);

            File rootDirectory = new File(directory);
            File workDirectory = new File(rootDirectory, "work");
            File installDirectory = new File(rootDirectory, "install");
            
            workDirectory.mkdirs();
            installDirectory.mkdirs();
            LOGGER.info("Using rootDirectory: " + rootDirectory);

            DirectoryInstallerAgent agent = new DirectoryInstallerAgent();
            agent.setClient(new RestGridClient(controllerUrl));
            agent.setInstallDirectory(installDirectory.toString());
            agent.setWorkDirectory(workDirectory);
            agent.setProfile(profile);
            agent.init();

            AgentPoller poller = new AgentPoller(agent);
            poller.start();
        } catch (Exception e) {
            LOGGER.error("Caught: " + e, e);
        }
    }

    public DirectoryInstallerAgent() {
        // Complete.
    }

    public void setInstallDirectory(String path) {
        LOGGER.info("install directory: " + path);
        installDir = path;
    }

    public String getInstallDirectory() {
        return installDir;
    }

    public void setTempSuffix(String s) {
        this.tmpSuffix = s;
    }

    public String getTempSuffix() {
        return tmpSuffix;
    }

    /**
     * should be careful when using this: do not use hard coded value as the ID should be somewhat guaranteed
     * to be unique. Can be used when reloading the agent settings after a restart
     *
     * @param anId
     */
    public void setAgentId(String anId) {
        agentId = anId;
    }

    @Override
    public boolean validateAgent() {
        if (installDir == null) {
            LOGGER.warn("No install directory set; ignoring provisioning change");
            return false;
        }
        return true;
    }

    @Override
    protected boolean installBundle(Feature feature, Bundle bundle) {

        LOGGER.info("Installing bundle " + bundle + " for feature " + feature);

        String filename = getFilename(bundle);
        if (filename == null) {
            LOGGER.warn("Cannot get filename for bundle " + bundle);
            return false;
        }
        String tmpFilename = getTempFilename(filename);

        File tmpPath = new File(installDir, tmpFilename);
        LOGGER.debug("Tmp Path: " + tmpPath);

        File path = new File(installDir, filename);

        FileOutputStream os = null;
        InputStream is = null;
        try {
            String uri = bundle.getUri();
            if (uri == null || "".equals(uri)) {
                throw new RuntimeException("Feature "
                        + feature
                        + " contains a bundle with null or empty URI");
            }
            URL url = new URL(uri);

            os = new FileOutputStream(tmpPath);
            if (os == null) {
                throw new RuntimeException("Cannot write to " + tmpPath);
            }

            is = SecurityUtils.getInputStream(url);
            if (is == null) {
                os.close();
                throw new RuntimeException("Cannot read from URL " + url);
            }

            FileUtils.copy(is, os);

            is.close();
            is = null;

            os.close();
            os = null;

            if (!filename.equals(tmpFilename)) {
                LOGGER.info("Renaming " + tmpPath + " to " + path);
                if (!tmpPath.renameTo(path)) {
                    LOGGER.error("failed to rename " + tmpPath + " to " + path);
                    tmpPath.delete();
                    return false;
                }
            }
            bundle.getAgentProperties().put(BUNDLE_FILE_KEY, path);
            return true;

        } catch (Exception e) {

            LOGGER.error("Failed to install bundle " + bundle + " for feature "
                    + feature + ", exception " + e);

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e1) {
                    // Complete
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e1) {
                    // Complete.
                }
            }

            try {
                if (!tmpPath.delete()) {
                    LOGGER.warn("Cannot delete file " + tmpPath);
                }
            } catch (Throwable t) {
                // Complete.
            }
        }
        return false;
    }

    @Override
    protected boolean uninstallBundle(Feature feature, Bundle bundle) {

        LOGGER.info("Uninstalling bundle " + bundle + " for feature " + feature);

        Object o = bundle.getAgentProperties().get(BUNDLE_FILE_KEY);
        if (o instanceof File) {
            File path = (File) o;
            LOGGER.debug("Path: " + path);

            if (!path.delete()) {
                LOGGER.warn("Cannot delete resource " + path);
            }

            bundle.getAgentProperties().remove(BUNDLE_FILE_KEY);
        }

        LOGGER.warn("cannot find installed file for bundle " + bundle);
        return true;
    }

    private String getFilename(Bundle bundle) {
        String name = bundle.getName();
        if (name != null && !"".equals(name)) {
            return name;
        }

        try {
            String path = new URL(bundle.getUri()).getPath();
            int i = path.lastIndexOf('/');
            if (i == -1) {
                return path;
            }
            return path.substring(i);
        } catch (Throwable t) {
            LOGGER.warn("Error getting path from URI " + bundle.getUri());
            return null;
        }
    }

    private String getTempFilename(String filename) {
        if (tmpSuffix == null || "".equals(tmpSuffix)) {
            return filename;
        }

        return filename + tmpSuffix;
    }
}
