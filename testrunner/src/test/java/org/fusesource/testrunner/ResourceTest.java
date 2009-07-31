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

import java.io.File;

import junit.framework.TestCase;

/**
 * ResourceTest
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class ResourceTest extends TestCase {

    public void testResourceManager() throws Exception {
        ResourceManager rm = new ResourceManager();
        File localDir = new File("target" + File.separator + "test-repo");
        rm.setLocalRepoDir(localDir);
        rm.purgeLocalRepo();
        rm.close();

        
//        File remoteDir = new File("C:/jvms");
//        rm.setCommonRepo(remoteDir.toURI().toString());
//
//        String resourcePath = "ibmjdk1.3.0_012402";
//        LaunchResource resource = new LaunchResource();
//        resource.setRepoName("common");
//        resource.setRepoPath(resourcePath);
//
//        rm.locateResource(resource);
//
//        assertTrue(new File(resource.getResolvedPath()).exists());
//        assertEquals(new File(resource.getResolvedPath()).getCanonicalPath(), localDir.getCanonicalPath() + File.separator + resourcePath);
    }
    
}
