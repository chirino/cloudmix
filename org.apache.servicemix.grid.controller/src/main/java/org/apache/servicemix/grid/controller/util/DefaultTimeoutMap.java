/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.grid.controller.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 630591 $
 */
public class DefaultTimeoutMap<K, V> implements TimeoutMap<K, V>, Runnable {
    private static final Log LOG = LogFactory.getLog(DefaultTimeoutMap.class);

    private Map<K, TimeoutMapEntry<K, V>> map = new HashMap<K, TimeoutMapEntry<K, V>>();
    private SortedSet<TimeoutMapEntry<K, V>> index = new TreeSet<TimeoutMapEntry<K, V>>();
    private ScheduledExecutorService executor;
    private long purgePollTime;
    private TimeoutListener<K, V> timeoutListener;

    public DefaultTimeoutMap() {
        this(null, 1000L);
    }

    public DefaultTimeoutMap(ScheduledExecutorService executor, long requestMapPollTimeMillis) {
        this.executor = executor;
        this.purgePollTime = requestMapPollTimeMillis;
        schedulePoll();
    }

    public V get(K key) {
        TimeoutMapEntry<K, V> entry = null;
        synchronized (map) {
            entry =  map.get(key);
            if (entry == null) {
                return null;
            }
            index.remove(entry);
            updateExpireTime(entry);
            index.add(entry);
        }
        return entry.getValue();
    }

    public void put(K key, V value, long timeoutMillis) {
        TimeoutMapEntry<K, V> entry = new TimeoutMapEntry<K, V>(key, value, timeoutMillis);
        synchronized (map) {
            Object oldValue = map.put(key, entry);
            if (oldValue != null) {
                index.remove(oldValue);
            }
            updateExpireTime(entry);
            index.add(entry);
        }
    }

    public void remove(K id) {
        synchronized (map) {
            TimeoutMapEntry<K, V> entry = map.remove(id);
            if (entry != null) {
                index.remove(entry);
            }
        }
    }

    public Collection<V> values() {
        List<V> answer = new ArrayList<V>(map.size());
        synchronized (map) {
            Collection<TimeoutMapEntry<K, V>> values = map.values();
            for (TimeoutMapEntry<K, V> value : values) {
                answer.add(value.getValue());
            }
        }
        return answer;
    }

    /**
     * Returns a copy of the keys in the map
     */
    public Object[] getKeys() {
        Object[] keys = null;
        synchronized (map) {
            Set<K> keySet = map.keySet();
            keys = keySet.toArray();
        }
        return keys;
    }

    /**
     * The timer task which purges old requests and schedules another poll
     */
    public void run() {
        purge();
        schedulePoll();
    }

    /**
     * Purges any old entries from the map
     */
    public void purge() {
        long now = currentTime();
        synchronized (map) {
            for (Iterator<TimeoutMapEntry<K, V>> iter = index.iterator(); iter.hasNext();) {
                TimeoutMapEntry<K, V> entry = iter.next();
                if (entry == null) {
                    break;
                }
                if (entry.getExpireTime() < now) {
                    if (isValidForEviction(entry)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Evicting inactive TimeoutMap entry for key: " + entry.getKey());
                        }
                        map.remove(entry.getKey());
                        onTimeout(entry.getKey(), entry.getValue());
                        iter.remove();
                    }
                } else {
                    break;
                }
            }
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public long getPurgePollTime() {
        return purgePollTime;
    }

    /**
     * Sets the next purge poll time in milliseconds
     */
    public void setPurgePollTime(long purgePollTime) {
        this.purgePollTime = purgePollTime;
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    /**
     * Sets the executor used to schedule purge events of inactive requests
     */
    public void setExecutor(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    public TimeoutListener<K, V> getTimeoutListener() {
        return timeoutListener;
    }

    public void setTimeoutListener(TimeoutListener<K, V> timeoutListener) {
        this.timeoutListener = timeoutListener;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    /**
     * lets schedule each time to allow folks to change the time at runtime
     */
    protected void schedulePoll() {
        if (executor != null) {
            executor.schedule(this, purgePollTime, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * A hook to allow derivations to avoid evicting the current entry
     *
     * @param entry
     * @return
     */
    protected boolean isValidForEviction(TimeoutMapEntry<K, V> entry) {
        return true;
    }

    protected void updateExpireTime(TimeoutMapEntry<K, V> entry) {
        long now = currentTime();
        entry.setExpireTime(entry.getTimeout() + now);
    }

    protected long currentTime() {
        return System.currentTimeMillis();
    }

    protected void onTimeout(K key, V value) {
        if (timeoutListener != null) {
            timeoutListener.onTimeout(key, value);
        }
    }
}
