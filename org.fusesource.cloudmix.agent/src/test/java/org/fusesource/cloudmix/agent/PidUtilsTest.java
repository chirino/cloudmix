/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

import com.sun.jna.Platform;

public class PidUtilsTest extends TestCase {
    public void testGetPid() {
        int pid = PidUtils.getPid();
        assertTrue("Pid Utils should return a meaningful pid", pid > 0);
        assertEquals("The pid should remain the same", pid, PidUtils.getPid());
    }

    public void testKillPid() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        // Launch the PidUtilsTestMain class as a child process.
        String cp = getClassPathOf(PidUtilsTestMain.class);
        final Process exec = Runtime.getRuntime().exec(new String[] {
            "java", "-cp", cp, PidUtilsTestMain.class.getName()
        });
        new Thread() {
            public void run() {
                try {
                    InputStream is = exec.getErrorStream();
                    int c;
                    while ((c = is.read()) >= 0) {
                        System.err.write(c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } .start();

        // Setup a thread to wait for the child's exit code.
        FutureTask<Integer> exitCode = new FutureTask<Integer>(new Callable<Integer>() {
            public Integer call() throws Exception {
                return exec.waitFor();
            }
        });
        new Thread(exitCode).start();
        try {
            exitCode.get(500, TimeUnit.MILLISECONDS);
            System.out
                .println("Cannot run the rest of the test.. could not execute the child process properly.");
            return;
        } catch (TimeoutException expected) {
            //ignore
        }

        // The child will let us know it's pid.
        BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        String line = reader.readLine();
        int childPid = Integer.parseInt(line);
        assertTrue(childPid > 0);

        assertTrue(PidUtils.isPidRunning(childPid));

        // Kill it..
        PidUtils.killPid(childPid, 9, 5);

        int rc = exitCode.get(3, TimeUnit.SECONDS);
        if (Platform.isWindows()) {
            // On windows we can actually say what the exit code should be.
            assert rc == 5;
        } else {
            assertTrue(rc != 0);
        }
        assertFalse(PidUtils.isPidRunning(childPid));

    }

    /**
     * Gives you back the classpath that you can use to load up the specified Class.
     * 
     * @param clazz
     * @return
     */
    private String getClassPathOf(Class<? extends Object> clazz) {
        List<String> path = new ArrayList<String>();
        buildClassPath(path, clazz.getClassLoader());
        StringBuilder rc = new StringBuilder();
        boolean first = true;
        for (String file : path) {
            if (!first) {
                rc.append(File.pathSeparator);
            }
            rc.append(file);
            first = false;
        }
        return rc.toString();
    }

    /**
     * Adds all the file paths in the provided ClassLoader into the provided ArrayList.
     * 
     * @param path
     * @param classLoader
     */
    private void buildClassPath(List<String> path, ClassLoader classLoader) {
        if (classLoader.getParent() != null) {
            buildClassPath(path, classLoader.getParent());
        }
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader ulc = (URLClassLoader)classLoader;
            URL[] urls = ulc.getURLs();
            if (urls == null) {
                return;
            }
            for (URL url : urls) {
                if ("file".equals(url.getProtocol())) {

                    File f;
                    try {
                        f = new File(url.toURI());
                    } catch (URISyntaxException e) {
                        f = new File(url.getPath());
                    }

                    path.add(f.getAbsolutePath());
                } else {
                    System.out.println("Unknown url: " + url);
                }
            }
        } else {
            System.out.println("Unknown cl: " + classLoader);
        }
    }
}
