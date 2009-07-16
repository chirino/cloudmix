package org.fusesource.testrunner;

import java.util.Vector;

/**
 * <p>
 * Title: TestRunner 2.0
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Sonic Software
 * </p>
 * 
 * @author Colin MacNaughton
 * @version 1.0
 */

public class TRComHubBroadcastMetaMsg implements java.io.Serializable {

    private static final long serialVersionUID = 3532929922135852792L;

    TRProcessContext[] m_recipProcs;
    Object m_msg;
    private transient String[] m_trRecips;
    private transient boolean m_serialized = true;

    public TRComHubBroadcastMetaMsg(Object msg, TRProcessContext[] recipProcs) {
        m_msg = msg;
        m_recipProcs = recipProcs;

        //Find the recipient TRAgents
        Vector trRecipList = new Vector();
        for (int i = 0; i < m_recipProcs.length; i++) {
            if (!trRecipList.contains(m_recipProcs[i].getAgentID())) {
                trRecipList.add(m_recipProcs[i].getAgentID());
            }
        }

        m_trRecips = new String[trRecipList.size()];
        trRecipList.copyInto(m_trRecips);
        m_serialized = false;
    }

    /**
     * @return Returns a reference to the payload object.
     */
    Object getMessage() {
        return m_msg;
    }

    /**
     * 
     * @return Returns the transient array of ids of testrunner agents with
     *         processes for which this message is intended
     * 
     *         This method should not be called on the receiving side
     */
    String[] getTRRecips() throws java.lang.IllegalAccessError {
        if (m_serialized) {
            throw new java.lang.IllegalAccessError("TRRecips not available after message has been serialized.");
        } else {
            return m_trRecips;
        }
    }

    /**
     * 
     * @return A list of process contexts for the recipients
     */
    TRProcessContext[] getRecips() {
        return m_recipProcs;
    }
}