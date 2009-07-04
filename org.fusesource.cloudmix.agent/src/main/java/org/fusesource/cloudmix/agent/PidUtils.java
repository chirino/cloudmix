/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fusesource.cloudmix.agent.unix.Posix;
import org.fusesource.cloudmix.agent.win32.Kernel32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public final class PidUtils {
    private static final String [] UNIX_COMMAND = {"sh", "-c", "echo $PPID"};
    private static final int PID = getPidInternal();
    
    private PidUtils() { /* static utility class */ }
    
    public static int getPid() {
        return PID;
    }
    
    /**
     * @param pid
     * @return true if the provided pid is running.
     * @throws IOException
     */
    public static boolean isPidRunning(int pid) throws IOException {
        Kernel32 kernel32 = Kernel32.Factory.get();
        if( kernel32!=null ) {
        	Pointer process = kernel32.OpenProcess(Kernel32.PROCESS_QUERY_LIMITED_INFORMATION, 0, pid);
        	if( process!=null ) {
        		kernel32.CloseHandle(process);
        		return true;
        	}
        	return false;
        }
        
        Posix posix = Posix.Factory.get();
        if( posix!=null ) {
        	return posix.kill(pid, 0) == 0;
        }
        throw new UnsupportedOperationException("isPidRunning is not yet supported on this operating system.");
    }
    
    /**
     * Kills a running PID.
     *   If your on a Unix system, it kills it with the given signal, but if your on Windows,
     *   you terminate the process with the provided exitCode.
     *  
     * @param pid
     * @param signal
     * @param exitCode
     * @throws IOException
     */
    public static void killPid(int pid, int signal, int exitCode) throws IOException {
        Kernel32 kernel32 = Kernel32.Factory.get();
        if( kernel32!=null ) {
        	Pointer process = kernel32.OpenProcess(Kernel32.PROCESS_TERMINATE, 0, pid);
        	if( process==null ) {
        		throw new IOException("Could not open process pid "+pid+": "+Kernel32.Factory.getLastErrorAsString());
        	}
        	try {
        		if( kernel32.TerminateProcess(process, exitCode) == 0 ) {
            		throw new IOException("Could not terminate process pid "+pid+": "+Kernel32.Factory.getLastErrorAsString());
        		}
            	return;
        	} finally {
        		kernel32.CloseHandle(process);
        	}
        }
        
        Posix posix = Posix.Factory.get();
        if( posix!=null ) {
        	if( posix.kill(pid, signal) != 0 ) {
        		throw new IOException("Could not kill process pid "+pid+": "+posix.strerror(Native.getLastError()));
        	}
        	return;
        }
        
        throw new UnsupportedOperationException("killPid is not yet supported on this operating system.");
    }

    
    private static int getPidInternal() {
        
    	// Try to do a native system call call...
    	Kernel32 kernel32 = Kernel32.Factory.get();
        if( kernel32!=null ) {
        	return kernel32.GetCurrentProcessId();
        }
        
    	// Try to do a native system call call...
        Posix posix = Posix.Factory.get();
        if( posix!=null ) {
        	return posix.getpid();
        }
        
        // Fall back to some JVM hacks..
        int id = getMXBeanPid();
        if (id != -1) {
            return id;
        }

        // Fall back to using some shell hacks..
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
