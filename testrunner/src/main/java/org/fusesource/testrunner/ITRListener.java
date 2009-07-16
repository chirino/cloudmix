package org.fusesource.testrunner;

/**
 * Defines the interface used for an TRAgentComHub to route messages.
 * 
 * @author Colin MacNaughton
 * @since 2.0
 * @see TRAgentComHub
 */
public interface ITRListener {
    /**
     * Called when there is an asynchronous error:
     */
    public void onTRException(Object reason, Throwable thrown);

    /**
     * Called when testrunner has information to output or when a TRMsg,
     * TRErrorMsg, or TRDisplayMsg is received from an agent. Applications
     * should check for instances of TRMsg types and act accordingly.
     */
    public void onTROutput(Object msg);
}