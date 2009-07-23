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
 * private class TRLoadingObjectInputStream
 *
 *  An extension of ObjectInputStream that attempts to load classes from a TRClassLoader
 *
 *  Author: Colin MacNaughton (cmacnaug@progresss.com)
 *	Since: 01/27/00
 */
package org.fusesource.testrunner;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.io.OptionalDataException;
import java.io.ObjectStreamClass;
import java.io.IOException;

/**
 * An extension of ObjectInputStream that attempts to load objects with classes
 * derived from the TRClassLoader specified
 * 
 * @author Colin MacNaughton (cmacnaug@progress.com)
 * @version 1.0
 * @since 1.0
 */
public class TRLoaderObjectInputStream extends ObjectInputStream {
    private static boolean DEBUG = false;
    ClassLoader classLoader;
    InputStream m_underlyingStream;

    /**
     * Constructs a new TRLoaderObjectInputStream
     * 
     * @param in
     *            The input stream from which to read objects
     * @param trcl
     *            The TRClassLoader to use.
     * @exception java.io.StreamCorruptedException
     * @exception java.io.IOException
     */
    public TRLoaderObjectInputStream(InputStream in, ClassLoader cl) throws StreamCorruptedException, IOException {
        super(in);
        m_underlyingStream = in;
        classLoader = cl;
    }

    protected Class resolveClass(ObjectStreamClass v) throws ClassNotFoundException, IOException {
        if (DEBUG)
            System.out.println("TRLoaderObjectInputStream loading class " + v.getName());

        //Doesn't work with JDK 1.6 see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6516909
        //return classLoader.loadClass(v.getName(), true);
        
        return Class.forName(v.getName(), false, classLoader);
    }

    /**
     * Sets a different TRClassLoader.
     * 
     * @param trcl
     *            The new TRClassLoader
     */
    public void setClassLoader(ClassLoader trcl) {
        classLoader = trcl;
    }

    /**
     * As the this object allocates some resources, it should be closed when no
     * longer needed.
     * 
     * @exception java.io.IOException
     *                If an error occurs closing the stream
     */
    public void close() throws IOException {
        super.close();
        classLoader = null;
    }
    
    /**
     * Sets whether this object writes debug output to System.out
     * 
     * @param val
     *            The debugFlag
     */
    public void setDebug(boolean val) {
        DEBUG = val;
    }

    public synchronized Object recoverableReadObject() throws StreamCorruptedException, ClassNotFoundException, OptionalDataException, IOException {
        m_underlyingStream.mark(Integer.MAX_VALUE);
        Object obj = null;
        try {
            obj = super.readObject();
        } catch (java.io.StreamCorruptedException sce) {
            m_underlyingStream.reset();
            byte[] nonSerializedData = new byte[m_underlyingStream.available()];

            m_underlyingStream.read(nonSerializedData, 0, nonSerializedData.length);
            //            System.out.println("Corrupted: " + nonSerializedData.length + ": "+ HexSupport.toHexFromBytes(nonSerializedData));

            throw new StreamCorruptedException("The following data could not be read as an object: " + new String(nonSerializedData));
        }
        return obj;
    }
}