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

/**
 * public class TRMetaMessage
 *
 *	Used to handle message metadata within testrunner
 *
 *	author: Colin MacNaughton (cmacnaug@progress.com)
 *	Date: 03/11/01
 */
package org.fusesource.testrunner;

import java.io.*;
import java.util.Hashtable;

class TRMetaMessage implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 8934236626977238345L;
    private byte[] m_classBytes = null;
    private Hashtable m_props = null;

    public TRMetaMessage(Object content, Hashtable props) throws IOException {
        if (content != null) {
            // store serialized object
            //Object obj = null;
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            BufferedOutputStream buffStream = new BufferedOutputStream(byteStream);
            ObjectOutputStream out = new ObjectOutputStream(buffStream);
            out.writeObject(content);
            out.flush();
            m_classBytes = byteStream.toByteArray();
            out.close();
        }
        m_props = props;
    }

    public Object getContent() throws Throwable {
        return getContent(null);
    }

    public Object getContent(TRClassLoader tcl) throws Throwable {
        if (m_classBytes == null)
            return null;
        // de-serialize content
        TRClassLoader classLoader = tcl;
        if (classLoader == null)
            classLoader = new TRClassLoader("");
        Object obj = null;
        ByteArrayInputStream inByteStream = new ByteArrayInputStream(m_classBytes);
        TRLoaderObjectInputStream inputStream = new TRLoaderObjectInputStream(new BufferedInputStream(inByteStream), classLoader);
        try {
            obj = inputStream.recoverableReadObject();
        } catch (Throwable thrown) {
            throw thrown;
        }
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }

        if (inByteStream != null) {
            inByteStream.close();
            inByteStream = null;
        }
        return obj;
    }

    public Hashtable getProperties() {
        return m_props;
    }
}