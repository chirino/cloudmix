package org.apache.servicemix.grid.agent.dir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.grid.agent.InstallerAgent;
import org.apache.servicemix.grid.agent.Bundle;
import org.apache.servicemix.grid.agent.Feature;
import org.apache.servicemix.grid.common.util.FileUtils;

public class DirectoryInstallerAgent extends InstallerAgent {

    private static final Log LOGGER = LogFactory.getLog(DirectoryInstallerAgent.class);

    private static final String BUNDLE_FILE_KEY = "file";

    private String installDir;
    private String tmpSuffix;
        
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
     *  to be unique. Can be used when reloading the agent settings after a restart
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
            URL url = new URL(bundle.getUri());
            
            os = new FileOutputStream(tmpPath);
            if (os == null) {
                throw new RuntimeException("Cannot write to " + tmpPath);
            }
            
            is = url.openStream();
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

        LOGGER.warn ("cannot find installed file for bundle " + bundle);
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
