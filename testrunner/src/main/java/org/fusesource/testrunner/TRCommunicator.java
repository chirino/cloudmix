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
package org.fusesource.testrunner;

/**
 * TRCommunicator
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public interface TRCommunicator {

    /**
     * Classes that wish to receive BroadCast messages asynchronously from a
     * JMSCommunicator must implement this interface.
     * 
     * @author Colin MacNaughton (cmacnaug@progress.com)
     * @version 1.0
     * @since 1.0
     * @see TRJMSCommunicator
     */
    public interface TRComHandler {
        /**
         * This callback is called by a TRCommunicator upon receipt of a message
         * 
         * @param obj
         */
        public void handleMessage(TRMetaMessage obj);
    }

    /**
     * Sets a handler for asynchronous messages from the communicator
     * @param handler
     */
    public void setTRComHandler(TRComHandler handler);

    /**
     * Pings the specified agent.
     * 
     * @param timeout
     *            The amount of time to wait for a reply.
     * @return true if a response was received in the allotted time.
     */
    public boolean ping(String agentID, long timeout) throws Exception;

    /**
     * Gets the next message from the communicator
     * 
     * @param timeout
     *            The timeout.
     * @return The next message or null, if none arrived within the timeout
     */
    public TRMetaMessage getMessage(long timeout) throws Exception;

    /**
     * Broadcasts a message to be sent asynchronously to all entities whose
     * clientID is in recips and are connected to the Control broker.
     * 
     * @param content
     *            The object to send
     * @param agentIDs
     *            List of agent names to which to broadcast
     */
    public void broadCast(TRMetaMessage msg, String[] agentIDs) throws Exception;

    /**
     * Sends a message with properties set
     * 
     * @param agentID
     *            The id of the intended recipient
     * @param content
     *            The object to send
     * @param props
     *            The properties to send
     */
    public void sendMessage(TRMetaMessage msg, String agentID) throws Exception;

    /**
     * Connects the communicator to the controller.
     */
    public void connect() throws Exception;

    /**
     * Close the communicator. Once closed it can no longer be used.
     */
    public void close() throws Exception;
}
