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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.apache.maven.wagon.providers.ftp.FtpWagon;
import org.apache.maven.wagon.providers.http.HttpWagon;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.util.FileUtils;

/**
 * RepositoryManager
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class ResourceManager {

    //Access to our local repo:
    private Wagon localWagon;

    //Access to the remote common repo:
    private Wagon commonWagon;

    private static final HashMap<String, Class<? extends Wagon>> wagonProviders = new HashMap<String, Class<? extends Wagon>>();

    private HashMap<String, Wagon> connectedRepos = new HashMap<String, Wagon>();

    static {
        wagonProviders.put("file", FileWagon.class);
        wagonProviders.put("ftp", FtpWagon.class);
        wagonProviders.put("http", HttpWagon.class);
    }

    public void setLocalRepoDir(File localRepoDir) throws Exception {
        Repository localRepo = new Repository("local", localRepoDir.toURI().toString());
        if (!localRepoDir.exists()) {
            localRepoDir.mkdir();
        }
        localWagon = connectWagon(localRepo);
    }

    public void setCommonRepo(String url) throws Exception {
        Repository remoteRepo = new Repository("common", url);
        commonWagon = connectWagon(remoteRepo);
    }

    public void locateResource(LaunchResource resource) throws Exception {
        Wagon w = null;
        long timestamp = 0;
        boolean existsLocally = false;
        if (localWagon.resourceExists(resource.getRepoPath())) {
            existsLocally = true;
            timestamp = new File(localWagon.getRepository().getBasedir() + File.separator + resource.getRepoPath()).lastModified();
        } else {
            synchronized (this) {
                w = connectedRepos.get(resource.getRepoName());
                if (w == null) {
                    Repository remote = new Repository(resource.getRepoName(), resource.getRepoUrl());
                    w = connectWagon(remote);
                }
            }

            if (w != null && w.resourceExists(resource.getRepoPath())) {
                try {
                    if (resource.getType() == LaunchResource.DIRECTORY) {
                        downloadDirectory(w, new File(localWagon.getRepository().getBasedir()), resource.getRepoPath());
                    } else {
                        w.getIfNewer(resource.getRepoPath(), new File(localWagon.getRepository().getBasedir(), resource.getRepoPath()), timestamp);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (timestamp == 0) {
                throw new Exception("Resource not found: " + resource);
            }
        }

        resource.setResolvedPath(localWagon.getRepository().getBasedir() + File.separator + resource.getRepoPath());
    }

    /**
     * @param resource
     * @param data
     * @throws IOException
     */
    public void deployFile(LaunchResource resource, byte[] data) throws Exception {
        // TODO Auto-generated method stub
        File f = File.createTempFile("tmp", "dat");
        FileOutputStream fw = new FileOutputStream(f);
        fw.write(data);
        fw.flush();
        fw.close();
        try {
            deployResource(resource, f);
        } finally {
            f.delete();
        }
    }

    public void deployDirectory(LaunchResource resource, File d) throws Exception {
        deployResource(resource, d);
    }

    private void deployResource(LaunchResource resource, File f) throws Exception {
        Wagon w = null;
        synchronized (this) {
            w = connectedRepos.get(resource.getRepoName());
            if (w == null) {
                Repository remote = new Repository(resource.getRepoName(), resource.getRepoUrl());
                w = connectWagon(remote);
            }
        }

        w.put(f, resource.getRepoPath());
    }

    private Wagon connectWagon(Repository repo) throws Exception {
        Class<? extends Wagon> wagonClass = wagonProviders.get(repo.getProtocol());
        Wagon w = wagonClass.newInstance();
        w.connect(repo);
        connectedRepos.put(repo.getName(), w);
        return w;
    }

    private static final void downloadDirectory(Wagon source, File targetDir, String path) throws Exception {
        Iterator i = source.getFileList(path).iterator();
        while (i.hasNext()) {
            String file = (String) i.next();
            if (file.endsWith("/")) {
                downloadDirectory(source, targetDir, path + File.separator + file.substring(0, file.length() - 1));
            } else {
                downloadFile(source, targetDir, path + File.separator + file);
            }
        }
    }

    private static final void downloadFile(Wagon source, File targetDir, String name) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        source.get(name, new File(targetDir, name));
    }

    public void purgeLocalRepo() throws IOException {
        FileUtils.cleanDirectory(localWagon.getRepository().getBasedir());
    }

    /**
     * Closes all repository connections.
     * 
     * @throws Exception
     */
    public void close() {
        for (Wagon w : connectedRepos.values()) {
            try {
                w.disconnect();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        connectedRepos.clear();
    }

}
