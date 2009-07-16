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


package org.fusesource.testrunner;

import java.io.Serializable;
/**
 * Communication originating from TestRunner should use or extend this class to distinguish
 * TestRunner communication from application specific communication.
 *
 * @author Colin MacNaughton (cmacnaug)
 * @version 1.0
 * @since 1.0
 * @see TRDisplayMsg
 * @see TRErrorMsg
 */

public class TRMsg
implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -8535306535065590677L;
    protected String m_source;
    protected String m_message;
    protected String newLine;
    /**
     * Constructor
     *
     */
    public TRMsg(String message)
    {
        m_message = message;
        newLine = System.getProperties().getProperty("line.separator", "\n");
    }

    /**
     * Sets the error message associated with the TRErrorObj
     *
     * @param str The error message.
     */
    public void setMessage(String str)
    {
        m_message = str;
    }

    public void setSource(String str)
    {
        m_source = str;
    }

    /**
     * Convert this objec to a String to display.
     *
     */
    public String toString()
    {
       return (m_source == null ? "Unknown Source" : m_source) + ": " +
              (m_message == null ? "" : m_message);
    }

    public String getMessage()	{return m_message;}
    public String getSource()	{return m_source;}
}