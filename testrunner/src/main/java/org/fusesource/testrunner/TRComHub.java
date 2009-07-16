package org.fusesource.testrunner;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This class allows different handlers to share an agent. It maintains a list
 * of the active agents in the test, and control the TRJMSCommunicators needed.
 * 
 * The com hub was introduced
 * 
 * @author Colin MacNaughton
 * @since 2.0
 * @see ITRAgentComHandler
 */
public class TRComHub implements Runnable {
    private static final boolean DEBUG = false;

    private TRJMSCommunicator m_com;
    ITRListener m_listener;

    private Thread m_thread;

    //PID, handler pairs
    private final Hashtable m_dispatchTable;

    private int m_requestCounter = 0;
    private final Object m_requestSync = new Object();
    private final Hashtable m_pendingRequests = new Hashtable();

    //List of bound agents:
    private Vector m_boundList;

    //Timeouts
    private long m_bindTimeout;
    private long m_launchTimeout;
    private long m_killTimeout;
    String m_controlURL;
    private boolean m_closed = false;

    public TRComHub(String controlUrl, long bindTimeout, long launchTimeout, long killTimeout, ITRListener listener) throws Exception {
        m_controlURL = controlUrl;
        m_bindTimeout = bindTimeout;
        m_launchTimeout = launchTimeout;
        m_killTimeout = killTimeout;
        m_listener = listener;

        //Get a communications object to talk with all agents
        m_com = new TRJMSCommunicator(controlUrl, //TestRunner Server
                System.getProperty("user.name") + System.currentTimeMillis(), //clientID (null = not specified)
                null, //specifiecs that this Communicator is not bindable
                null); //specifies that we don't handle async messages.
        m_com.setSendDisplayObjs(true);

        m_dispatchTable = new Hashtable();
        m_boundList = new Vector();

        m_thread = new Thread(this);
        m_thread.setName("MRAgentComHub");
        m_thread.start();
    }

    public synchronized boolean bindAgent(String agentName) {
        if (m_closed) {
            throw new IllegalStateException("TRComHub is closed");
        }

        String agent = agentName.toUpperCase();
        try {
            if (m_boundList.contains(agent)) {
                return true;
            }

            boolean ret = m_com.bind(agent, m_bindTimeout);
            if (ret) {
                m_boundList.addElement(agent);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            m_listener.onTRException("Error binding agent " + agent, e);
            return false;
        }
    }

    public synchronized boolean releaseAll() throws Exception {
        for (int i = m_boundList.size() - 1; i >= 0; i--) {
            if (m_com.release((String) m_boundList.elementAt(i), m_bindTimeout)) {
                m_boundList.removeElementAt(i);
            } else {
                m_listener.onTROutput("Unable to release TRAgent: " + (String) m_boundList.elementAt(i) + ".");
            }

        }

        if (m_boundList.size() > 0) {
            return false;
        } else {
            m_dispatchTable.clear();
            return true;
        }
    }

    public void close() {
        synchronized (this) {
            m_closed = true;

            try {
                releaseAll();
            } catch (Exception e) {
                m_listener.onTRException("Error releasing agents.", e);
            }
        }

        //Release all agents
        //Wait for receive thread to finish
        try {
            m_thread.join();
        } catch (java.lang.InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        try {
            m_com.close();
        } catch (Exception e) {
            m_listener.onTRException("Error closing testrunner connection.", e);
        }

    }

    public void run() {
        while (!m_closed) {
            Object msg = null;

            try {
                msg = m_com.getMessage(1000);
            } catch (Exception e) {
                m_listener.onTRException("Error trying to get TRMessage", e);
            }

            if (msg == null) {
                //If we are closed wait for any late messages:
                /*
                 * if(m_closed) { try { msg = m_com.getMessage(5000); } catch
                 * (Exception e) {
                 * m_listener.onTRException("Error trying to get TRMessage", e);
                 * } //If there is still no message return; if(msg == null) {
                 * return; } } else
                 */
                {
                    continue;
                }
            }

            String source = m_com.getSource();
            if (DEBUG)
                m_listener.onTROutput("Dispatching message: " + msg);
            dispatch(source, msg, m_com.getProperties());
        }
    }

    private void dispatch(String source, Object msg, Hashtable props) {
        if (handleTRMsg(msg)) {
            return;
        }

        Integer pid = (Integer) props.get(TRAgent.PID);

        if (pid == null) {
            m_listener.onTROutput("A message was received with no pid: " + msg);
            return;
        } else {
            TRProcessContext context = new TRProcessContext(source, pid);

            //Look up in the dispatch table see if we have a handler
            //for communication with this pid and agentname
            ITRAgentComHandler handler = (ITRAgentComHandler) m_dispatchTable.get(context.toString());
            if (handler != null) {
                if (DEBUG)
                    m_listener.onTROutput("Creating DispatchThread for " + handler);
                //handler.handleMessage(msg, props);
                //TODO: Create a thread pool:
                new DispatchThread(handler, msg, props, context);
            } else {
                String contextList = "";

                synchronized (m_dispatchTable) {
                    Enumeration contexts = m_dispatchTable.keys();
                    while (contexts.hasMoreElements()) {
                        contextList += contexts.nextElement().toString();
                    }
                }

                m_listener.onTRException("Error no handler for message from " + source + " with process context: [" + context + "] Message: " + msg + ", Known contexts: " + contextList,
                        new Exception("No Handler found"));
                return;
            }
        }
    }

    private boolean handleTRMsg(Object msg) {
        TRMsg trmsg = null;
        if (msg instanceof TRMsg) {
            trmsg = ((TRMsg) msg);
        } else {
            return false;
        }

        //Check if this is a response:
        Integer correlation = (Integer) m_com.getProperties().get(TRAgent.TR_REQ_TRACKING);
        if (correlation != null) {
            TRRequest request = null;
            request = (TRRequest) m_pendingRequests.remove(correlation);

            if (request != null) {
                request.onResponse(trmsg);
            }
            //Check for a timed out launch success, if we get one issue a kill:
            else if (trmsg.getMessage().equals(TRAgent.LAUNCH_SUCCESS)) {
                Integer pid = (Integer) m_com.getProperties().get(TRAgent.PID);
                //The launch must have timed out
                m_listener.onTRException("Received late launch success, killing process: " + pid, null);
                try {
                    m_com.sendMessage(trmsg.getSource().toUpperCase(), new TRLaunchKill(), TRAgent.PID, pid);
                } catch (Exception e) {
                    m_listener.onTRException("Error killing process: " + pid, null);
                }
            } else {
                //The request must have timed out
                m_listener.onTRException(this + ": Received TR response with no outstanding request (probably request timed out): " + trmsg, null);
            }
            return true;
        } else if (msg instanceof TRDisplayMsg) {
            m_listener.onTROutput(msg);
            return true;
        }

        return false;
    }

    public TRProcessContext launchProc(String agent, TRLaunchDescr trld, ITRAgentComHandler handler) throws Exception {
        if (m_closed) {
            throw new Exception("ComHub is closed");
        }

        LaunchRequest req = new LaunchRequest(agent, trld, handler);
        req.sendRequest();
        req.join(m_launchTimeout);

        switch (req.m_status) {

        case TRRequest.SUCCESS: {
            return req.m_context;
        }
        case TRRequest.FAILURE: {
            throw new Exception("Failure launching " + trld.getRunName() + " on " + agent + " -- " + req.m_failure);
        }
        case TRRequest.PENDING:
        default: {
            req.cancel();
            throw new Exception("Timed out launching " + trld.getRunName() + " on " + agent);
        }
        }
    }

    public Integer killProc(TRProcessContext ctx) throws Exception {
        if (m_closed) {
            throw new Exception("ComHub is closed");
        }

        KillRequest req = new KillRequest(ctx);
        req.sendRequest();
        req.join(m_killTimeout);

        m_dispatchTable.remove(ctx.toString());

        switch (req.m_status) {
        case TRRequest.SUCCESS: {
            return ctx.getPid();
        }
        case TRRequest.FAILURE: {
            throw new Exception("Error killing process: " + req.m_failure);
        }
        case TRRequest.PENDING:
        default: {
            req.cancel();
            throw new Exception("ERROR: Timed out killing process: " + ctx);
        }
        }
    }

    /**
     * Broadcasts a message to be sent asynchronously to all entities whose
     * clientID is in recips and are connected to the Control broker.
     * 
     * @param content
     *            The object to send
     * @param recips
     * @exception Exception
     */
    public void broadCast(Object content, TRProcessContext[] procRecips) throws Exception {
        TRComHubBroadcastMetaMsg msg = new TRComHubBroadcastMetaMsg(content, procRecips);
        m_com.broadCast(msg, msg.getTRRecips(), null);
    }

    /**
     * Sends a message to the process with the given context:
     * 
     * @param ctx
     *            The process context to which to send
     * @param msg
     *            The Message.
     * @throws Exception
     *             If there is an error sending.
     */
    public void sendMessage(TRProcessContext ctx, Object msg) throws Exception {
        if (m_boundList.contains(ctx.getAgentID().toUpperCase())) {
            m_com.sendMessage(ctx.getAgentID(), msg, TRAgent.PID, ctx.getPid());
        } else {
            throw new Exception(this + ": " + ctx.getAgentID() + " is not bound");
        }
    }

    /**
     * Sends a message to the specified TRAgent.
     * 
     * @param agentID
     *            The agent id
     * @param message
     *            The message to send
     * @throws Exception
     *             If there is an error writing the message
     */
    public void sendMessage(String agentID, Object msg) throws Exception {
        m_com.sendMessage(agentID, msg);
    }

    private class DispatchThread implements Runnable {
        Thread m_dispatchThread;
        DispatchHolder m_holder = null;

        public DispatchThread(ITRAgentComHandler handler, Object msg, Hashtable props, TRProcessContext ctx) {
            m_holder = new DispatchHolder(handler, msg, props, ctx);

            m_dispatchThread = new Thread(this);
            m_dispatchThread.setName("DispatchThread--" + handler);
            m_dispatchThread.start();
        }

        public void run() {
            try {
                m_holder.m_handler.handleMessage(m_holder.m_msg, m_holder.m_props, m_holder.m_ctx);
            } catch (Throwable thrown) {
                m_listener.onTRException("Error during message dispatch to: " + m_holder.m_handler + ". Msg: " + m_holder.m_msg, thrown);
            }
        }
    }

    /*
     * private class DispatchThreadPool { final HashSet m_threads = new
     * HashSet();
     * 
     * final LinkedList m_dispatchList = new LinkedList();
     * 
     * boolean m_closed = false;
     * 
     * DispatchThreadPool() {
     * 
     * }
     * 
     * public synchronized void dispatch(ITRAgentComHandler handler, Object msg,
     * Hashtable props, TRProcessContext ctx) { if(m_freeThreads == 0) {
     * m_threads.add(new DispatchThread(this)); } }
     * 
     * public synchronized DispatchHolder getNextDispatchable(DispatchThread dt)
     * { while(!m_closed && !m_dispatchList.isEmpty()) { try { wait(); } catch
     * (InterruptedException e) { break; } }
     * 
     * if(!m_dispatchList.isEmpty()) { return (DispatchHolder)
     * m_dispatchList.removeFirst(); } }
     * 
     * public synchronized void close() { m_closed = true; }
     * 
     * 
     * }
     */

    private class DispatchHolder {
        final ITRAgentComHandler m_handler;
        final Object m_msg;
        final Hashtable m_props;
        final TRProcessContext m_ctx;

        DispatchHolder(ITRAgentComHandler handler, Object msg, Hashtable props, TRProcessContext ctx) {
            m_handler = handler;
            m_msg = msg;
            m_props = props;
            m_ctx = ctx;
        }
    }

    private abstract class TRRequest {
        static final int PENDING = 0;
        static final int SUCCESS = 1;
        static final int FAILURE = 2;

        protected int m_status = PENDING;

        private final Integer m_tracking;

        private final Object m_req;
        private final String m_agent;
        protected final Hashtable m_props = new Hashtable();

        TRRequest(String agent, Object req) {
            synchronized (m_requestSync) {
                m_tracking = new Integer(m_requestCounter++);
            }

            m_pendingRequests.put(m_tracking, this);
            m_props.put(TRAgent.TR_REQ_TRACKING, m_tracking);
            m_req = req;
            m_agent = agent;
        }

        final void sendRequest() throws Exception {
            m_com.sendMessage(m_agent, m_req, m_props);
        }

        synchronized final void join(long timeout) throws InterruptedException {
            long limit = System.currentTimeMillis() + timeout;
            long remaining = limit - System.currentTimeMillis();

            //Wait for launch response
            while (remaining > 0 && m_status == PENDING) {
                wait(remaining);
                remaining = limit - System.currentTimeMillis();
            }
        }

        synchronized final void onResponse(TRMsg msg) {
            handleResponse(msg);
            notifyAll();
        }

        abstract protected void handleResponse(TRMsg msg);

        void cancel() {
            m_pendingRequests.remove(m_tracking);
        }
    }

    private class LaunchRequest extends TRRequest {
        private final ITRAgentComHandler m_handler;
        private TRProcessContext m_context = null;
        private Object m_failure = null;

        LaunchRequest(String agent, TRLaunchDescr trld, ITRAgentComHandler handler) {
            super(agent, trld);
            m_handler = handler;
        }

        public void handleResponse(TRMsg msg) {
            if (msg.getMessage().equals(TRAgent.LAUNCH_SUCCESS)) {
                m_context = new TRProcessContext(msg.getSource().toUpperCase(), (Integer) m_com.getProperties().get(TRAgent.PID));
                //Register the handler with the new pid in the dispatch table:
                m_dispatchTable.put(m_context.toString(), m_handler);
                m_status = SUCCESS;
            } else if (msg.getMessage().equals(TRAgent.LAUNCH_FAILURE)) {
                m_status = FAILURE;
                m_failure = msg;
            } else {
                m_listener.onTRException("Unexpected launch response: " + msg, null);
                m_status = FAILURE;
                m_failure = msg;
            }
        }
    }

    private class KillRequest extends TRRequest {
        //private TRProcessContext m_context = null;
        private Object m_failure = null;

        KillRequest(TRProcessContext context) {
            super(context.getAgentID(), new TRLaunchKill());
            //m_context = context;
            m_props.put(TRAgent.PID, context.getPid());
        }

        public void handleResponse(TRMsg msg) {
            if (msg.getMessage().equals(TRAgent.KILL_SUCCESS)) {
                m_status = SUCCESS;
            } else {
                m_status = FAILURE;
                m_failure = msg;
            }
        }
    }
}