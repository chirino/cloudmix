package org.fusesource.testrunner;

import java.io.Serializable;

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

public class TRProcessContext implements Serializable {
    private static final long serialVersionUID = -1595780843031898697L;
    private String m_agentID;
    private Integer m_pid;

    TRProcessContext(String agentID, Integer pid) {
        m_agentID = agentID;
        m_pid = pid;
    }

    String getAgentID() {
        return m_agentID;
    }

    Integer getPid() {
        return m_pid;
    }

    public boolean equals(Object comp) {
        if (comp instanceof TRProcessContext) {
            TRProcessContext trpc = (TRProcessContext) comp;
            //System.out.println("Matching...");
            if (!trpc.getAgentID().equalsIgnoreCase(m_agentID))
                return false;
            if (trpc.getPid().intValue() != m_pid.intValue())
                return false;

            //System.out.println("Matched!");
            return true;
        } else {
            //System.out.println("Match failed!");
            return false;
        }
    }

    public String toString() {
        return m_agentID + "@@PID:" + m_pid;
    }
}