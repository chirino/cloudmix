/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.security;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

public final class SecurityUtils {

    private SecurityUtils() {
        // Complete.
    }

    /**
     * Create HTTP Basic Authentication header given username and password.
     * 
     * @param username
     * @param password
     * @return String containing HTTP Basic Auth header suitable to be send via the HTTP Authorization header.
     */
    public static String toBasicAuth(String username, char[] password) {
        StringBuilder sb = new StringBuilder().append(username).append(":").append(String.valueOf(password));
        // Clear array.
        Arrays.fill(password, ' ');
        return getBasicAuthHeader(sb.toString());
    }

    public static String getBasicAuthHeader(String userInfo) {
        return "Basic " + new String(Base64.encodeBase64(userInfo.getBytes()));
    }

    /**
     * Get input stream for URL adding credentials if neccessary.
     * 
     * @param url
     * @return input stream for URL
     * @throws IOException
     */
    public static InputStream getInputStream(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        String userInfo = url.getUserInfo();
        if (userInfo != null && !"".equals(userInfo)) {
            conn.setRequestProperty("Authorization", getBasicAuthHeader(userInfo));
        }
        return conn.getInputStream();
    }

}
