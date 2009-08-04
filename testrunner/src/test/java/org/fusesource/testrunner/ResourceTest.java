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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

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

    Log log = LogFactory.getLog(ResourceTest.class);

    public void testWebDavResourceManager() throws Exception {

        ResourceManager rm = new ResourceManager();
        File localDir = new File("target" + File.separator + "test-repo");
        rm.setLocalRepoDir(localDir);
        log.info("Deleting local resource directory: " + localDir);
        rm.purgeLocalRepo();
        log.info("Deleted local resource directory: " + localDir);

        String remoteRepo = "dav://fusesource.com/forge/dav/fusemqptest/test-file-repo";
        AuthenticationInfo authInfo = new AuthenticationInfo();
        authInfo.setUserName("foo");
        authInfo.setPassword("bar");
        rm.setCommonRepo(remoteRepo, authInfo);

        LaunchResource resource = new LaunchResource();
        resource.setRepoName("common");
        resource.setRepoPath("testfolder");
        resource.setType(LaunchResource.DIRECTORY);

        rm.locateResource(resource);

        assertEquals(new File("test-file-repo", resource.getRepoPath()), new File(resource.getResolvedPath()));

        rm.close();

    }

    public void testFileResourceManager() throws Exception {
        ResourceManager rm = new ResourceManager();
        File localDir = new File("target" + File.separator + "test-repo");
        rm.setLocalRepoDir(localDir);
        log.info("Deleting local resource directory: " + localDir);
        rm.purgeLocalRepo();
        log.info("Deleted local resource directory: " + localDir);

        File remoteDir = new File("test-file-repo");
        rm.setCommonRepo(remoteDir.toURI().toString(), null);

        String resourcePath = "testfolder";
        LaunchResource resource = new LaunchResource();
        resource.setRepoName("common");
        resource.setRepoPath(resourcePath);
        resource.setType(LaunchResource.DIRECTORY);

        rm.locateResource(resource);

        assertEquals(new File("test-file-repo", resource.getRepoPath()), new File(resource.getResolvedPath()));

        rm.close();
    }

    /**
     * @param resolvedPath
     */
    private void assertEquals(File source, File copy) {
        assertTrue(copy.exists());
        
        if(source.isFile() && copy.isFile())
        {
            return;
        }
        else if(source.isFile() != copy.isFile())
        {
            fail(source + " is not equal to " + copy);
        }
        
        //Compare directory contents:
        ArrayList<String> copies = new ArrayList<String>(Arrays.asList(copy.list()));
        List<String> sources = Arrays.asList(source.list());
        for (String sf : sources)
        {
            if(!copies.remove(sf))
            {
                fail(sf + " not found in " + copy);
            }
            
            assertEquals(new File(source, sf), new File(copy, sf));
            
        }
       
        if(!copies.isEmpty())
        {
            fail("Extra files in copy: " + copies);
        }
        
        
    }
}
