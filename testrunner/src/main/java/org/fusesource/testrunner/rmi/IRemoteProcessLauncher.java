package org.fusesource.testrunner.rmi;

import org.fusesource.testrunner.LaunchDescription;

import java.rmi.Remote;

/**
 * @author chirino
 */
public interface IRemoteProcessLauncher extends Remote {

    public void bind(String owner) throws Exception;

    public void unbind(String owner) throws Exception;

    public IRemoteProcess launch(LaunchDescription launchDescription, IRemoteProcessListener handler) throws Exception;

}
