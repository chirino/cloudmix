/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.controller.util;

/**
 * @version $Revision: 1.1 $
 */
public interface TimeoutListener<K, V> {
    void onTimeout(K key, V value);
}
