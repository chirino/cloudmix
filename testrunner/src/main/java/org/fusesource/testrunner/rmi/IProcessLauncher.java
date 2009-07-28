package org.fusesource.testrunner.rmi;

import java.rmi.Remote;

/**
 * @author chirino
 */
public interface IProcessLauncher extends Remote {

    public void bind(String owner) throws Exception;

    public void unbind(String owner) throws Exception;

    public IProcess launch(LaunchDescription launchDescription, IProcessListener handler) throws Exception;

}
