/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.common.util;

/**
 * @version $Revision: 1.1 $
 */
public class Strings {

    /**
     * Concatenates the iterables together into a string using the given prefix, separator and postfix
     */
    public static String mkString(Iterable iterable, String prefix, String separator, String postfix) {
        StringBuilder buffer = new StringBuilder(prefix);
        boolean first = true;
        for (Object value : iterable) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(value);
        }
        buffer.append(postfix);
        return buffer.toString();
    }

    /**
     * Concatenates the iterables together into a string using the given separator
     */
    public static String mkString(Iterable iterable, String separator) {
        return mkString(iterable, "", separator, "");
    }

    /**
     * Concatenates the iterables together into a string using "," as the separator
     */
    public static String mkString(Iterable iterable) {
        return mkString(iterable, ",");
    }

    public static String asString(Object value) {
        return asString(value, null);
    }

    public static String asString(Object value, String defaultIfNull) {
        return value == null ? defaultIfNull : value.toString();
    }
}
