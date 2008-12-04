/**************************************************************************************
 * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.controller.util;

import java.util.Map;

/**
 * Represents an entry in a {@link TimeoutMap}
 *
 * @version $Revision: 630591 $
 */
public class TimeoutMapEntry<K, V> implements Comparable, Map.Entry<K, V> {
    private K key;
    private V value;
    private long timeout;
    private long expireTime;

    public TimeoutMapEntry(K id, V value, long timeout) {
        this.key = id;
        this.value = value;
        this.timeout = timeout;
    }

    public K getKey() {
        return key;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V aValue) {
        V oldValue = aValue;
        this.value = aValue;
        return oldValue;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int compareTo(Object that) {
        if (this == that) {
            return 0;
        }
        if (that instanceof TimeoutMapEntry) {
            return compareTo((TimeoutMapEntry) that);
        }
        return 1;
    }

    public int compareTo(TimeoutMapEntry that) {
        long diff = this.expireTime - that.expireTime;
        if (diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        }
        return this.key.hashCode() - that.key.hashCode();
    }

    public String toString() {
        return "Entry for key: " + key;
    }
}