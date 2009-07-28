/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.testrunner.rmi;

import java.io.File;
import java.util.Map;

/**
 * @version $Revision: 1.1 $
*/
public class ProcessMonitor implements Runnable {
    Thread thread;
    private String tempDirectory;
    private boolean cleanupRequested = false;
    private ProcessLauncher processLauncher;

    public ProcessMonitor(ProcessLauncher processLauncher) {
        this.processLauncher = processLauncher;
        tempDirectory = processLauncher.getDataDirectory() + File.separator + processLauncher.getAgentId() + File.separator + "temp";
        thread = new Thread(this, processLauncher.getAgentId() + "-Process Monitor");
        thread.start();
    }

    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    wait(ProcessLauncher.CLEANUP_TIMEOUT);
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

    public void shutdown() {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void cleanUpTempFiles() {
        //If we aren't running anything cleanup: temp parts
        Map<Integer,RemoteProcess> processes = processLauncher.getProcesses();
        if (processes == null || processes.size() == 0) {
            File tempDir = new File(tempDirectory);
            String[] subDirs = tempDir != null ? tempDir.list() : null;

            System.out.println("*************Cleaning up temporary parts*************");
            for (int i = 0; subDirs != null && i < subDirs.length; i++) {
                try {
                    ProcessLauncher.recursiveDelete(tempDir + File.separator + subDirs[i]);
                } catch (Exception e) {
                    System.out.println("ERROR cleaning up temporary parts:");
                    e.printStackTrace();
                }
            }
        }
    }

    public void checkForRogueProcesses(long timeout) {
        for (RemoteProcess remoteProcess : processLauncher.getProcesses().values()) {
            remoteProcess.ping(timeout);
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
}
