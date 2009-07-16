/*
 * Copyright (c) 1999, 2000, 2001 Sonic Software Corporation. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Progress Software Corporation.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Progress.
 *
 * PROGRESS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. PROGRESS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

 /*
 * public class TRClassLoader
 *
 *  A customized class loader for TestRunner. This class is necessary
 *  because at the time a TestRunner Agent is launched it does not know
 *  what object may be passed to it via an object stream such as
 *  TRJMSCommunicator or TRProcessCommunicator. It would be insufficient
 *  to launch TRAgent with certain classes in the classpath as the versions
 *  of the classes being passed are subject to change.
 *
 *	Author: Colin MacNaughton (cmacnaug@progresss.com)
 *	Since: 01/27/00
 */

package org.fusesource.testrunner;

import java.util.*;
import java.util.zip.*;
import java.io.*;

/**
 * This classloader can be used to dynamically load classes at runtime. Typically applications
 * using TestRunner will not use TRClassloader because their application specific classes
 * are already in their classpath. TestRunner, however, must make use of TRClassLoader because
 * it does not 'know' what classes may be passed to it.
 */
public class TRClassLoader
extends ClassLoader
{
    private static boolean DEBUG = false;

    //Store exposed class path
    private String m_classpath;

    //To store the bytes which represent classes:
    private Hashtable classBytesTable;
    //To store classes that have already been loaded:
    private Hashtable loadedClassesCache;

    /**
     * Constructs a new classloader with the specified classpath ( + those already loaded by the
     * primordial classloader).
     *
     * @param classpath The classpath
     * @exception java.lang.Exception
     */
    public TRClassLoader(String classpath)
    throws Exception
    {
        super();
        m_classpath = classpath;
        classBytesTable = new Hashtable();
        loadedClassesCache = new Hashtable();

        StringTokenizer cpTokenizer = new StringTokenizer(classpath, File.pathSeparator);

        while(cpTokenizer.hasMoreTokens())
        {
            if(DEBUG) System.out.println("Dynamically loading: " + classpath);
            String token = cpTokenizer.nextToken();

            //If the token is a class file then we can go ahead and load it
            //under the assumption that this is an accessible file
            if(token.endsWith(".class"))
            {
                try
                {
                    BufferedInputStream fis = new BufferedInputStream(new FileInputStream(token));

                    int classSize = fis.available();

                    byte [] classBytes = new byte[classSize];
                    fis.read(classBytes);

                    storeClass(token, classBytes);
                }
                catch (Exception e)
                {
                    throw new Exception("ERROR: reading class file " + token);
                }
            }

            //else if it is a jar extract the contents of the jar:
            //and load those classes
            else if(token.endsWith(".jar"))
            {
                //ZipFile is needed to tell us the size of compressed files. (this info is not
                //available from the ZipInputStream used to read the jar:
                ZipFile zip = null;
                ZipInputStream zipStream = null;
                try
                {
                    zipStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(token)));
                    zip = new ZipFile(token);
                }
                catch (Exception e)
                {
                    throw new Exception("ERROR: opening arhive: " + token + " -- " + e.getMessage());
                }

                ZipEntry entry = null;
                while((entry = zipStream.getNextEntry()) != null)
                {
                    if (DEBUG) System.out.print("Compression for " + entry.getName() + ":");
                    if(entry.getMethod() == ZipEntry.STORED)
                    {
                        if (DEBUG) System.out.println("STORED: " + entry.getCompressedSize() + "/" + entry.getSize());
                    }
                    else if (entry.getMethod() == ZipEntry.DEFLATED)
                    {
                        if (DEBUG) System.out.println("DEFLATED: "  + entry.getCompressedSize() + "/" + entry.getSize());
                    }
                    else
                    {
                        if (DEBUG) System.out.println("Compression unknown " + entry.getCompressedSize() + "/" + entry.getSize());
                    }

                    //only load class files:
                    if(!entry.getName().endsWith(".class"))
                    {
                        continue;
                    }

                    try
                    {
                        int size = (int) entry.getSize();
                        if(size == -1)
                        {
                            size = (int) zip.getEntry(entry.getName()).getSize();
                        }
                        byte[] entryBytes = new byte[(int)size];
                        int rb=0;
                        int chunk=0;
                        while (((int)size - rb) > 0)
                        {
                            chunk = zipStream.read(entryBytes, rb ,(int)size - rb);
                            if (chunk==-1) {
                            break;
                            }
                            rb+=chunk;
                        }

                        if (DEBUG) System.out.println("Read " + rb + " for " + entry.getName() + "into array of size " + entryBytes.length);
                        storeClass(entry.getName(), entryBytes);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        throw new Exception("ERROR: reading " + entry.getName() + " from archive " + token);
                    }
                }//while(contents.hasMoreElements())
            }//else if(token.endsWith(".jar"))
        }//while(cpTokenizer.hasMoreTokens())

        if(DEBUG)
        {
            listLoaded(System.out);
        }
    }

    private void storeClass(String name, byte [] bytes)
    throws Exception
    {
        String className = name.replace('/', '.');
        className = className.replace('\\', '.');
        className = className.substring(0, className.length() - 6);
        classBytesTable.put(className, bytes);
    }

    /**
     * loads the specified class
     *
     * @param className The name of the class to load (using '.' as the path separator)
     * @param resolveIt whether to resolve the class. (Necessary if an object is to be instantiated with the class e.g. using
     * forName() or newInstance();
     * @return The class
     * @exception java.lang.ClassNotFoundException If the class cannot be found
     */
    public synchronized Class loadClass(String className, boolean resolveIt)
    throws ClassNotFoundException
    {
        return super.loadClass(className, resolveIt);
    }

    protected Class findClass(String name)
    throws ClassNotFoundException
    {
        if (DEBUG)
        {
            System.out.println(this.toString() + " attempting to find " + name);

        }
        //Check to see if we have loaded the class:
        if(loadedClassesCache != null && loadedClassesCache.containsKey(name))
        {
            return (Class) loadedClassesCache.get(name);
        }
        else if(classBytesTable != null && classBytesTable.containsKey(name))
        {
            Class result = null;
            byte [] classBytes = (byte []) classBytesTable.get(name);
            result = defineClass(name, classBytes, 0, classBytes.length);
            loadedClassesCache.put(name, result);
            return result;
        }
        else
        {
            throw new ClassNotFoundException(name);
        }
    }

    /* private String byteToHexString(byte b)
     *
     *	For debug streams
     */
//    private String byteToHexString(byte b)
//    {
//        String str = "";
//        str += "0x" + getHexHigh(b) + getHexLow(b);
//        return str;
//    }

    /* private String getHexHigh(byte b)
     *
     *	For debug streams
     */
//    private String getHexHigh(byte b)
//    {
//        return getHexLow((byte)(b >> 4));
//    }

    /* private String getHexLow(byte b)
     *
     *	For debug streams
     */
//    private String getHexLow(byte b)
//    {
//        switch (b & 0x0f)
//        {
//            case 0: return "0";
//            case 1: return "1";
//            case 2: return "2";
//            case 3: return "3";
//            case 4: return "4";
//            case 5: return "5";
//            case 6: return "6";
//            case 7: return "7";
//            case 8: return "8";
//            case 9: return "9";
//            case 10: return "a";
//            case 11: return "b";
//            case 12: return "c";
//            case 13: return "d";
//            case 14: return "e";
//            case 15: return "f";
//            default: return "X";
//        }
//    }

    /**
     * Sets whether this class prints debug output to System.out
     *
     * @param val debugFlag
     */
    /* public void setDebug(boolean val)
     *
     *	Set debug output for this class
     *
     */
    public void setDebug(boolean val)
    {
        DEBUG = val;
    }

    /**
     * Prints a list of loaded classes to the specified PrintStream
     *
     * @param out The PrintStream
     */
    public void listLoaded(PrintStream out)
    {
        Enumeration keys = classBytesTable.keys();
        if (keys.hasMoreElements())
        {
            out.println ("Loaded bytes for:");
        }
        while(keys.hasMoreElements())
        {
            out.println((String) keys.nextElement());
        }
    }

    /**
     * Returns the classpath for this class loader
     *
    **/
    public String getClasspath() { return m_classpath;}

    /**
     * Because the classloader allocates memory to load and store classes it should be closed when it is no
     * longer needed.
     */
    public void close()
    {
        if(classBytesTable != null)
        {
            classBytesTable.clear();
        }
        classBytesTable = null;

        if(loadedClassesCache != null)
        {
            loadedClassesCache.clear();
        }
        loadedClassesCache = null;
    }

    /**
     * Overides finalize() in object
     *
     * @exception java.lang.Throwable
     */
    public void finalize()
    throws Throwable
    {
        if(DEBUG) System.out.println("Running Finalization on " + this);
        super.finalize();
        if(DEBUG) System.out.println("Finished Running Finalization");
    }

    public String toString()
    {
        String lcc = (loadedClassesCache == null) ? " Cache is empty": "";
        String cbt = (classBytesTable == null) ? " Bytes Table is empty": "";
        return "TRClassLoader " + this.hashCode() + " for: " + m_classpath + lcc + cbt ;
    }
}
