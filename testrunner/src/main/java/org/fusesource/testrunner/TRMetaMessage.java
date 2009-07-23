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

    private byte[] contentBytes = null;
    protected Hashtable props = null;
    private boolean isInternal = false;

    private transient Object content;
    private transient String source;

    TRMetaMessage(Object content) {
        this(content, null);
    }

    TRMetaMessage(Object content, Hashtable props) {
        if (content != null) {
            // store serialized object
            //Object obj = null;
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try {
                ObjectOutputStream out = new ObjectOutputStream(byteStream);
                out.writeObject(content);
                out.flush();
                contentBytes = byteStream.toByteArray();
                out.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        this.props = props;
    }

    TRMetaMessage(byte[] contentBytes, Hashtable props) {
        this.contentBytes = contentBytes;
        this.props = props;
    }

    public void setIntProperty(String key, Integer val) {
        if (props == null) {
            props = new Hashtable();
        }
        props.put(key, val);
    }

    public Integer getIntProperty(String key) {
        if (props == null) {
            return null;
        } else {
            return (Integer) props.get(key);
        }
    }

    public void setProperty(String key, Object val) {
        if (props == null) {
            props = new Hashtable();
        }
        props.put(key, val);
    }

    public String getProperty(String key) {
        if (props == null) {
            return null;
        } else {
            return (String) props.get(key);
        }
    }

    public Hashtable getProperties() {
        return props;
    }

    byte[] getContentBytes() {
        return contentBytes;
    }

    /**
     * If the message is internal then it is safe to call {@link #getContent()}
     * since only testrunner classes will be present in the object.
     * 
     * @return
     */
    boolean isInternal() {
        return isInternal;
    }

    /**
     * @param b
     */
    public void setInternal(boolean isInternal) {
        this.isInternal = isInternal;
    }

    /**
     * @param source
     *            The source of the message
     */
    void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public Object getContent() throws Exception {
        if (content != null) {
            return content;
        }

        if (contentBytes == null) {
            return null;
        }

        Object obj = null;
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(contentBytes));
        try {
            return ois.readObject();
        } finally {
            ois.close();
        }
    }

}