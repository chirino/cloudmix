package org.fusesource.testrunner;

/**
 * Defines the interface used for an TRAgentComHub to route messages.
 * 
 * @author Colin MacNaughton
 * @since 2.0
 * @see TRAgentComHub
 */
public interface ProcessListener {
    
    /**
     * Called when a process exits, passing the exit code.
     * @param exitCode
     */
    public void processDone(TRProcessContext ctx, int exitCode);
    
    public void handleProcessInfo(TRProcessContext ctx, String info);
    
    public void handleError(TRProcessContext ctx, String message, Throwable thrown);
    
    public void handleSystemErr(TRProcessContext ctx, String err);
    
    public void handleSystemOut(TRProcessContext ctx, String output);
    
    public void handleMessage(TRProcessContext ctx, Object msg);
}