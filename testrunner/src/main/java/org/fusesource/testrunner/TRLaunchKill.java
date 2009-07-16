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
 * A TRLaunchKill is sent to a running TRAgent using a TRJMSCommunicator. The
 * TRLaunchKill provides the information necessary for the TRAgent to kill a
 * process that it is currently running.
 * 
 * @author Colin MacNaughton (cmacnaug)
 * @see TRJMSCommunicator
 * @see TRProcessCommunicator
 * @see TRAgent
 * @see TRLaunchDescr
 */
public class TRLaunchKill implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 5753967829955948330L;

    public TRLaunchKill() {
    }
}
