package org.fusesource.testrunner.rmi;

import org.fusesource.rmiviajms.JMSRemoteObject;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQDestination;

import javax.jms.Destination;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarOutputStream;
import java.util.zip.*;

/**
 * @author chirino
 */
public class RemoteClassLoader extends ClassLoader {
    private IClassLoaderServer server;

    static class PathElement implements Serializable {
        URL url;
        long jarFileChecksum;
        long jarFileSize;
    }

    public interface IClassLoaderServer extends Remote {
        IClassLoaderServer getParent() throws RemoteException;
        List<PathElement> getPathElements() throws RemoteException;
        byte[] download(URL url) throws RemoteException, IOException;
        byte[] findResource(String name) throws RemoteException;
    }


    static public class ClassLoaderServer implements IClassLoaderServer {
        private final IClassLoaderServer parent;
        private final ClassLoader classLoader;
        List<PathElement> elements;

        public ClassLoaderServer(IClassLoaderServer parent, ClassLoader classLoader) throws IOException {
            this.parent = parent;
            this.classLoader = classLoader;
            elements = createElements(classLoader);
        }

        public IClassLoaderServer getParent() throws RemoteException {
            return parent;
        }

        public List<PathElement> getPathElements() throws RemoteException {
            return elements;
        }

        public byte[] download(URL url) throws RemoteException, IOException {
            for (PathElement element : elements) {
                if( element.url.equals(url) ) {
                    return read(url.openStream());
                }
            }
            return null;
        }

        public byte[] findResource(String name) throws RemoteException {
            InputStream is = classLoader.getResourceAsStream(name);
            if( is ==null )
                return null;
            return read(is);
        }

    }

    static final long ROUNDUP_MILLIS = 1999;

    static private List<PathElement> createElements(ClassLoader classLoader) throws IOException {
        if( !(classLoader instanceof URLClassLoader) ) {
            return null;
        }
        URLClassLoader ucl = (URLClassLoader) classLoader;
        URL[] urls = ucl.getURLs();

        List<PathElement> rc = new ArrayList<PathElement>();
        for (URL url : urls) {
            PathElement element = new PathElement();
            element.url = url;
            if( "file".equals(url.getProtocol()) ) {
                File file = new File(url.getFile());

                if( !file.exists() ) {
                    continue;
                }

                // We have to jar up dirs..
                if( file.isDirectory() ) {
                    file = jar(file);
                    file.deleteOnExit();
                    element.url = file.toURI().toURL();
                }

                element.jarFileSize = file.length();
                element.jarFileChecksum = checksum(new FileInputStream(file));
            }
            rc.add(element);
        }
        return rc;
    }

    private static File jar(File source) throws IOException {
        File tempJar = File.createTempFile("temp", ".jar");
        ZipOutputStream os = new ZipOutputStream(new FileOutputStream(tempJar));
        os.setMethod(ZipOutputStream.DEFLATED);
        os.setLevel(5);
        recusiveAdd(os, source, null);
        os.close();
        return tempJar;
    }

    private static void recusiveAdd(ZipOutputStream os, File source, String jarpath) throws IOException {
        String prefix = "";
        if( jarpath!=null ) {
            ZipEntry entry = new ZipEntry(jarpath);
            entry.setTime(source.lastModified()+ROUNDUP_MILLIS);
            os.putNextEntry(entry);
            prefix = jarpath+"/";
        }

        if ( source.isDirectory() ) {
            for (File file : source.listFiles()) {
                recusiveAdd(os, file, prefix+file.getName());
            }
        } else {
            FileInputStream is = new FileInputStream(source);
            try {
                copy(is, os);
            } finally {
                try {
                    is.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    private static long checksum(InputStream is) throws IOException {
        Checksum sum = new CRC32();
        try {
            byte buffer[] = new byte[1024*4];
            int c;
            while( (c=is.read(buffer)) > 0 ) {
                sum.update(buffer, 0, c);
            }
            return sum.getValue();
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
            }
        }
    }

    private static byte[] read(InputStream is) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            copy(is, os);
            return os.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
            }
        }
    }

    private static void copy(InputStream is, OutputStream os) throws IOException {
        byte buffer[] = new byte[1024*4];
        int c;
        while( (c=is.read(buffer)) > 0 ) {
            os.write(buffer, 0, c);
        }
    }

    public static ClassLoader createRemoteClassLoader(String uri, File cacheDir, int depth, ClassLoader parent) throws IOException {
        Destination queue = ActiveMQDestination.createDestination(uri, ActiveMQDestination.QUEUE_TYPE);
        System.out.println("CL server at "+queue);
        IClassLoaderServer cle = JMSRemoteObject.toProxy(queue, IClassLoaderServer.class);
        return createRemoteClassLoader(cle, cacheDir, depth, parent);
    }
    
    /**
     * Builds a classloader tree to match remote classloader treee
     * exported by the IClassLoaderExporter.
     *
     * @param cle
     * @param cacheDir
     * @param depth
     * @param parent
     * @return
     * @throws RemoteException
     */
    public static ClassLoader createRemoteClassLoader(IClassLoaderServer cle, File cacheDir, int depth, ClassLoader parent) throws IOException {

        if( depth == 0 ) {
            return parent;
        }
        if( cle == null ) {
            return parent;
        }

        parent = createRemoteClassLoader(cle.getParent(), cacheDir, depth-1, parent);
        List<PathElement> elements = cle.getPathElements();
        if( elements == null ) {
            // That classloader was not URL classloader based, so we could not import it
            // by downloading it's jars.. we will have to use dynamically.
            return new RemoteClassLoader(parent, cle);
        }

        // We can build stadard URLClassLoader by downloading all the
        // jars or using the same URL elements as the original classloader.
        ArrayList<URL> urls = new ArrayList<URL>();
        for (PathElement element : elements) {

            if( element.jarFileSize==0 ) {
                urls.add(element.url);
            } else {

                String name = new File(element.url.getPath()).getName();
                File jarDirectory = new File(cacheDir, Long.toHexString(element.jarFileChecksum));
                jarDirectory.mkdirs();
                File file = new File(jarDirectory, name);

                if ( !file.exists() ) {
//                    System.out.println("Downloading "+file);
                    // We need to download it...
                    File tmp = null;
                    FileOutputStream out=null;
                    try {
                        tmp = File.createTempFile(name, ".part", jarDirectory);
                        // Yeah this is not ideal.. we should really stream it..
                        byte []data = cle.download(element.url);
                        out = new FileOutputStream(tmp);
                        out.write(data);

                    } finally {
                        try{ out.close(); } catch (Throwable e) {}
                    }
                    if( !tmp.renameTo(file) ) {
                        tmp.delete();
                    }
                }

                // It may be in the cache dir allready...
                if ( file.exists() ) {

                    if( element.jarFileChecksum != checksum(new FileInputStream(file))
                            || element.jarFileSize != file.length() ) {
                        throw new IOException("Checksum missmatch: "+name);
                    }

                    urls.add(file.toURI().toURL());
                } else {
                    throw new IOException("Could not download: "+name);
                }
            }
        }

        URL t[] = new URL[urls.size()];
        urls.toArray(t);
        return new URLClassLoader(t, parent);
    }


    public RemoteClassLoader(ClassLoader parent, IClassLoaderServer server) {
        super(parent);
        this.server = server;
    }

    public Class findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        try {
            byte data[] = server.findResource(path);
            if( data == null ) {
                throw new ClassNotFoundException(name);
            }
            return defineClass(name, data, 0, data.length);
        } catch (RemoteException e) {
            throw new ClassNotFoundException(name);
        }
    }

    // TODO: need to handle resoruces.

}