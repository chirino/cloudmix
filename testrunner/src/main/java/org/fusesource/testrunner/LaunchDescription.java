package org.fusesource.testrunner;

import org.fusesource.testrunner.Expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author chirino
*/
public class LaunchDescription implements Serializable {
    ArrayList<Expression> command = new ArrayList<Expression>();
    HashMap<String, Expression> environment;
    Expression.FileExpression workingDirectory;
    ArrayList<LaunchResource> resources = new ArrayList<LaunchResource>();
    
    public LaunchDescription add(String value) {
        return add(Expression.string(value));
    }

    public LaunchDescription add(Expression value) {
        command.add(value);
        return this;
    }

    public LaunchDescription setEnv(String key, String value) {
        return setEnv(key, Expression.string(value));
    }

    public LaunchDescription setEnv(String key, Expression value) {
        if( environment == null ) {
            environment = new HashMap<String, Expression>();
        }
        environment.put(key, value);
        return this;
    }
    
    /**
     * Adds a resource to the launch description. The receiving
     * agent will resolve it, copying it to it's local resource
     * cache. 
     * 
     * To refer to the Resource on the command line call {@link #add(Expression)}
     * with {@link Expression#resource(LaunchResource)} e.g.
     * 
     * <code>
     * LaunchDescription ld = new LaunchDescription();
     * LaunchResource lr = new LauncResource();
     * ... 
     * ld.addResource(lr);
     * ld.add(Expression.resource(lr);
     * </code>
     * 
     * 
     * @param resource
     * @see Expression#resource(LaunchResource)
     */
    public void addResource(LaunchResource resource) {
        resources.add(resource);
    }

    public Expression.FileExpression getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(Expression.FileExpression workingDirectory) {
        this.workingDirectory = workingDirectory;
    }


    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory =  Expression.file(workingDirectory);
    }

    public ArrayList<Expression> getCommand() {
        return command;
    }

    public HashMap<String, Expression> getEnvironment() {
        return environment;
    }

    public ArrayList<LaunchResource> getResources() {
        return resources;
    }
}