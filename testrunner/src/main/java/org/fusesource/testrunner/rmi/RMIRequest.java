/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.testrunner.rmi;

import java.io.IOException;
import java.util.Hashtable;

/** 
 * RMIRequest
 * <p>
 * Place holder for now. If we continue to use this it should be cleaned up.
 * </p>
 * @author cmacnaug
 * @version 1.0
 * @deprecated
 */
public class RMIRequest extends TRMetaMessage{

    private static final String ARGS = "ARGS";
    private static final String TARGET = "TARGET";
    
    public static final int AGENT = 0;
    public static final int CLIENT = 1;
    public static final int CLIENT_PROC_LISTENER = 2;
    
    /**
     */
    RMIRequest(int target, String method, Object [] args) {
        super(method, new Hashtable());
        props.put(ARGS, args);
        props.put(TARGET, new Integer(target));
        super.setInternal(true);
    }
    
    public int getTarget()
    {
        return getIntProperty(TARGET).intValue();
    }
    
    public String getMethod()
    {
        try {
            return getContent().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object [] getArgs()
    {
        return (Object []) props.get(ARGS);
    }
    
}
