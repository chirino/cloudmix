/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.util;

import java.util.Collection;

/**
 * Represents a thread safe map of values which timeout after a period of
 * inactivity.
 *
 * @version $Revision: 630591 $
 */
public interface TimeoutMap<K, V> extends Runnable {
    /**
     * Looks up the value in the map by the given key.
     *
     * @param key the key of the value to search for
     * @return the value for the given key or null if it is not present (or has
     *         timed out)
     */
    V get(K key);

    Collection<V> values();

    /**
     * Returns a copy of the keys in the map
     */
    Object[] getKeys();

    /**
     * Adds the key value pair into the map such that some time after the given
     * timeout the entry will be evicted
     */
    void put(K key, V value, long timeoutMillis);

    void remove(K key);

    /**
     * Purges any old entries from the map
     */
    void purge();

    TimeoutListener<K, V> getTimeoutListener();

    void setTimeoutListener(TimeoutListener<K, V> timeoutListener);
}
