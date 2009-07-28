package org.fusesource.testrunner;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author chirino
*/
abstract public class Expression implements Serializable {
    abstract public String evaluate();

    public static PropertyExpression property(String name) {
        return new PropertyExpression(name, null);
    }

    public static PropertyExpression property(String name, Expression defaultExpression) {
        return new PropertyExpression(name, defaultExpression);
    }

    public static StringExpression string(String value) {
        return new StringExpression(value);
    }

    public static FileExpression file(String value) {
        return new FileExpression(string(value));
    }

    public static FileExpression file(Expression value) {
        return new FileExpression(value);
    }

    public static PathExpression path(List<FileExpression> list) {
        return new PathExpression(list);
    }
    public static PathExpression path(FileExpression... value) {
        List<FileExpression> list = Arrays.asList(value);
        return path(list);
    }

    public static AppendExpression append(List<Expression> list) {
        return new AppendExpression(list);
    }
    public static AppendExpression append(Expression... value) {
        List<Expression> list = Arrays.asList(value);
        return append(list);
    }


    public static class StringExpression extends Expression {
        String value;
        public StringExpression(String value) {
            this.value = value;
        }

        public String evaluate() {
            return value;
        }
    }

    public static class PropertyExpression extends Expression {
        String name;
        Expression defaultExpression;

        public PropertyExpression(String name, Expression defaultExpression) {
            this.name = name;
            this.defaultExpression = defaultExpression;
        }

        public String evaluate() {
            String rc =  System.getProperty(name);
            if( rc == null && defaultExpression !=null ) {
                rc = defaultExpression.evaluate();
            }
            return rc;
        }
    }

    public static class FileExpression extends Expression {
        Expression name;
        public FileExpression(Expression name) {
            this.name = name;
        }

        public String evaluate() {
            String t = name.evaluate();
            if( '/' != File.separatorChar ) {
                t.replace('/', File.separatorChar);
            } else {
                t.replace('\\', File.separatorChar);
            }
            return t;
        }
    }

    public static class PathExpression extends Expression {
        final ArrayList<FileExpression> files = new ArrayList<FileExpression>();
        public PathExpression(Collection<FileExpression> files) {
            this.files.addAll(files);
        }

        public String evaluate() {
            StringBuilder sb = new StringBuilder();
            boolean first=true;
            for (FileExpression file : files) {
                if( !first ) {
                    sb.append(File.pathSeparatorChar);
                }
                first=false;
                sb.append(file.evaluate());
            }
            return sb.toString();
        }
    }

    public static class AppendExpression extends Expression {
        final ArrayList<Expression> parts = new ArrayList<Expression>();
        public AppendExpression(Collection<Expression> parts) {
            this.parts.addAll(parts);
        }
        public String evaluate() {
            StringBuilder sb = new StringBuilder();
            for (Expression expression : parts) {
                sb.append(expression.evaluate());
            }
            return sb.toString();
        }
    }


    public String toString() {
        return evaluate();
    }
}