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

/**
 * Resource
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class LaunchResource implements Serializable {

    public static final short FILE = 0;
    public static final short DIRECTORY = 1;
    
    private short type = DIRECTORY;
    private String repoPath;
    private String repoUrl;
    private String repoName = "common";
    
    //Resolved by a resource manager to a local file system
    //path after the resource is downloaded.
    private transient String resolvedPath;

    /**
     * @return the id
     */
    public String getRepoPath() {
        return repoPath;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setRepoPath(String id) {
        this.repoPath = id;
    }

    /**
     * @return the repoUrl
     */
    public String getRepoUrl() {
        return repoUrl;
    }

    /**
     * @param repoUrl
     *            the repoUrl to set
     */
    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    /**
     * @return the repoName
     */
    public String getRepoName() {
        return repoName;
    }

    /**
     * @param repoName
     *            the repoName to set
     */
    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }
    
    /**
     * @return the type
     */
    public short getType() {
        return type;
    }

    /**
     * Sets the type of resource. If directory is 
     * specified then the agent will recursively pull
     * down the contents of the directory.
     * 
     * @param type the type to set
     */
    public void setType(short type) {
        this.type = type;
    }

    void setResolvedPath(String resolvedPath) {
        this.resolvedPath = resolvedPath;
    }

    public String getResolvedPath() {
        return resolvedPath;
    }
}
