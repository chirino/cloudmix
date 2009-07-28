/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.testrunner.rmi;

import org.fusesource.testrunner.LocalProcessLauncher;
import org.fusesource.testrunner.LocalProcess;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @version $Revision: 1.1 $
*/
public class RemoteListenerMonitor implements Runnable {

    private final LocalProcessLauncher processLauncher;

    Thread thread;
    private String tempDirectory;
    private boolean cleanupRequested = false;

    public RemoteListenerMonitor(LocalProcessLauncher processLauncher) {
        this.processLauncher = processLauncher;
    }

    public void start() {
        tempDirectory = processLauncher.getDataDirectory() + File.separator + processLauncher.getAgentId() + File.separator + "temp";
        thread = new Thread(this, processLauncher.getAgentId() + "-Process Monitor");
        thread.start();
    }

    public void stop() {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    wait(LocalProcessLauncher.CLEANUP_TIMEOUT);
                } catch (InterruptedException ie) {
                    cleanupRequested = true;
                    return;
                } finally {
                    checkForRogueProcesses(15000);
                    if (cleanupRequested) {
                        cleanUpTempFiles();
                        cleanupRequested = false;
                    }
                }
            }

        }
    }


    public void cleanUpTempFiles() {
        //If we aren't running anything cleanup: temp parts
        Map<Integer, LocalProcess> processes = processLauncher.getProcesses();
        if (processes == null || processes.size() == 0) {
            File tempDir = new File(tempDirectory);
            String[] subDirs = tempDir != null ? tempDir.list() : null;

            System.out.println("*************Cleaning up temporary parts*************");
            for (int i = 0; subDirs != null && i < subDirs.length; i++) {
                try {
                    recursiveDelete(tempDir + File.separator + subDirs[i]);
                } catch (Exception e) {
                    System.out.println("ERROR cleaning up temporary parts:");
                    e.printStackTrace();
                }
            }
        }
    }

    public void checkForRogueProcesses(long timeout) {
        for (LocalProcess remoteProcess : processLauncher.getProcesses().values()) {
            ((RemotedProcess)remoteProcess).ping(timeout);
        }
    }

    /**
     * Requests cleanup of temporary files
     */
    public synchronized void requestCleanup() {
        cleanupRequested = true;
        notify();
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    private static void checkDirectoryDepth(String path, String message, int minDepth) throws Exception {
        int depth = 0;
        int index = -1;
        if (path.startsWith(File.separator + File.separator)) {
            depth -= 2;
        } else if (path.startsWith(File.separator)) {
            depth--;
        }

        while (true) {
            index = path.indexOf(File.separator, index + 1);
            if (index == -1) {
                break;
            } else {
                depth++;
            }
        }

        if (minDepth > depth)
            throw new Exception(message);
    }

    static void recursiveDelete(String srcDir) throws IOException, Exception {
        //String srcFileName = "";
        String[] fileList;

        //Just delete and return if a file is specified:
        File srcFile = new File(srcDir);

        //Check to make sure that we aren't deleting a root or first level directory:
        checkDirectoryDepth(srcFile.getAbsolutePath(), "Directory depth is too shallow to risk recursive delete for path: " + srcFile.getAbsolutePath()
                + " directory depth should be at least 2 levels deep.", 2);

        if (!srcFile.exists()) {
        } else if (srcFile.isFile()) {
            int retries = 0;
            while (!srcFile.delete()) {
                if (retries > 20) {
                    throw new IOException("ERROR: Unable to delete file: " + srcFile.getAbsolutePath());
                }
                retries++;
            }
        } else {
            fileList = srcFile.list();
            // Copy parts from cd to installation directory
            for (int j = 0; j < fileList.length; j++) {
                //Format file names
                recursiveDelete(srcDir + File.separator + fileList[j]);
            }
            //Finally once all leaves are deleted delete this node:
            int retries = 0;

            while (!srcFile.delete()) {
                if (retries > 20) {
                    throw new IOException("ERROR: Unable to delete directory. Not empty?");
                }
                retries++;
            }
        }
    }//private void recursiveDelete(String dir)

}
