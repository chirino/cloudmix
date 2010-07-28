/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.security;

/**
 * Provides a way of obtaining a password from different sources.
 */
public interface PasswordProvider {

    /**
     * Get a password as a char array. The array should be cleared after use.
     * 
     * @return the password or null if the password could not be obtained.
     */
    char[] getPassword();
}
