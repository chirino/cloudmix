package org.fusesource.testrunner;

import java.io.IOException;

/**
 * @author chirino
 */
public interface LocalStreamListener {

    int FD_STD_IN = 0;
    int FD_STD_OUT = 1;
    int FD_STD_ERR = 2;

    public void open(int fd) throws IOException;

    public void write(int fd, byte[] data) throws IOException;

    public void close(int fd) throws IOException;
}