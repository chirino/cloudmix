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
import java.util.Iterator;
import java.util.Vector;
import java.util.Date;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedOutputStream;
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
public class TRJMSCommunicator implements javax.jms.MessageListener, javax.jms.ExceptionListener {
    private static boolean DEBUG = false;
    private static final boolean devDebug = false;
    private static final String FACTORY_PROP = "testrunner.jms.provider";
    //Control Broker Variables:
    protected static final String TOPIC_PREFIX = "TESTRUNNER.";
    protected static final String BROADCAST_TOPIC_POSTFIX = "BROADCAST";
    private static final long RETRY_TIMEOUT = 30000; //30s

    //Internal Msg Property Headers:
    private static final String BIND = "Bind";
    private static final String PING = "Ping";
    private static final String RELEASE = "Release";
    private static final String CLIENT_ID = "clientID";
    private static final String VERSION = "Version";

    protected static final String SONIC_ADMIN_USER = "Administrator";
    protected static final String SONIC_ADMIN_PASSWORD = "Administrator";
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

    private String m_source; //The clientID of the last message received.
    private Hashtable m_props; //The properties of the last message received.
    private String m_clientID; //The (unique) clientID
    private String m_hostAddress; //The address of the host to which to connect
    private boolean boundFlag; //If set bound to communicate with one other entity:
    private Object boundFlagLock; //To synchronize access to boundFlag
    private Object handleMessageLock; //To synchronize handleMessage() called on synchronous and asynchronous message receipt
    private Object requestLock; //To serialize requests
    private Object replyLock; //To serialize reply
    private Object m_sendLock; //To serialize sends
    private Object m_getMessageLock; //To protect the receive session.
    private String master; //...and the entities client ID is this
    private Vector boundList;
    private ITRBindListener m_bindListener;
    private BroadCastListener m_broadCastListener;
    private boolean sendDisplayObjs = false; //If set to true DisplayObjects are sent instead of being directly displayed on screen
    private Hashtable m_classLoaders;
    private TRClassLoader m_defaultClassLoader;

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
     * @param asyncHandler
     *            If this entity wishes to listen for broadCasted messages this
     *            must be non-null. See broadCast(..)
     * @exception java.lang.Exception
     *                If there is a general error starting communication
     * @exception javax.jms.JMSSecurityException
     *                If there is a Security Error connecting to the broker
     * @exception javax.jms.JMSException
     *                If there is a JMS error connecting to the broker
     */

    public TRJMSCommunicator(String hostAddress, String clientID, ITRBindListener bindListener, ITRAsyncMessageHandler asyncHandler)

    throws Exception, javax.jms.JMSSecurityException, javax.jms.JMSException {

        m_hostAddress = hostAddress;
        m_clientID = clientID == null ? null : clientID.toUpperCase();
        m_bindListener = bindListener;
        m_broadCastListener = new BroadCastListener(asyncHandler);

        boundList = new Vector();
        boundFlag = false;
        boundFlagLock = new Object();
        m_classLoaders = new Hashtable(); // key = pid; value = TRClassLoader
        handleMessageLock = new Object();
        requestLock = new Object();
        replyLock = new Object();
        m_sendLock = new Object();
        m_getMessageLock = new Object();
        setupConnection();
        m_defaultClassLoader = new TRClassLoader("");
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
    
    private TopicConnectionFactory createFactory() throws Exception
    {
        if(m_factory == null)
        {
            //Instantiate the connection factory:
            String factoryName = System.getProperty(FACTORY_PROP, "progress.message.jclient.TopicConnectionFactory");
            if(factoryName.startsWith("progress.message"))
            {
                Constructor c = Class.forName(factoryName).getConstructor(new Class[] {String.class, String.class, String.class});
                m_factory = (TopicConnectionFactory) c.newInstance(new Object [] {m_hostAddress, SONIC_ADMIN_USER, SONIC_ADMIN_PASSWORD});
            }
            if(factoryName.startsWith("org.apache.activemq"))
            {
                Constructor c = Class.forName(factoryName).getConstructor(new Class[] {String.class, String.class, String.class});
                m_factory = (TopicConnectionFactory) c.newInstance(new Object [] {SONIC_ADMIN_USER, SONIC_ADMIN_PASSWORD, m_hostAddress});
            }
            else
            {
                //Try for a constructor taking a connect url:
                Constructor c = Class.forName(factoryName).getConstructor(new Class[] {String.class});
                m_factory = (TopicConnectionFactory) c.newInstance(new Object [] {m_hostAddress});
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

        // close all class loaders
        Iterator it = m_classLoaders.values().iterator();
        while (it.hasNext()) {
            TRClassLoader classLoader = (TRClassLoader) it.next();
            if (classLoader != null) {
                classLoader.close();
                classLoader = null;
            }
        }

        //Release any JMSCommunicators bound to this clientID:
        if (DEBUG || devDebug)
            System.out.println(m_clientID + ": Releasing any bound recipients.");

        try {
            if (!releaseAll(30000)) {
                errorFlag = true;
                errorReason += "ERROR: Unable to release bind on all agents." + System.getProperty("line.separator");
            }
        } catch (Exception e) {
            errorFlag = true;
            errorReason += "ERROR: Unable to release bind on all agents." + System.getProperty("line.separator");
        }

        if (DEBUG || devDebug)
            System.out.println(m_clientID + ": Terminating JMS communication.");
        try {
            if (m_connection != null) {
                m_connection.stop();
                m_publisher.close();
                m_publisher = null;
                m_sendSession.close();
                m_sendSession = null;

                m_subscriber.close();
                m_subscriber = null;
                m_rcvSession.close();
                m_rcvSession = null;

                m_broadCastSubscriber.close();
                m_broadCastSubscriber = null;
                m_asyncRcvSession.close();
                m_asyncRcvSession = null;

                m_internalSubscriber.close();
                m_internalSubscriber = null;
                m_internalSession.close();
                m_internalSession = null;

                m_replyPublisher.close();
                m_replyPublisher = null;
                m_replySession.close();
                m_replySession = null;

                m_requestPublisher.close();
                m_requestPublisher = null;
                m_requestSubscriber.close();
                m_requestSubscriber = null;
                m_requestSession.close();
                m_requestSession = null;

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

    // Get Pid-specific class loader, if any, else return default class loader
    private TRClassLoader getClassLoader(Hashtable props) {
        TRClassLoader classLoader = null;
        if (props != null) {
            Integer pid = (Integer) props.get(TRAgent.PID);
            if (DEBUG)
                System.out.println("Getting classloader for PID = " + pid);
            if (pid != null)
                classLoader = (TRClassLoader) m_classLoaders.get(pid);
        }
        if (classLoader == null)
            classLoader = m_defaultClassLoader;
        return classLoader;
    }

    /**
     * If the owner of this communicator wishes to receive objects of type
     * TRDisplayMsg, this should be set to true. Otherwise these messages are
     * printed to System.out.
     * 
     * @param value
     */
    /*
     * public void setSendDisplayObjs(boolean value)
     * 
     * Determines whether a display object is returned by get message or
     * directly output to screen.
     */
    public void setSendDisplayObjs(boolean value) {
        sendDisplayObjs = value;
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
    public Object getMessage(long timeout) throws Exception {
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
            Object obj = null;

            try {
                obj = handleMessage(msg);
            } catch (Throwable thrown) {
                Exception e = new Exception();
                e = (Exception) thrown;
                throw e;
            }

            if (obj != null) {
                return obj;
            }

            //Otherwise call recursively with the remaining time left:
            return getMessage(timeout - (System.currentTimeMillis() - time));
        }
    }

    private Object handleMessage(javax.jms.Message msg) throws Throwable {
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
            synchronized (boundFlagLock) {
                if (DEBUG || devDebug)
                    System.out.println("Synched on boundFlagLock in handleMessage");
                if (source != null && msg instanceof StreamMessage && (!boundFlag || (boundFlag && source.equals(master)))) {

                    Object obj = null;
                    byte[] classBytes;
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    try {
                        while (true) {
                            byteStream.write(((StreamMessage) msg).readByte());
                        }
                    } catch (MessageEOFException meofe) {

                        classBytes = byteStream.toByteArray();
                        byteStream.close();
                        byteStream = null;

                        ByteArrayInputStream inByteStream = new ByteArrayInputStream(classBytes);
                        // the wrapped object is serialized inside of a TRMetaMessage, therefore, no special class loader is needed.
                        TRLoaderObjectInputStream inputStream = new TRLoaderObjectInputStream(new BufferedInputStream(inByteStream), m_defaultClassLoader);
                        try {
                            obj = inputStream.recoverableReadObject();
                        } catch (Throwable thrown) {
                            throw thrown;
                        }

                        if (inputStream != null) {
                            inputStream.close();
                            inputStream = null;
                        }

                        if (inByteStream != null) {
                            inByteStream.close();
                            inByteStream = null;
                        }
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
                        m_props = (Hashtable) ((TRBroadCastMetaMessage) obj).getProperties().get(m_clientID);
                    } else {
                        m_props = (Hashtable) ((TRMetaMessage) obj).getProperties();
                    }

                    // get Pid-specific class loader
                    TRClassLoader classLoader = getClassLoader(m_props);

                    obj = (Object) ((TRMetaMessage) obj).getContent(classLoader);
                    if (DEBUG || devDebug)
                        System.out.println("Meta message content is " + (obj == null ? "null" : obj.getClass().getName()));

                    if (obj instanceof TRDisplayMsg && !sendDisplayObjs) {
                        System.out.println(obj);
                    } else {
                        if (DEBUG || devDebug)
                            System.out.println("" + m_source + ": " + obj);
                        if (DEBUG || devDebug)
                            System.out.println("End synch on boundFlagLock in handleMessage (returning msg)");
                        m_source = source;
                        return obj;
                    }
                } else {
                    if (DEBUG || devDebug)
                        System.out.println("Received a message for another entity from " + source);
                }
            }//synchronized (boundFlagLock)

            if (DEBUG || devDebug)
                System.out.println("End synch on boundFlagLock in handleMessage (returning null)");
            return null;
        }//synchronized(handleMessageLock)
    }

    /**
     * Gets the source (clientID) of the last message received
     * 
     * @return The source
     */
    public String getSource() {
        return m_source;
    }

    private void checkVersionMatch(Message msg) throws TRVersionMismatchException {
        try {
            String source = msg.getStringProperty(CLIENT_ID);
            String version = msg.getStringProperty(VERSION);

            //Check for correct version: (If we are bound the check was done at bind time)
            if (version != null) {
                if (!version.equals(Version.getVersionString())) {
                    if (source != null) {
                        this.sendMessage(source, "Version mismatch: " + Version.getVersionString() + " / " + version);
                    }
                    throw new TRVersionMismatchException("Version mismatch: source (" + source + ") version: " + version + " this version: " + Version.getVersionString());
                }
                return;
            } else {
                if (source != null) {
                    this.sendMessage(source, "Poorly formatted message, VERSION not specified. Potential TestRunner version mismatch or message from non TestRunner entity");
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
     * The properties associated with the last message.
     * 
     * @return Properties hashtable or null if no properties.
     */
    public Hashtable getProperties() {
        if (DEBUG || devDebug)
            System.out.println("Returning properties:" + m_props);
        return m_props;
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

            //Check to see if this is a bind request for this clientID:
            if (msg.getStringProperty(BIND) != null) //Is a message about a bind:
            {
                System.out.println(m_clientID + ": received bind request from " + source);

                synchronized (boundFlagLock) //to protect bound flag
                {
                    long time = msg.getLongProperty("TimeOut");
                    //Check first if we are still connected to our master
                    //If the master is gone we are free to accept more
                    //bind requests:
                    //We use a timeout that is half the bind requestors timeout
                    //when we ping the the current master so that we can reply to
                    //the requestor in time.
                    if (boundFlag && !ping(master, time / 2)) {
                        System.out.println(m_clientID + ": releasing bind to " + master);
                        boundFlag = false;
                        this.m_bindListener.bindReleaseNotify(master);
                        master = null;
                    }
                    //If we are not bound accept the request
                    if ((!boundFlag && source != null && m_bindListener != null)) {
                        //Accept the bind:
                        boundFlag = true;
                        master = source;
                        this.m_bindListener.bindNotify(master);
                        this.reply(msg, source, BIND, "reply", new Boolean(true), true);
                        if (DEBUG || devDebug)
                            System.out.println(m_clientID + ": accepting bind request. Now bound to " + master);
                    } else {
                        if (DEBUG || devDebug)
                            System.out.println(m_clientID + ": rejecting bind request from " + source + ".");
                        this.reply(msg, source, BIND, "reply", new Boolean(false), true);
                        this.sendMessage(source, new TRErrorMsg("Already bound", null));
                    }
                }
                //m_pingTimer.SetNotify(this);
                return;
            }
            //Check to see if this is a release request for this clientID:
            if (msg.getStringProperty(RELEASE) != null) {
                //Always accept and acknowledge a release:
                //Even if we aren't bound to the source we can still say that
                //we aren't bound to them anymore. This way the source at
                //least knows that the request was received
                if (DEBUG || devDebug)
                    System.out.println(m_clientID + ": acknowledging bind release request.");

                //If we actually are bound to the source release the bind
                synchronized (boundFlagLock) //to protect bound flag
                {
                    if (boundFlag && source.equalsIgnoreCase(master)) {
                        m_bindListener.bindReleaseNotify(master);
                        if (DEBUG || devDebug)
                            System.out.println(m_clientID + ": releasing bind to " + source);
                        boundFlag = false;
                        master = null;
                    }
                }
                this.reply(msg, source, RELEASE, "reply", new Boolean(true), true);
                return;
            }

            //Check to see if this is a ping request for this clientID:
            if (msg.getStringProperty(PING) != null) //Is a message about a bind:
            {
                //Always acknowledge a ping:
                if (DEBUG || devDebug)
                    System.out.println(m_clientID + ": acknowledging ping request.");
                this.reply(msg, source, PING, "reply", new Boolean(true), true);
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

    void printStatus() {
        System.out.println("STATUS:");
        System.out.println("Time: " + new Date());
        System.out.println("bound: " + boundFlag);
        System.out.println("master: " + master);
        System.out.println("Last Message Source: " + m_source);
        System.out.println("bindListener: " + m_bindListener);
    }

    /**
     * Sends a message
     * 
     * @param recipient
     *            The clientID of the intended recipient
     * @param content
     *            The object to send
     * @exception JMSException
     * @exception MessageFormatException
     * @exception MessageNotWriteableException
     */
    public void sendMessage(String recipient, Object content) throws Exception {
        sendMessage(recipient, content, null);
    }

    /**
     * Sends a message with a property set
     * 
     * @param recipient
     *            The clientId of the recipient
     * @param content
     *            The object to send
     * @param propertyName
     *            The key for the property
     * @param PropertyValue
     *            The value of the property.
     * @exception JMSException
     * @exception MessageFormatException
     * @exception MessageNotWriteableException
     */
    public void sendMessage(String recipient, Object content, String propertyName, Object PropertyValue) throws Exception {
        Hashtable props = new Hashtable();
        props.put(propertyName, PropertyValue);
        sendMessage(recipient, content, props);
    }

    /**
     * Sends a message with properties set
     * 
     * @param recipient
     *            The clientID of the intended recipient
     * @param content
     *            The object to send
     * @param props
     *            The properties to send
     * @exception JMSException
     * @exception MessageFormatException
     * @exception MessageNotWriteableException
     */

    public void sendMessage(String recipient, Object content, Hashtable props) throws Exception {
        if (props == null) {
            props = new Hashtable();
        }
        if (content instanceof TRMsg) {
            ((TRMsg) content).setSource(m_clientID);
        }
        internalSend(recipient, new TRMetaMessage(content, props));
    }

    private void internalSend(String recipient, Object content) throws Exception {
        StreamMessage msg = m_sendSession.createStreamMessage();

        //Always set a property with out unique clientID
        //So that the entity we are sending to knows that it is us
        msg.setStringProperty(CLIENT_ID, m_clientID);
        msg.setStringProperty(VERSION, Version.getVersionString());

        //Send the serializable object as an stream of bytes to be
        //dealt with on the other end:

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(byteStream));
        oos.writeObject((java.io.Serializable) content);
        oos.flush();
        byte[] objectBytes = byteStream.toByteArray();
        for (int i = 0; i < objectBytes.length; i++) {
            msg.writeByte(objectBytes[i]);
        }

        Topic topic = m_sendSession.createTopic(TOPIC_PREFIX + recipient.toUpperCase());
        synchronized (m_sendLock) {
            if (DEBUG || devDebug)
                System.out.println("Sending message on " + topic.getTopicName());
            m_publisher.publish(topic, msg, DeliveryMode.PERSISTENT, 0, 0);
            if (DEBUG || devDebug)
                System.out.println("Finished Sending message on " + topic.getTopicName());
        }
    }

    /**
     * Broadcasts a message to be sent asynchronously to all entities whose
     * clientID is in recips and are connected to the Control broker.
     * 
     * @param content
     *            The object to send
     * @param recips
     * @exception JMSException
     * @exception MessageFormatException
     * @exception MessageNotWriteableException
     */
    public void broadCast(Object content, String[] recips) throws Exception, JMSException, MessageFormatException, MessageNotWriteableException {
        broadCast(content, recips, null);
    }

    /**
     * Broadcasts a message to be sent asynchronously to all entities whose
     * clientID is in recips and are connected to the control broker.
     * 
     * @param content
     *            The object to send
     * @param recips
     *            The intended recipients
     * @param propsTable
     *            A Hashtable of Hashtables of properties for each recipient
     *            each keyed on the name of the recipient.
     * @exception JMSException
     * @exception MessageFormatException
     * @exception MessageNotWriteableException
     */
    public void broadCast(Object content, String[] recips, Hashtable propsTable) throws Exception, JMSException, MessageFormatException, MessageNotWriteableException {
        if (propsTable == null) {
            propsTable = new Hashtable();
        }
        if (content instanceof TRMsg) {
            ((TRMsg) content).setSource(m_clientID);
        }

        internalSend(BROADCAST_TOPIC_POSTFIX, new TRBroadCastMetaMessage(content, recips, propsTable));
    }

    /**
     * Binds another entity for exclusive communication.
     * 
     * @param recipient
     *            The clientID of the recipient to bind
     * @param timeout
     *            The amount of time to allow the entity being bound to
     *            acknowledge that it is bound before returning false
     * @return true if the bind request is accepted, false otherwise.
     * @exception JMSException
     * @exception MessageFormatException
     * @exception MessageNotWriteableException
     */
    /*
     * public boolean bind(String recipient, long timeout)
     * 
     * Request to bind another JMSCommunicator for exclusive communication with
     * this JMSCommunicator (using its unique clientID.
     */
    public boolean bind(String recipient, long timeout) throws JMSException, MessageFormatException, MessageNotWriteableException {
        if (DEBUG || devDebug)
            System.out.println("Attempting to bind: " + recipient);

        if (request(recipient, BIND, "", timeout)) {
            if (DEBUG || devDebug)
                System.out.println("Bound: " + recipient);
            boundList.addElement(recipient);
            return true;
        } else {
            if (DEBUG || devDebug)
                System.out.println(m_clientID + ": ERROR: binding " + recipient);
        }
        return false;

    }

    /**
     * Release all agents bound by this communicator
     * 
     * @param timeout
     *            The amount of time for all the bound entities to confirm they
     *            have been released
     * @return true if successful.
     * @exception JMSException
     * @exception MessageFormatException
     * @exception MessageNotWriteableException
     */
    public boolean releaseAll(long timeout) throws JMSException, MessageFormatException, MessageNotWriteableException {
        boolean releaseSuccess = true;
        while (boundList.size() > 0) {
            if (!release((String) boundList.elementAt(0), timeout)) {
                if (DEBUG || devDebug)
                    System.out.println("ERROR: Unable to release bind on " + (String) boundList.elementAt(0));
                releaseSuccess = false;
                boundList.removeElementAt(0);
            }
        }
        return releaseSuccess;
    }

    /**
     * Releases a single bindee.
     * 
     * @param recipient
     *            The clientID of the recipient to release.
     * @param timeout
     *            The time to allow for the bindee to comfirm that it has been
     *            released
     * @return true if the recipient was released.
     * @exception JMSException
     * @exception MessageFormatException
     * @exception MessageNotWriteableException
     */
    /*
     * public boolean release(String recipient, long timeout)
     * 
     * Request to release another JMSCommunicator from exclusive communication
     * with this JMSCommunicator (using its unique clientID).
     */
    public boolean release(String recipient, long timeout) throws JMSException, MessageFormatException, MessageNotWriteableException {
        if (DEBUG || devDebug)
            System.out.println("Attempting to release: " + recipient);

        if (request(recipient, RELEASE, "request", timeout)) {
            if (DEBUG || devDebug)
                System.out.println("Released: " + recipient);
            boundList.removeElement(recipient);
            return true;
        } else {
            if (DEBUG || devDebug)
                System.out.println(m_clientID + " ERROR: releasing bind on " + recipient + " (timed out)");
        }
        return false;
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

    /*
     * public boolean ping(String recipient, long timeout)
     * 
     * Request for ping reply from another JMSCommunicator.
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

    private void reply(Message msg, String source, String property, String value, Object object, boolean internal) {
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
     * Sets the custom class loader for this Communicator and Pid to be used
     * when reading serialized objects:
     * 
     * @param tcl
     *            = class loader pid = id of launched process which is the
     *            destination for an object
     */
    public void setClassLoader(TRClassLoader tcl, int pid) throws Exception {
        if (DEBUG)
            System.out.println("Setting class loader " + tcl + " for PID = " + pid);
        TRClassLoader existingClassLoader = (TRClassLoader) m_classLoaders.get(new Integer(pid));
        if (existingClassLoader != null) {
            existingClassLoader.close();
            existingClassLoader = null;
        }
        TRClassLoader newClassLoader = tcl;
        if (newClassLoader == null)
            newClassLoader = new TRClassLoader("");
        m_classLoaders.put(new Integer(pid), newClassLoader);
    }

    /**
     * Remove the specified TRClassLoader if it had been set for this
     * communicator and pid before
     * 
     * @param tcl
     *            The TRClassloader to remove pid = the id of the launched
     *            process with which the class loader was associated
     */
    public void removeClassLoader(TRClassLoader tcl, int pid) {
        if (DEBUG)
            System.out.println("Removing class loader for PID = " + pid);
        TRClassLoader classLoader = (TRClassLoader) m_classLoaders.get(new Integer(pid));
        if (classLoader == tcl) {
            tcl.close();
            tcl = null;
            m_classLoaders.remove(new Integer(pid));
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
        ITRAsyncMessageHandler m_messageHandler;

        public BroadCastListener(ITRAsyncMessageHandler messageHandler) {
            m_messageHandler = messageHandler;
        }

        public void onMessage(javax.jms.Message message) {
            if (m_messageHandler != null) {
                if (DEBUG || devDebug)
                    System.out.println("Calling handle message from BroadCastListener");

                if (DEBUG || devDebug)
                    System.out.println("Received broadcast message.");
                try {
                    Object obj = handleMessage(message);

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
                        sendMessage((String) message.getStringProperty(CLIENT_ID), new TRErrorMsg("ERROR: handling broadcast message.", thrown));
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
