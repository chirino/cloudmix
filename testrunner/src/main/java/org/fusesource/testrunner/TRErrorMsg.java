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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Sent to the launcher if an error occurs. It is expected that applications
 * written to be launched by TestRunner will also make use of this class for
 * error reporting
 * 
 * @author Colin MacNaughton (cmacnaug)
 * @version 1.0
 * @since 02/09/01
 * @see TRMsg
 */

public class TRErrorMsg extends TRMsg {
    /**
     * 
     */
    private static final long serialVersionUID = 8203120286863862224L;
    protected String m_exception;

    /**
     * Constructor
     * 
     * @param source
     *            The toString() method is called on the object passed in and
     *            used to indicate the source of the exception
     * @param msg
     *            The message for this error or null for no message.
     * @param e
     *            The exception related to this error or null for no exception.
     * @param thrown
     *            The Throwable that may have caused the error (optionally null)
     */
    public TRErrorMsg(String msg, Throwable thrown) {
        super(msg);
        setException(thrown);
    }

    /**
     * Set the Throwable associated with this error object. The exception's
     * stack trace will be displayed in the error message.
     * 
     * @param e
     * @param thrown
     */
    public void setException(Throwable thrown) {
        if (thrown != null) {
            StringWriter stackTrace = new StringWriter();
            thrown.printStackTrace(new PrintWriter(stackTrace));
            m_exception = stackTrace.toString();
        }
    }

    /**
     * Convert this objec to a String to display.
     * 
     * @return An error message of the form: ERROR from <i>source</i>:
     *         <i>ErrorMessage</i> Related Exception: <i>ExceptionStackTrace</i>
     */
    public String toString() {
        return "ERROR from " + m_source + ":" + (m_message == null ? "" : newLine + "\t" + m_message) + (m_exception == null ? "" : newLine + "\tRelated Exception:" + newLine + m_exception);
    }

    public String getException() {
        return m_exception;
    }

}