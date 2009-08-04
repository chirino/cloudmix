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
package org.fusesource.testrunner;

import java.io.Serializable;
import java.util.Properties;

/** 
 * HostProperties
 * <p>
 * Description:
 * </p>
 * @author cmacnaug
 * @version 1.0
 */
public class HostProperties implements Serializable{

    public String os;
    public int numProcessors;
    public int systemMemory;
    public String defaultHostName;
    public String defaultExternalHostName;
    public String dataDirectory;
    private Properties systemProperties;
    
    /**
     * Gets the os that the host is running. 
     * @return The host's os. 
     */
    public String getOS()
    {
        return os;
    }
    
    /**
     * @return Gets the number of processors on the host.
     */
    public int getNumProcessors()
    {
        return numProcessors;
    }
    
    /**
     * @return Gets the total amount of RAM on the host. 
     */
    public int getSystemMemory()
    {
        return systemMemory;
    }
    
    /**
     * The default hostname 
     * @return The hostname as seen by the host  
     */
    public String getDefaultHostName()
    {
        return defaultHostName; 
    }
    
    /**
     * The hostname externally accessible to by other hosts. If the host isn't behind a firewall
     * this will be the same as the hostname.  
     * 
     * @return The hostname externally accessible to by other hosts
     */
    public String getExternalHostName()
    {
        return defaultExternalHostName;
    }
    
    /**
     * @return Returns a directory on the host that is free for tests to use. 
     */
    public String getDataDirectory()
    {   
        return dataDirectory;
    }
    
    public Properties getSystemProperties()
    {
        return systemProperties;
    }
}
