package org.fusesource.testrunner;

import java.util.Hashtable;

/**
 * Defines the interface used for an TRAgentComHub to route messages.
 * 
 * @author Colin MacNaughton
 * @since 2.0
 * @see TRAgentComHub
 */
public interface ITRAgentComHandler {
    public void handleMessage(Object msg, Hashtable props, TRProcessContext ctx);
}