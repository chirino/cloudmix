/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @version $Revision: 1.1 $
 */
public class CompositeCallable<T> implements Callable<List<T>> {
    private final List<Callable<T>> callables;

    public CompositeCallable(List<Callable<T>> callables) {
        this.callables = callables;
    }

    public List<T> call() throws Exception {
        List<T> answer = new ArrayList<T>();
        for (Callable<T> callable : callables) {
            T result = callable.call();
            answer.add(result);
        }
        return answer;
    }
}
