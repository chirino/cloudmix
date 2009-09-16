/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.util;


/**
 * A number of useful helper methods for working with Objects
 *
 * @version $Revision: 630591 $
 */
public final class ObjectHelper {
    
    //private static final transient Log LOG = LogFactory.getLog(ObjectHelper.class);

    private ObjectHelper() { /* Utility classes should not have a public constructor */ }
    
    /**
     * @deprecated use the equal method instead
     *
     * @see #equal(Object, Object)
     */
    public static boolean equals(Object a, Object b) {
        return equal(a, b);
    }

    /**
     * A helper method for comparing objects for equality while handling nulls
     */
    public static boolean equal(Object a, Object b) {
        if (a == b) {
            return true;
        }
        return a != null && b != null && a.equals(b);
    }

    /**
     * Returns true if the given object is equal to any of the expected value
     *
     * @param object
     * @param values
     * @return
     */
    public static boolean isEqualToAny(Object object, Object... values) {
        for (Object value : values) {
            if (equal(object, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A helper method for performing an ordered comparsion on the objects
     * handling nulls and objects which do not handle sorting gracefully
     */
    @SuppressWarnings("unchecked")
    public static int compare(Object a, Object b) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        if (a instanceof Comparable) {
            Comparable comparable = (Comparable)a;
            return comparable.compareTo(b);
        } else {
            int answer = a.getClass().getName().compareTo(b.getClass().getName());
            if (answer == 0) {
                answer = a.hashCode() - b.hashCode();
            }
            return answer;
        }
    }

    public static void notNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must be specified");
        }
    }

    public static boolean isNotNullAndNonEmpty(String text) {
        return text != null && text.trim().length() > 0;
    }

    public static boolean isNullOrBlank(String text) {
        return text == null || text.trim().length() <= 0;
    }

}
