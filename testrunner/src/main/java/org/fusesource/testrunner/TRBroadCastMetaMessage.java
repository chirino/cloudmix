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
 * public class TRBroadCastMetaMessage
 *
 *	Used to handle message metadata within testrunner when sending broadcast messages
 *
 *	author: Colin MacNaughton (cmacnaug@progress.com)
 *	Date: 03/11/01
 */
package org.fusesource.testrunner;

import java.util.Hashtable;

class TRBroadCastMetaMessage
extends TRMetaMessage
{
    /**
     * 
     */
    private static final long serialVersionUID = 2268767890922282320L;
    public String [] m_recips  = null;

    public TRBroadCastMetaMessage(Object content, String [] recips, Hashtable propsTable)
        throws java.io.IOException
    {
        super(content, propsTable);
        m_recips = recips;
    }
}