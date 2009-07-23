/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.testrunner.rmi;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * TRClient
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class TRClient implements Runnable {

    private static final boolean DEBUG = false;

    /**
     * Defines the interface used for an TRAgentComHub to route messages.
     * 
     * @author Colin MacNaughton
     * @since 2.0
     * @see TRAgentComHub
     */
    public interface TRClientListener {
        /**
         * Called when there is an asynchronous error:
         */
        public void onTRException(String reason, Throwable thrown);

        /**
         * Called when testrunner has information to output or when a TRMsg,
         * TRErrorMsg, or TRDisplayMsg is received from an agent. Applications
         * should check for instances of TRMsg types and act accordingly.
         */
        public void onTRInfo(String msg);
    }

    TRCommunicator m_com;

    TRClientListener m_listener;

    private Thread m_thread;

    //PID, handler pairs
    private final Hashtable m_dispatchTable = new Hashtable();

    private int m_requestCounter = 0;
    private final Object m_requestSync = new Object();
    private final Hashtable m_pendingRequests = new Hashtable();

    //List of bound agents:
    private final Vector m_boundList = new Vector();

    //Timeouts
    private long bindTimeout = 5000;
    private long launchTimeout = 1000;;
    private long killTimeout = 5000;;
    String m_controlURL;
    private boolean m_closed = false;
    private OrhanProcessListener orphanListener = new OrhanProcessListener();
    
    public TRClient(TRCommunicator controlCom) throws Exception {
        this.m_com = controlCom;
        m_com.connect();
        //        m_com.setTRComHandler(this);

        m_thread = new Thread(this);
        m_thread.setName("TRClient");
        m_thread.start();
    }

    public void setListener(TRClientListener listener) {
        m_listener = listener;
    }

    /**
     * @return the bindTimeout
     */
    public long getBindTimeout() {
        return bindTimeout;
    }

    /**
     * @param bindTimeout
     *            the bindTimeout to set
     */
    public void setBindTimeout(long bindTimeout) {
        this.bindTimeout = bindTimeout;
    }

    /**
     * @return the launchTimeout
     */
    public long getLaunchTimeout() {
        return launchTimeout;
    }

    /**
     * @param launchTimeout
     *            the launchTimeout to set
     */
    public void setLaunchTimeout(long launchTimeout) {
        this.launchTimeout = launchTimeout;
    }

    /**
     * @return the killTimeout
     */
    public long getKillTimeout() {
        return killTimeout;
    }

    /**
     * @param killTimeout
     *            the killTimeout to set
     */
    public void setKillTimeout(long killTimeout) {
        this.killTimeout = killTimeout;
    }

    /**
     * Broadcasts a message to be sent asynchronously to all entities whose
     * clientID is in recips and are connected to the Control broker.
     * 
     * @param content
     *            The object to send
     * @param propsTable
     *            Table of properties for each process
     * @param agents
     * @exception Exception
     */
    public void broadCast(TRProcessContext[] processes, Object content, Hashtable[] processProps) throws Exception {
        TRProcessBroadcastMetaMsg msg = new TRProcessBroadcastMetaMsg(content, processes);
        //Get the agentIDs to which to broadcast:
        String[] agentIDs = msg.getTRRecips();
        m_com.broadCast(msg, agentIDs);
    }

    /**
     * Sends a message to the process with the given context:
     * 
     * @param ctx
     *            The process context to which to send
     * @param message
     *            The Message.
     * @throws Exception
     *             If there is an error sending.
     */
    public void sendMessage(TRProcessContext ctx, Object message) throws Exception {
        TRMetaMessage msg = new TRMetaMessage(message);
        msg.setIntProperty(TRAgent.PID, ctx.getPid());
        m_com.sendMessage(msg, ctx.getAgentID());
    }

    public TRProcessContext launchProcess(String agent, TRLaunchDescr trld, ProcessListener handler) throws Exception {
        if (m_closed) {
            throw new IllegalStateException("closed");
        }

        agent = agent.toUpperCase();

        LaunchRequest req = new LaunchRequest(agent, trld, handler);
        req.sendRequest();
        req.join(launchTimeout);
        return req.getResponse();
    }

    public Integer killProcess(TRProcessContext ctx) throws Exception {
        if (m_closed) {
            throw new Exception("ComHub is closed");
        }

        KillRequest req = new KillRequest(ctx);
        req.sendRequest();
        req.join(killTimeout);
        return req.getResponse();
    }

    public synchronized void bindAgent(String agentName) throws Exception {
        if (m_closed) {
            throw new IllegalStateException("closed");
        }

        String agent = agentName.toUpperCase();

        if (m_boundList.contains(agent)) {
            return;
        }

        BindRequest req = new BindRequest(agent);
        req.sendRequest();
        req.join(bindTimeout);

    }

    public synchronized void releaseAgent(String agentName) throws Exception {
        if (m_closed) {
            throw new IllegalStateException("closed");
        }

        String agent = agentName.toUpperCase();
        if (!m_boundList.contains(agent)) {
            return;
        }

        ReleaseRequest req = new ReleaseRequest(agent);
        req.sendRequest();
        try {
            req.join(bindTimeout);
        } catch (Exception e) {
            m_boundList.remove(agentName);
        }
    }

    public synchronized void releaseAll() throws Exception {

        Vector failures = null;
        try {
            for (int i = m_boundList.size() - 1; i >= 0; i--) {

                String agent = (String) m_boundList.elementAt(i);
                try {
                    releaseAgent(agent);
                } catch (Exception e) {
                    if (failures == null) {
                        failures = new Vector();
                    }
                    failures.add(agent);
                }
            }
        } finally {
            m_dispatchTable.clear();
        }

        if (failures != null) {
            throw new Exception("Failed to release: " + failures);
        }
    }

    public void run() {
        while (!m_closed) {
            TRMetaMessage msg = null;

            try {
                msg = m_com.getMessage(1000);
                if (msg == null) {
                    continue;
                }

                dispatch(msg);
            } catch (Exception e) {
                m_listener.onTRException("Error trying to get TRMessage", e);
            }
        }
    }

    private void dispatch(TRMetaMessage msg) {

        //Check if this is a response:
        Integer correlation = (Integer) msg.getIntProperty(TRAgent.TR_REQ_TRACKING);
        if (correlation != null) {
            TRRequest request = null;
            request = (TRRequest) m_pendingRequests.remove(correlation);

            if (request != null) {
                request.onResponse(msg);
            }
            //Check for a timed out launch success, if we get one issue a kill:
            else if (TRAgent.COMMAND_LAUNCH.equalsIgnoreCase(msg.getProperty(TRAgent.PROP_COMMAND_RESPONSE))) {
                Integer pid = (Integer) msg.getIntProperty(TRAgent.PID);

                //The launch must have timed out
                m_listener.onTRException("Received late launch success, killing process: " + pid, null);
                //Send a kill:
                try {
                    TRProcessContext ctx = new TRProcessContext(msg.getSource(), pid);
                    new KillRequest(ctx).sendRequest();
                } catch (Exception e) {
                    m_listener.onTRException("Error killing process: " + pid, null);
                }
            } else {
                //The request must have timed out
                m_listener.onTRException(this + ": Received TR response with no outstanding request (probably request timed out): " + msg, null);
            }
            return;
        } else if (msg instanceof RMIRequest) {
            RMIRequest rmi = (RMIRequest) msg;
            switch (rmi.getTarget()) {
            case RMIRequest.CLIENT_PROC_LISTENER: {
                TRProcessContext ctx = (TRProcessContext) rmi.getArgs()[0];

                //Look up in the dispatch table see if we have a handler
                //for communication with this pid and agentname
                ProcessListener handler = (ProcessListener) m_dispatchTable.get(ctx);
                if (handler != null) {
                    if (DEBUG)
                        System.out.println("Creating DispatchThread for " + handler);
                    //handler.handleMessage(msg, props);
                    //TODO: Create a thread pool:
                    new DispatchThread(handler, rmi, ctx);
                } else {
                    invokeProcessHandler(orphanListener, rmi, ctx);
                    return;
                }
                return;
            }
            case RMIRequest.CLIENT: {
                new UnsupportedOperationException().printStackTrace();
                return;
            }
            default: {
                new UnsupportedOperationException().printStackTrace();
                return;
            }
            }
        } else {
            new UnsupportedOperationException().printStackTrace();
        }
    }

    public void close() {
        synchronized (this) {

            try {
                releaseAll();
            } catch (Exception e) {
                m_listener.onTRException("Error releasing agents.", e);
            }
            m_closed = true;
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

    private abstract class TRRequest {
        static final int PENDING = 0;
        static final int SUCCESS = 1;
        static final int FAILURE = 2;

        protected int m_status = PENDING;

        private final Integer m_tracking;

        protected final RMIRequest m_req;
        protected final String m_agent;
        protected Object failure;

        TRRequest(String agent, String commandID, Object[] args) {
            synchronized (m_requestSync) {
                m_tracking = new Integer(m_requestCounter++);
            }
            m_req = new RMIRequest(RMIRequest.AGENT, commandID, args);
            m_pendingRequests.put(m_tracking, this);
            m_req.setIntProperty(TRAgent.TR_REQ_TRACKING, m_tracking);
            m_req.setInternal(true);
            m_agent = agent;
        }

        final void sendRequest() throws Exception {
            m_com.sendMessage(m_req, m_agent);
        }

        synchronized final void join(long timeout) throws Exception {
            long limit = System.currentTimeMillis() + timeout;
            long remaining = limit - System.currentTimeMillis();

            //Wait for launch response
            while (remaining > 0 && m_status == PENDING) {
                try {
                    wait(remaining);
                } catch (InterruptedException ie) {
                    cancel();
                    Thread.currentThread().interrupt();
                    throw new Exception(this + " Interrupted", ie);

                }
                remaining = limit - System.currentTimeMillis();
            }

            switch (m_status) {

            case TRRequest.SUCCESS: {
                return;
            }
            case TRRequest.FAILURE: {
                throw new Exception(this + " Failure: " + failure);
            }
            case TRRequest.PENDING:
            default: {
                cancel();
                throw new Exception(this + " Timed Out.");
            }
            }

        }

        synchronized final void onResponse(TRMetaMessage msg) {
            Object ret;
            try {
                ret = msg.getContent();
                if (ret instanceof TRErrorMsg) {
                    failure = ((TRErrorMsg) ret).getException();
                    m_status = FAILURE;
                } else {
                    m_status = SUCCESS;
                    handleResponse(ret);
                }
            } catch (Exception e) {
                failure = e;
                m_status = FAILURE;
            }

            notifyAll();
        }

        abstract protected void handleResponse(Object response);

        void cancel() {
            m_pendingRequests.remove(m_tracking);
        }
    }

    private class BindRequest extends TRRequest {
        BindRequest(String agent) throws IOException {
            super(agent, TRAgent.COMMAND_BIND, new Object[] {});
        }

        public void handleResponse(Object response) {
            boolean ret = ((Boolean) response).booleanValue();
            if (ret) {
                m_boundList.addElement(m_agent);
            } else {
                m_status = FAILURE;
            }
        }

    }

    private class ReleaseRequest extends TRRequest {
        ReleaseRequest(String agent) throws IOException {
            super(agent, TRAgent.COMMAND_RELEASE, new Object[] {});
        }

        public void handleResponse(Object response) {
            boolean ret = ((Boolean) response).booleanValue();
            if (ret) {
                m_boundList.removeElement(m_agent);
            } else {
                m_status = FAILURE;
            }
        }

    }

    private class LaunchRequest extends TRRequest {
        private final ProcessListener m_handler;
        private TRProcessContext m_context = null;

        LaunchRequest(String agent, TRLaunchDescr trld, ProcessListener handler) throws IOException {
            super(agent, TRAgent.COMMAND_LAUNCH, new Object[] { trld });
            m_handler = handler;
        }

        public void handleResponse(Object response) {
            m_context = (TRProcessContext) response;
            m_status = SUCCESS;
            m_dispatchTable.put(m_context, m_handler);
        }

        public TRProcessContext getResponse() {
            return m_context;
        }
    }

    private class KillRequest extends TRRequest {
        private Integer exitCode;
        private TRProcessContext ctx;

        KillRequest(TRProcessContext context) throws IOException {
            super(context.getAgentID(), TRAgent.COMMAND_KILL, new Object[] { context });
            this.ctx = context;
        }

        public void handleResponse(Object response) {
            exitCode = (Integer) response;
            m_dispatchTable.remove(ctx);
        }

        public Integer getResponse() {
            return exitCode;
        }
    }

    private class DispatchThread implements Runnable {
        Thread m_dispatchThread;
        RMIRequest rmi = null;
        TRProcessContext ctx;
        ProcessListener handler;

        public DispatchThread(ProcessListener handler, RMIRequest rmi, TRProcessContext ctx) {
            this.rmi = rmi;
            this.ctx = ctx;
            this.handler = handler;

            m_dispatchThread = new Thread(this);
            m_dispatchThread.setName("DispatchThread--" + handler);
            m_dispatchThread.start();
        }

        public void run() {
            invokeProcessHandler(handler, rmi, ctx);
        }
    }

    //TODO: Fix this to use something else
    private final void invokeProcessHandler(ProcessListener handler, RMIRequest rmi, TRProcessContext ctx) {
        try {
            Object[] args = rmi.getArgs();
            if (rmi.getMethod().equals("handleError")) {
                handler.handleError(ctx, (String) args[1], (Throwable) args[2]);
            } else if (rmi.getMethod().equals("handleMessage")) {
                handler.handleMessage(ctx, args[1]);
            } else if (rmi.getMethod().equals("handleProcessInfo")) {
                handler.handleProcessInfo(ctx, (String) args[1]);
            } else if (rmi.getMethod().equals("handleSystemErr")) {
                handler.handleSystemErr(ctx, (String) args[1]);
            } else if (rmi.getMethod().equals("handleSystemOut")) {
                handler.handleSystemOut(ctx, (String) args[1]);
            } else if (rmi.getMethod().equals("processDone")) {
                handler.processDone(ctx, ((Integer) args[1]).intValue());
            } else {
                throw new UnsupportedOperationException("Unsupported ProcessListener method: " + rmi.getMethod());
            }
        } catch (Throwable thrown) {
            m_listener.onTRException("Error during message dispatch to: " + handler, thrown);
        }
    }

    private class OrhanProcessListener implements ProcessListener {
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ProcessListener#handleError(org.fusesource
         * .testrunner.TRProcessContext, java.lang.String, java.lang.Throwable)
         */
        public void handleError(TRProcessContext ctx, String message, Throwable thrown) {
            m_listener.onTRException("Error from orphanned process: [" + ctx + "] Message: " + message,
                    new Exception("No Handler found"));

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ProcessListener#handleMessage(org.fusesource
         * .testrunner.TRProcessContext, java.lang.Object)
         */
        public void handleMessage(TRProcessContext ctx, Object msg) {
            m_listener.onTRException("Error no handler for message with process context: [" + ctx + "] Message: " + msg,
                    new Exception("No Handler found"));

        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.fusesource.testrunner.ProcessListener#handleProcessInfo(org.
         * fusesource.testrunner.TRProcessContext, java.lang.String)
         */
        public void handleProcessInfo(TRProcessContext ctx, String info) {
            m_listener.onTRInfo(info);

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ProcessListener#handleSystemErr(org.fusesource
         * .testrunner.TRProcessContext, java.lang.String)
         */
        public void handleSystemErr(TRProcessContext ctx, String err) {
            m_listener.onTRInfo(err);

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ProcessListener#handleSystemOut(org.fusesource
         * .testrunner.TRProcessContext, java.lang.String)
         */
        public void handleSystemOut(TRProcessContext ctx, String output) {
            m_listener.onTRInfo(output);

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.fusesource.testrunner.ProcessListener#processDone(org.fusesource
         * .testrunner.TRProcessContext, int)
         */
        public void processDone(TRProcessContext ctx, int exitCode) {
            //Ignore.
        }

    }

}
