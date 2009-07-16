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
 *	An object that is intented only to be displayed by the receiver with no other impact
 * on application flow. Entities that receive this Object through a TestRunner Communicator
 * should display this object to stdout.
 *
 */
package org.fusesource.testrunner;

/**
 * This is a TestRunner message whose type indicates that the its content is
 * information and not appropriate for application handling. By default a
 * TRJMSCommunicator only prints this type of message to screen and does not
 * return it as a message, althought the communicator can be set to return them.
 * Occasional applications may find it useful to send these. If for example it
 * is desirable to have a message printed on TRAgents screen this messagetype
 * can be sent. Launched processes may also wish to send these to display
 * information on their controller's console
 * 
 * @author Colin MacNaughton (cmacnaug@progress.com)
 * @version 1.0
 * @since 1.0
 * @see TRJMSCommunicator
 */
public class TRDisplayMsg extends TRMsg {

    /**
     * 
     */
    private static final long serialVersionUID = 819786134008866931L;

    /**
     * Constructor
     * 
     * @param message
     *            The message to send
     */
    public TRDisplayMsg(String message) {
        super(message);
    }
}