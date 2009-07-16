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
 * public Interface ITRAsyncMessageHandler
 *
 *	Class implementing this interface can handle receipt of asynchronous Messages from the
 *  controller
 *
 *	Author: Colin MacNaughton (cmacnaug@progresss.com)
 *	Since: 01/03/00
 */

package org.fusesource.testrunner;

/**
 * Classes that wish to receive BroadCast messages asynchronously from a
 * JMSCommunicator must implement this interface.
 * 
 * @author Colin MacNaughton (cmacnaug@progress.com)
 * @version 1.0
 * @since 1.0
 * @see TRJMSCommunicator
 */
public interface ITRAsyncMessageHandler {
    /**
     * This callback is called by a TRJMSCommunicator upon receipt of an
     * asynchronous broadcast message
     * 
     * @param obj
     */
    public void handleMessage(Object obj);
}