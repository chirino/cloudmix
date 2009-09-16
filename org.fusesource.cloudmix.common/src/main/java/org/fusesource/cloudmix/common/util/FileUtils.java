/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class FileUtils {

    public static final int BUFFER_SIZE = 1024;

    private static final Log LOGGER = LogFactory.getLog(FileUtils.class);

    private FileUtils() { /* Utility classes should not have a public constructor */
    }

    public static File createDirectory(File parentDir, String path) {
        File dir = parentDir == null ? new File(path) : new File(parentDir, path);
        return createDirectory(dir);

    }

    public static File createDirectory(File dir) {
        if (!dir.exists()) {
            LOGGER.info("Creating work directory " + dir);
            if (!dir.mkdirs()) {
                LOGGER.error("failed to make work directory " + dir);
                return null;
            }
        }
        return dir;
    }

    public static void deleteDirectory(File dir) {
        LOGGER.info("deleting directory " + dir);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
            dir.delete();
        }
    }

    public static void deleteFile(File file) {
        LOGGER.info("deleting file " + file);
        if (!file.delete()) {
            LOGGER.warn("failed to delete file " + file);
        }
    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];

        long total = 0;
        int n = is.read(buffer);
        while (n != -1) {
            total += n;
            os.write(buffer, 0, n);
            n = is.read(buffer);
        }
        LOGGER.info("Copied " + total + " bytes");

        is.close();
        os.close();
    }

    public static String readFile(String path) throws IOException {
        return readFile(new File(path));
    }

    public static String readFile(File file) throws IOException {

        StringBuilder sb = new StringBuilder();

        BufferedReader reader = new BufferedReader(new FileReader(file));

        char[] chars = new char[BUFFER_SIZE];
        int length;

        while ((length = reader.read(chars)) > 0) {
            sb.append(chars, 0, length);
        }

        reader.close();
        return sb.toString();
    }
}
