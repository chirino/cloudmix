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
