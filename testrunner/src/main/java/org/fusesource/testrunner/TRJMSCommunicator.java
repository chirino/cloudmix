/*
 * Copyright (c) 1999, 2000, 2001 Sonic Software Corporation. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Progress Software Corporation.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Progress.
 *
 * PROGRESS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. PROGRESS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

/*
 * public class TRJMSCommunicator
 *
 *	Method that encapsulates communication between a TestRunnerAgent and another
 *  object using JMS.
 *
 *	author: Colin MacNaughton (cmacnaug@progress.com)
 *	Date: 02/01/01
 */

package org.fusesource.testrunner;

import javax.jms.*;

import java.util.Hashtable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;

/**
 * TRJMSCommunicator facilitate communication between TestRunner Agents and
 * their potential controllers using a Sonic control broker. Both the agents and
 * the controllers must have the necessary Sonic jars in their classpath to talk
 * to the control broker. When an entity is done using a TRJMSCommunicator it
 * should call the close method.<br>
 * <br>
 * 
 * This communicator provides the ability to send (Serializable) object messages
 * with properties that can be retrieved by the recipient by calling
 * getProperties(). Controllers and agents must specify a clientID which will be
 * used as to uniquely identify them as TestRunner entities for a particular
 * control broker. The source (clientID) of a message received can be obtained
 * by calling getSource().<br>
 * <br>
 * 
 * The communicator also provides the ability to bind and release another entity
 * for exclusive communication using the bind and release or releaseAll methods.
 * An entity can ping another entity to check if it is connected using the ping
 * method.
 * 
 * The communicator allows a classLoader to be set (or removed). If a
 * classloader is set it will be used to read received objects.
 * 
 * @author Colin MacNaughton (cmacnaug)
 * @version 1.0
 * @since 1.0
 * @see TRAgent
 * @see ITRBindListener
 * @see ITRAsyncMessageHandler
 */
public class TRJMSCommunicator implements javax.jms.MessageListener, javax.jms.ExceptionListener, TRCommunicator {
    private static boolean DEBUG = false;
    private static final boolean devDebug = false;
    public static final String FACTORY_PROP = "testrunner.jms.provider";
    //Control Broker Variables:
    protected static final String TOPIC_PREFIX = "TESTRUNNER.";
    protected static final String BROADCAST_TOPIC_POSTFIX = "BROADCAST";
    private static final long RETRY_TIMEOUT = 30000; //30s

    //Internal Msg Property Headers:
    private static final String PING = "Ping";
    private static final String CLIENT_ID = "clientID";
    private static final String VERSION = "Version";

    protected static final String CF_USER = "Administrator";
    protected static final String CF_PASSWORD = "Administrator";
    private TopicConnectionFactory m_factory;
    private TopicConnection m_connection;

    //JMS objects to be used for messages sent by the owner of this object:
    private TopicSession m_sendSession;
    private TopicSession m_rcvSession;
    private TopicSession m_asyncRcvSession;
    private TopicPublisher m_publisher;
    private TopicSubscriber m_subscriber;
    private TopicSubscriber m_broadCastSubscriber;

    //JMS objects used to listen for internal messages used for bind/release and pings
    private TopicSession m_internalSession;
    private TopicSubscriber m_internalSubscriber;

    //JMS objects for making internal requests:
    private TopicSession m_requestSession;
    private TopicPublisher m_requestPublisher;
    private TopicSubscriber m_requestSubscriber;

    //JMS objects for making internal replies:
    private TopicSession m_replySession;
    private TopicPublisher m_replyPublisher;

    private String m_clientID; //The (unique) clientID
    private String m_hostAddress; //The address of the host to which to connect
    private Object handleMessageLock; //To synchronize handleMessage() called on synchronous and asynchronous message receipt
    private Object requestLock; //To serialize requests
    private Object replyLock; //To serialize reply
    private Object m_sendLock; //To serialize sends
    private Object m_getMessageLock; //To protect the receive session.
    private BroadCastListener m_broadCastListener;

    /**
     * Constructor
     * 
     * @param hostAddress
     *            The address of the Sonic control broker
     * @param clientID
     *            The (unique) clientID for this entity, a JMSException is
     *            thrown if another entity connected to the control broker is
     *            already using the clientID. The clientID will be used by other
     *            entities to send messages to the owner of this
     *            TRJMSCommunicator
     * @param bindListener
     *            If this entity wishes other agents to be able to bind it this
     *            must be non null.
     * @exception java.lang.Exception
     *                If there is a general error starting communication
     * @exception javax.jms.JMSSecurityException
     *                If there is a Security Error connecting to the broker
     * @exception javax.jms.JMSException
     *                If there is a JMS error connecting to the broker
     */

    public TRJMSCommunicator(String hostAddress, String clientID)

    throws Exception, javax.jms.JMSSecurityException, javax.jms.JMSException {

        m_hostAddress = hostAddress;
        m_clientID = clientID == null ? null : clientID.toUpperCase();
        m_broadCastListener = new BroadCastListener(new TRComHandler() {

            public void handleMessage(TRMetaMessage obj) {
                System.err.println("No broadcast listener set ... ignoring: " + obj);
            }
        });

        handleMessageLock = new Object();
        requestLock = new Object();
        replyLock = new Object();
        m_sendLock = new Object();
        m_getMessageLock = new Object();
    }

    /**
     * Sets a handler for asynchronous messages from the communicator
     * 
     * @param handler
     */
    public void setTRComHandler(TRComHandler handler) {
        m_broadCastListener.m_messageHandler = handler;
    }

    private void setupConnection() throws Exception {
        synchronized (m_getMessageLock) {
            m_connection = null;
            while (m_connection == null) {
                try {
                    System.out.println(m_clientID + ": Connecting to TestRunner Control Broker: " + m_hostAddress);
                    //Get a TopicConnectionFactory with the connectID set to user.name
                    createFactory();
                    m_connection = m_factory.createTopicConnection();
                } catch (javax.jms.JMSException jmse) {
                    System.out.println("Unable to connect to " + m_hostAddress + "(" + jmse.getMessage() + "). Will try again in " + RETRY_TIMEOUT / 1000 + " seconds.");
                    Thread.sleep(RETRY_TIMEOUT);
                }
            }
            m_connection.setClientID(m_clientID);

            //Set a connection listener
            m_connection.setExceptionListener((javax.jms.ExceptionListener) this);

            //Set up sending and receiving sessions:
            m_sendSession = m_connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            m_rcvSession = m_connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            m_asyncRcvSession = m_connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            //Create publisher to testrunner topic:
            //Leave it unbound so we can decide where to send to at the time we are sending:
            m_publisher = m_sendSession.createPublisher(null);

            //Create subscriber (with no local set) to listen listen for messages intended for
            //the owner of this JMSCommunicator:
            Topic topic = null;
            topic = m_rcvSession.createTopic(TOPIC_PREFIX + m_clientID);
            m_subscriber = m_rcvSession.createSubscriber(topic, null, true);

            //Create a subscriber to listen for message broadCast to all entities communicating
            //On this server:
            topic = m_asyncRcvSession.createTopic(TOPIC_PREFIX + BROADCAST_TOPIC_POSTFIX);
            m_broadCastSubscriber = m_asyncRcvSession.createSubscriber(topic, null, true);
            m_broadCastSubscriber.setMessageListener(m_broadCastListener);

            //This internal session is to be used only from the onMessage thread:
            m_internalSession = m_connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            //Set an asynchrnous subscriber to deal with internal requests (bind, reply and release):
            if (m_broadCastListener != null) {
                topic = m_internalSession.createTopic(TOPIC_PREFIX + m_clientID + ".internal");
                m_internalSubscriber = m_internalSession.createSubscriber(topic, null, true);
                m_internalSubscriber.setMessageListener(this);
            }

            //Create a session for requests:
            m_requestSession = m_connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            m_requestPublisher = m_requestSession.createPublisher(null);
            Topic replyTopic = m_requestSession.createTopic(TOPIC_PREFIX + m_clientID + ".reply");
            m_requestSubscriber = m_requestSession.createSubscriber(replyTopic, null, true);

            //Create a session for replies:
            m_replySession = m_connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            m_replyPublisher = m_replySession.createPublisher(null);

            //Start the connection:
            m_connection.start();
            System.out.println(m_clientID + ": Connection to " + m_hostAddress + " established.");
        }
    }

    private TopicConnectionFactory createFactory() throws Exception {
        if (m_factory == null) {
            //Instantiate the connection factory:
            String factoryName = System.getProperty(FACTORY_PROP, "progress.message.jclient.TopicConnectionFactory");
            if (factoryName.startsWith("progress.message")) {
                Constructor c = Class.forName(factoryName).getConstructor(new Class[] { String.class, String.class, String.class });
                m_factory = (TopicConnectionFactory) c.newInstance(new Object[] { m_hostAddress, CF_USER, CF_PASSWORD });
            }
            if (factoryName.startsWith("org.apache.activemq")) {
                Constructor c = Class.forName(factoryName).getConstructor(new Class[] { String.class, String.class, String.class });
                m_factory = (TopicConnectionFactory) c.newInstance(new Object[] { CF_USER, CF_PASSWORD, m_hostAddress });
            } else {
                //Try for a constructor taking a connect url:
                Constructor c = Class.forName(factoryName).getConstructor(new Class[] { String.class });
                m_factory = (TopicConnectionFactory) c.newInstance(new Object[] { m_hostAddress });
            }
        }

        return m_factory;

    }

    /**
     * Should be called when this object's owner is finished using it.
     */

    public void close() throws Exception {
        String errorReason = "";
        boolean errorFlag = false;

        if (DEBUG || devDebug)
            System.out.println(m_clientID + ": Terminating JMS communication.");
        try {
            if (m_connection != null) {
                m_connection.close();
                m_connection = null;
            }
        } catch (Exception e) {
            errorFlag = true;
            errorReason += "ERROR: Shutting down JMS.";
        }

        if (errorFlag)
            throw new Exception(errorReason);
    }

    /**
     * Gets a message intended for the owner of this.
     * 
     * @param timeout
     *            The amount of time to wait
     * @return An object or null if timed out.
     */
    /*
     * public void getMessage(long timeout)
     * 
     * * Synchronously receives messages
     */
    public TRMetaMessage getMessage(long timeout) throws Exception {
        synchronized (m_getMessageLock) {
            long time = System.currentTimeMillis();

            //Return null if we've timed out.
            if (timeout <= 0) {
                return null;
            }

            if (DEBUG || devDebug)
                System.out.println("Waiting for message on " + m_subscriber.getTopic().getTopicName());
            Message msg = m_subscriber.receive(timeout);

            //If we got no message return null.
            if (msg == null) {
                return null;
            }

            //Check the message version this is the right version
            checkVersionMatch(msg);

            if (DEBUG || devDebug)
                System.out.println("Calling handle message from getMessage");
            TRMetaMessage trMsg = null;

            trMsg = handleMessage(msg);

            //            obj = (Object) ((TRMetaMessage) obj).getContent(classLoader);
            //            if (DEBUG || devDebug)
            //                System.out.println("Meta message content is " + (obj == null ? "null" : obj.getClass().getName()));
            //
            //            if (obj instanceof TRDisplayMsg && !sendDisplayObjs) {
            //                System.out.println(obj);
            //            } else {
            //                if (DEBUG || devDebug)
            //                    System.out.println("" + m_source + ": " + obj);
            //                if (DEBUG || devDebug)
            //                    System.out.println("End synch on boundFlagLock in handleMessage (returning msg)");
            //                m_source = source;
            //                return obj;
            //            }

            if (trMsg != null) {
                return trMsg;
            }

            //Otherwise call recursively with the remaining time left:
            return getMessage(timeout - (System.currentTimeMillis() - time));
        }
    }

    private TRMetaMessage handleMessage(javax.jms.Message msg) throws Exception {
        if (DEBUG || devDebug)
            System.out.println("Got message");
        String source = null;

        synchronized (handleMessageLock) {

            source = msg.getStringProperty(CLIENT_ID);

            //If this is a valid JMSCommunicator message for this client
            //(or for all clients) and this Communicator is either not bound
            //or the message is from its master then:
            //return the object or
            //display it if it is a DisplayObj and keep waiting for a non-display
            //message:
            if (DEBUG || devDebug)
                System.out.println("Synched on boundFlagLock in handleMessage");
            if (source != null && msg instanceof BytesMessage) {
                BytesMessage bMessage = (BytesMessage) msg;
                if (devDebug)
                    System.out.println("Got: " + msg);
                Object obj = null;
                byte[] classBytes = new byte[(int) bMessage.getBodyLength()];
                bMessage.readBytes(classBytes);

                // the wrapped object is serialized inside of a TRMetaMessage, therefore, no special class loader is needed.
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(classBytes));
                try {
                    obj = ois.readObject();
                } finally {
                    ois.close();
                }

                if (obj == null) {
                    if (DEBUG || devDebug)
                        System.out.println(this + "Read null.");
                    return null;
                } else {
                    if (DEBUG || devDebug)
                        System.out.println(this + " Read a " + obj.getClass().getName());
                }

                //Check to see that this is a test runner message:
                if (!(obj instanceof TRMetaMessage)) {
                    return null;
                }

                TRMetaMessage ret = (TRMetaMessage) obj;
                if (obj instanceof TRBroadCastMetaMessage) {
                    String[] recips = (String[]) ((TRBroadCastMetaMessage) obj).m_recips;
                    boolean isRecip = false;

                    //We don't break when we find that we are in the recipient
                    //list instead we traverse the whole array for fairness (so
                    //that the order of the clientID in the list doesn't matter
                    for (int i = 0; i < recips.length; i++) {
                        if (recips[i].equalsIgnoreCase(m_clientID)) {
                            isRecip = true;
                        }
                    }
                    //If we are not a recipient return null
                    if (!isRecip)
                        return null;

                    ret = new TRMetaMessage(ret.getContentBytes(), (Hashtable) ((TRBroadCastMetaMessage) obj).getProperties().get(m_clientID));
                }

                ret.setSource(source);

                return ret;
            } else {
                if (DEBUG || devDebug)
                    System.out.println("Received a message for another entity from " + source);
            }
            return null;
        }//synchronized(handleMessageLock)
    }

    private void checkVersionMatch(Message msg) throws TRVersionMismatchException {
        try {
            String source = msg.getStringProperty(CLIENT_ID);
            String version = msg.getStringProperty(VERSION);

            //Check for correct version: (If we are bound the check was done at bind time)
            if (version != null) {
                if (!version.equals(Version.getVersionString())) {
                    if (source != null) {
                        internalSend("Version mismatch: " + Version.getVersionString() + " / " + version, source);
                    }
                    throw new TRVersionMismatchException("Version mismatch: source (" + source + ") version: " + version + " this version: " + Version.getVersionString());
                }
                return;
            } else {
                if (source != null) {
                    internalSend("Poorly formatted message, VERSION not specified. Potential TestRunner version mismatch or message from non TestRunner entity", source);
                }
                throw new TRVersionMismatchException("Poorly formatted message, VERSION not specified. Potential TestRunner version mismatch.");
            }
        } catch (TRVersionMismatchException trvme) {
            throw trvme;
        } catch (Exception e) {
            throw new TRVersionMismatchException("ERROR: checking message version.");
        }
    }

    /**
     * Used internally
     * 
     * An asynchronous listener on the communicator's 'internal topic' The
     * internal topic is used To pass non-application specific message between
     * JMSCommunicators such as bind and release requests.
     * 
     * @param msg
     */

    public void onMessage(Message msg) {
        if (DEBUG || devDebug)
            System.out.println("Received internal message.");
        try {
            String source = msg.getStringProperty(CLIENT_ID);

            //Check the message version this is the right version
            try {
                checkVersionMatch(msg);
            } catch (TRVersionMismatchException trvme) {
                System.out.println(trvme.getMessage());
                return;
            }

            //Check to see if this is a ping request for this clientID:
            if (msg.getStringProperty(PING) != null) //Is a message about a bind:
            {
                //Always acknowledge a ping:
                if (DEBUG || devDebug)
                    System.out.println(m_clientID + ": acknowledging ping request.");
                this.reply(msg, source, PING, "reply", new Boolean(true));
                return;
            }
        } catch (JMSException jmse) {
            System.out.println(m_clientID + "JMSCommunicator: ERROR: internal communication error handling message. Ceasing communication");
            jmse.printStackTrace();
        } catch (Exception e) {
            System.out.println(m_clientID + "JMSCommunicator: ERROR: internal error handling message.");
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.testrunner.TRCommunicator#broadCast(org.fusesource.testrunner
     * .TRMetaMessage, java.lang.String[])
     */
    public void broadCast(TRMetaMessage msg, String[] agentIDs) throws Exception {
        sendMessage(new TRBroadCastMetaMessage(msg, agentIDs, null), BROADCAST_TOPIC_POSTFIX);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.testrunner.TRCommunicator#connect()
     */
    public void connect() throws Exception {
        setupConnection();
    }

    private void internalSend(Object obj, String recipient) throws Exception {
        sendMessage(new TRMetaMessage(obj), recipient);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.fusesource.testrunner.TRCommunicator#sendMessage(org.fusesource.
     * testrunner.TRMetaMessage, java.lang.String)
     */
    public void sendMessage(TRMetaMessage trMsg, String recipient) throws Exception {
        trMsg.setSource(m_clientID);
        BytesMessage msg = m_sendSession.createBytesMessage();

        //Always set a property with out unique clientID
        //So that the entity we are sending to knows that it is us
        msg.setStringProperty(CLIENT_ID, m_clientID);
        msg.setStringProperty(VERSION, Version.getVersionString());

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteStream);
        oos.writeObject(trMsg);
        oos.flush();
        byte[] objectBytes = byteStream.toByteArray();
        msg.writeBytes(objectBytes);

        Topic topic = m_sendSession.createTopic(TOPIC_PREFIX + recipient.toUpperCase());
        synchronized (m_sendLock) {
            if (DEBUG || devDebug)
                System.out.println("Sending message on " + topic.getTopicName());
            m_publisher.publish(topic, msg, DeliveryMode.PERSISTENT, 0, 0);

            if (DEBUG || devDebug)
                System.out.println("Finished Sending message on " + topic.getTopicName());

            if (devDebug) {
                System.out.println("Sent: " + msg);
                //                System.out.println("Sent: " + objectBytes.length + ": " + HexSupport.toHexFromBytes(objectBytes));
            }
        }
    }

    /**
     * Ping another TRJMSCommunicator
     * 
     * @param recipient
     *            The clientID of the entity to ping
     * @param timeout
     *            The time to wait for a reply.
     * @return true if the entity being pinged replies, false otherwise.
     * @exception JMSException
     * @exception MessageFormatException
     * @exception MessageNotWriteableException
     */
    public boolean ping(String recipient, long timeout) throws JMSException, MessageFormatException, MessageNotWriteableException {
        if (DEBUG || devDebug)
            System.out.println(m_clientID + ": pinging " + recipient);
        if (request(recipient, PING, "request", timeout)) {
            if (DEBUG || devDebug)
                System.out.println(recipient + " acknowledged ping.");
            return true;
        }

        return false;
    }

    /*
     * private synchronized boolean request(String recipient, String property,
     * String value, long timeout)
     * 
     * Generates a request for the intended recipient defined by property and
     * value.
     */
    private boolean request(String recipient, String property, String value, long timeout) {
        if (DEBUG || devDebug)
            System.out.println("In request (" + recipient + " - " + property + ")");
        synchronized (requestLock) {
            try {
                ObjectMessage msg = m_requestSession.createObjectMessage();
                msg.setStringProperty(property, value);
                msg.setStringProperty(CLIENT_ID, m_clientID);
                msg.setStringProperty("JMSReplyTo", TOPIC_PREFIX + m_clientID + ".reply");
                msg.setStringProperty(VERSION, Version.getVersionString());
                msg.setLongProperty("TimeOut", timeout);

                Topic sendTopic = m_requestSession.createTopic(TOPIC_PREFIX + recipient.toUpperCase() + ".internal");
                if (DEBUG || devDebug)
                    System.out.println("About to request - " + property + " on " + sendTopic.getTopicName());
                m_requestPublisher.publish(sendTopic, msg);
                if (DEBUG || devDebug)
                    System.out.println("Finished.");

                //Wait for reply:
                if (DEBUG || devDebug)
                    System.out.println("About to receive on " + m_requestSubscriber.getTopic().getTopicName());
                ObjectMessage reply = null;
                if (timeout > 0) {
                    reply = (ObjectMessage) m_requestSubscriber.receive(timeout);
                } else {
                    reply = (ObjectMessage) m_requestSubscriber.receiveNoWait();
                }
                if (reply == null) {
                    if (DEBUG || devDebug)
                        System.out.println(m_clientID + ": ERROR: timed out attempting to " + property + " " + recipient);
                    return false;
                }

                String source = reply.getStringProperty(CLIENT_ID);

                boolean theReply = false;
                if (source.equalsIgnoreCase(recipient) && ((Boolean) reply.getObject()).booleanValue() == true) {
                    theReply = true;
                }

                if (DEBUG || devDebug)
                    System.out.println(m_clientID + ": received " + property + " acknowledgement. (" + theReply + ")");
                return theReply;
            } catch (Exception e) {
                System.out.println(m_clientID + ": ERROR: requesting " + property + " from " + recipient);
                e.printStackTrace();
            }
            return false;
        }
    }

    private void reply(Message msg, String source, String property, String value, Object object) {
        if (DEBUG || devDebug)
            System.out.println("In reply (" + source + " - " + property + ")");
        synchronized (replyLock) {
            try {
                ObjectMessage reply = m_replySession.createObjectMessage();
                reply.setStringProperty(CLIENT_ID, m_clientID);
                reply.setStringProperty(property, value);
                reply.setObject((java.io.Serializable) object);

                Topic replyTopic = m_replySession.createTopic(msg.getStringProperty("JMSReplyTo"));
                m_replyPublisher.publish(replyTopic, reply);

            } catch (javax.jms.JMSException jmse) {
                System.out.println("ERROR: replying to " + property + " request from " + source);
                jmse.printStackTrace();
            }
            return;
        }
    }

    /**
     * @param val
     *            Sets whether this communicator prints its debug output to
     *            System.out.
     */
    /*
     * public void setDebug(boolean val)
     */
    public void setDebug(boolean val) {
        if (val)
            System.out.println("Setting DEBUG = " + val);
        DEBUG = val;
    }

    private class BroadCastListener implements javax.jms.MessageListener {
        TRComHandler m_messageHandler;

        public BroadCastListener(TRComHandler messageHandler) {
            m_messageHandler = messageHandler;
        }

        public void onMessage(javax.jms.Message message) {
            if (m_messageHandler != null) {
                if (DEBUG || devDebug)
                    System.out.println("Calling handle message from BroadCastListener");

                if (DEBUG || devDebug)
                    System.out.println("Received broadcast message.");
                try {
                    TRMetaMessage obj = handleMessage(message);

                    if (obj != null) {
                        if (DEBUG || devDebug)
                            System.out.println("Msg null or not for us.");

                        //Check the message version this is the right version
                        try {
                            checkVersionMatch(message);
                        } catch (TRVersionMismatchException trvme) {
                            System.out.println(trvme.getMessage());
                            return;
                        }
                        m_messageHandler.handleMessage(obj);
                    }
                } catch (Throwable thrown) {
                    System.out.println("ERROR: handling broadcast message.");
                    thrown.printStackTrace();
                    try {
                        internalSend(new TRErrorMsg("ERROR: handling broadcast message.", thrown), (String) message.getStringProperty(CLIENT_ID));
                    } catch (Exception ex2) {
                        System.out.println("Error sending error to source.");
                        ex2.printStackTrace();
                    }
                    return;
                }
            }
        }
    }

    /**
     * Handle asynchronous problem with the connection. (as specified in the
     * javax.jms.ExceptionListener interface).
     */
    public void onException(javax.jms.JMSException jsme) {
        System.out.println("\n\nThere is a problem with the connection.");
        System.out.println("   JMSException: " + jsme.getMessage());

        // See if the error is a dropped connection. If so, try to reconnect.
        // NOTE: the test is against Progress SonicMQ error codes.
        //int dropCode = progress.message.jclient.ErrorCodes.ERR_CONNECTION_DROPPED;
        //if (progress.message.jclient.ErrorCodes.testException(jsme, dropCode))
        {
            System.err.println("Please wait while the application tries to " + "re-establish the connection...");

            if (m_connection != null) {
                try {
                    m_connection.close();
                } catch (JMSException e) {
                    System.err.println("Error closing failed connection!");
                    e.printStackTrace();
                }
            }

            // Reestablish the connection
            try {
                setupConnection();
            } catch (Exception e) {
                System.out.println("ERROR: Unable to reconnect to control broker " + m_hostAddress);
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
