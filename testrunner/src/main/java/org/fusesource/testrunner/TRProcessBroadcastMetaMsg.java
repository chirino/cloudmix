package org.fusesource.testrunner;

import java.io.IOException;
import java.util.Vector;

/**
 * Used to broadcast an object to multiple processes running across multiple agents.
 */
public class TRProcessBroadcastMetaMsg extends TRMetaMessage implements java.io.Serializable {

    private static final long serialVersionUID = 3532929922135852792L;

    TRProcessContext[] m_recipProcs;
    private transient String[] m_trRecips;
    private transient boolean m_serialized = true;

    public TRProcessBroadcastMetaMsg(Object msg, TRProcessContext[] recipProcs) throws IOException {
        super(msg);
        setInternal(true);
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

    public TRMetaMessage[] getSubMessages(String agentId)
    {
        Vector msgs = new Vector();
        for(int i = 0; i < m_recipProcs.length; i++)
        {
            TRMetaMessage msg = new TRMetaMessage(getContentBytes(), getProperties());
            msg.setIntProperty(TRAgent.PID, m_recipProcs[i].getPid());
            msg.setSource(getSource());
            msg.classLoader = classLoader;
            msgs.add(msg);
        }
        
        TRMetaMessage[] ret = new TRMetaMessage [msgs.size()];
        msgs.copyInto(ret);
        return ret;
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