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
    HashMap<String, Expression> enviorment;
    Expression.FileExpression workingDirectory;

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
        if( enviorment == null ) {
            enviorment = new HashMap<String, Expression>();
        }
        enviorment.put(key, value);
        return this;
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

    public HashMap<String, Expression> getEnviorment() {
        return enviorment;
    }
}