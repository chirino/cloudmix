package org.fusesource.testrunner.rmi;

import java.util.LinkedList;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URI;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @author chirino
 */
public class RemoteLoadingMain {
    
    private File classLoaderLibs;
    private File cacheDirectory;
    private String classLoaderURI;
    private String mainClass;
    private String[] args;
    private int depth=100;

    static class SyntaxException extends Exception {
        SyntaxException(String message) {
            super(message);
        }
    }

    public static void main(String args[]) throws Throwable {

        RemoteLoadingMain main = new RemoteLoadingMain();

        LinkedList<String> alist = new LinkedList<String>(Arrays.asList(args));

        try {
            // Process the options.
            while( !alist.isEmpty() ) {
                String arg = alist.removeFirst();
                if( arg.equals("--help") ) {
                    showUsage();
                    return;
                } else if( arg.equals("--cache-dir") ) {
                    try {
                        main.setCacheDirectory(new File(alist.removeFirst()).getCanonicalFile());
                    } catch (Exception e) {
                        throw new SyntaxException("Expected a directoy after the --cache-dir option.");
                    }
                } else if( arg.equals("--classloader-url") ) {
                    try {
                        main.setClassLoaderURI(alist.removeFirst());
                    } catch (Throwable e) {
                        throw new SyntaxException("Expected a url after the --classloader-url option.");
                    }
                } else if( arg.equals("--classloader-libs") ) {
                    try {
                        main.setClassLoaderLibs(new File(alist.removeFirst()).getCanonicalFile());
                    } catch (Throwable e) {
                        throw new SyntaxException("Expected a directoy after the --classloader-libs option.");
                    }
                } else if( arg.equals("--classloader-depth") ) {
                    try {
                        main.setDepth(Integer.parseInt(alist.removeFirst()));
                    } catch (Throwable e) {
                        throw new SyntaxException("Expected a number after the --classloader-depth option.");
                    }
                } else {
                    // Not an option.. then it must be the main class name and args..
                    main.setMainClass(arg);
                    String a[]= new String[alist.size()];
                    alist.toArray(a);
                    main.setArgs(a);
                    break;
                }
            }

            // Validate required arguments/options.
            if( main.getMainClass()==null ) {
                throw new SyntaxException("Main class not specified.");
            }
            if( main.getCacheDirectory()==null ) {
                throw new SyntaxException("Cache directory not specified.");
            }
            if( main.getClassLoaderURI()==null ) {
                throw new SyntaxException("ClassLoader URL not specified.");
            }

        } catch (SyntaxException e) {
            System.out.println("Invalid Syntax: "+e.getMessage());
            System.out.println();
            showUsage();
            System.exit(2);
        }
        main.execute();
    }

    private void execute() throws Throwable {
        ClassLoader mainCl = loadMainClassLoader();
        Class clazz = mainCl.loadClass(mainClass);

        // Invoke the main.
        Method mainMethod = clazz.getMethod("main", new Class[]{String[].class});
        try {
            mainMethod.invoke(null, new Object[]{ args });
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private ClassLoader loadMainClassLoader() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ClassLoader cl = getClass().getClassLoader();
        if( classLoaderLibs!=null ) {
            // Doing this allows us to keep the classes used in remote class loading
            // seperate from the classes loaded by those classloaders.
            ArrayList<URL> urls = new ArrayList<URL>();
            for (File file : classLoaderLibs.listFiles()) {
                if( file.isFile() &&
                    ( file.getName().endsWith(".jar")
                      || file.getName().endsWith(".zip")
                    )
                  ) {
                    urls.add(file.getCanonicalFile().toURI().toURL());
                }
            }
            URL u[] = new URL[urls.size()];
            urls.toArray(u);
            cl = new URLClassLoader(u);
        }

        // Create the remote classloader
        Class remoteCLClass = cl.loadClass("org.fusesource.testrunner.rmi.RemoteClassLoader");
        Method method = remoteCLClass.getMethod("createRemoteClassLoader", new Class[]{String.class, File.class, int.class, ClassLoader.class});

        // Use the new remote classloader to load the main class
        ClassLoader mainCl = null;
        mainCl = (ClassLoader) method.invoke(null, new Object[]{classLoaderURI, cacheDirectory, depth, null});
        return mainCl;
    }

    private static void showUsage() {
        System.out.println();
    }

    public void setClassLoaderURI(String classLoaderURI) {
        this.classLoaderURI = classLoaderURI;
    }

    public void setCacheDirectory(File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    public File getCacheDirectory() {
        return cacheDirectory;
    }

    public String getClassLoaderURI() {
        return classLoaderURI;
    }

    public File getClassLoaderLibs() {
        return classLoaderLibs;
    }

    public void setClassLoaderLibs(File classLoaderLibs) {
        this.classLoaderLibs = classLoaderLibs;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }


    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}