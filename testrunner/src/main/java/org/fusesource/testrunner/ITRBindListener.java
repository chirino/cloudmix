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
 * public Interface ITRBindListener
 *
 *	This defines an interface for an object using a JMSCommunicator object to
 *  set indicate that it can be bound to listen to a single 'controller'
 *
 *	Author: Colin MacNaughton (cmacnaug@progresss.com)
 *	Since: 01/03/00
 */

package org.fusesource.testrunner;

/**
 * Classes that wish to allow other entities to bind them must implement this
 * interface. Because bind and release requests are both operation for which the
 * controller must specify a timeout, the implementation of bindNotify and
 * bindReleaseNotify should not be long.
 * 
 * @author Colin MacNaughton (cmacnaug@progress.com)
 * @version 1.0
 * @since 1.0
 * @see TRJMSCommunicator
 */
public interface ITRBindListener {

    /**
     * The TRJMSCommunicator calls this method when a bind request is received
     * from a controller with a clientID specified by <c>controller</c>. The
     * programmer should not make this method very long as the bind operation
     * has a timeout
     * 
     * @param controller
     */
    //Upon returning this communicator knows that it is bound until
    //it's bind release method is called.
    public abstract void bindNotify(String controller);

    /**
     * The TRJMSCommunicator calls this method when the controller releases this
     * agent. This method should return quickly as release operates under a
     * timeout.
     * 
     * @param controller
     */
    public abstract void bindReleaseNotify(String controller);
}