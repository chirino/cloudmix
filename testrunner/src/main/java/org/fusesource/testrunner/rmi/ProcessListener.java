package org.fusesource.testrunner.rmi;

/**
 * Defines the interface used for an TRAgentComHub to route messages.
 * 
 * @author Colin MacNaughton
 * @since 2.0
 */
public interface ProcessListener {
    
    /**
     * Called when a process exits, passing the exit code.
     * @param exitCode
     */
    public void processDone(TRProcessContext ctx, int exitCode);
    

    public void handleSystemErr(TRProcessContext ctx, String err);

    public void handleSystemOut(TRProcessContext ctx, String output);

    public void handleError(TRProcessContext ctx, String message, Throwable thrown);

    public void handleProcessInfo(TRProcessContext ctx, String info);

    public void handleMessage(TRProcessContext ctx, Object msg);
}