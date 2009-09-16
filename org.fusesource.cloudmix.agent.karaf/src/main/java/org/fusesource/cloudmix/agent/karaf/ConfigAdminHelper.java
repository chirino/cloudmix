/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.karaf;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility methods for updating ConfigAdmin .cfg filess
 */
public class ConfigAdminHelper {

    private ConfigAdminHelper() {
        // hide constructor
    }

    /**
     * Merge two Strings containing a comma-separated list of values
     * @param first the first string
     * @param last  the second string
     * @return a result string, containing a comma-separated list of the union of values in the original strings
     */
    public static String merge(String first, String last) {
        return merge(first.split(","), last.split(","));
    }

    private static String merge(String[] first, String[] last) {
        Set<String> merged = new TreeSet<String>();
        for (String element : first) {
            merged.add(element);
        }
        for (String element : last) {
            merged.add(element);
        }
        return explode(merged.toArray(new String[]{}), ",");
    }

    private static String explode(String[] elements, String separator) {
        StringBuffer result = new StringBuffer();
        for (int i = 0 ; i < elements.length ; i++) {
            result.append(elements[i]);
            if (i + 1 < elements.length) {
                result.append(separator);
            }
        }
        return result.toString();
    }

    /**
     * Merge a set of properties into an existing file, updating the file by adding the new property values
     *
     * @param file the existing properties file
     * @param properties the set of properties to be merged into the file
     * @throws IOException if a problem occurs while reading/writing the file
     */
    public static void merge(File file, Map<String, String> properties) throws IOException {
        if (properties.isEmpty()) {
            // don't need to do anything for an empty map
            return;
        }
        
        Properties result = new Properties();
        if (file.exists()) {
            // let's load the contents of the existing file
            result.load(new FileInputStream(file));
        }
        for (String key : properties.keySet()) {
            if (result.containsKey(key)) {
                result.put(key, merge(result.getProperty(key), properties.get(key)));
            } else {
                result.put(key, properties.get(key));
            }
        }

        //write the result back to file with a plain PrintWriter -- Properties.store() escapes the : with a \
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
            writer.printf("# Created by CloudMix on %s%n", new Date());
            for (Object key : result.keySet()) {
                writer.printf("%s=%s%n", key, result.get(key));
            }
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
