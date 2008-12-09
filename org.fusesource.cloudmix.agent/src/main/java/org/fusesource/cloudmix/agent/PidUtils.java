/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.lang.management.ManagementFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PidUtils {
    private static final String [] UNIX_COMMAND = {"sh", "-c", "echo $PPID"};
    private static final int PID = getPidInternal();
    
    private PidUtils() { /* static utility class */ }
    
    public static int getPid() {
        return PID;
    }
    
    private static int getPidInternal() {
        int id = getMXBeanPid();
        if (id != -1) {
            return id;
        }
        
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            // if we're on Windows, we out of luck now
            return -1; 
        }
        
        // on other platforms, try this
        return getPidFromShell();
    }

    private static int getMXBeanPid() {
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName().trim();
            Pattern pattern = Pattern.compile("^(\\d*).*");
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                name = matcher.group(1);
                return Integer.parseInt(name);                
            } else {
                return -1;
            }
        } catch (Throwable th) {
            return -1;
        }
    }
    
    private static int getPidFromShell() {
        try {
            Process p = Runtime.getRuntime().exec(UNIX_COMMAND);
            byte [] bytes = new byte[100];
            p.getInputStream().read(bytes);
            
            int value = Integer.parseInt(new String(bytes));
            if (value == 0) {
                value = -1;
            }
            return value;
        } catch (Throwable th) {
            return -1;
        }
    }
}
