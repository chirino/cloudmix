#!/bin/sh
#  runTRAgent.sh

#  ---------------------------------------------------------------------------
#  Usage:   runTRAgent.sh 
#  Purpose: See the testrunner documentation for the explanation
#  ---------------------------------------------------------------------------

#  -------- USER MUST SET THE FOLLOWING VARIABLES AFTER INSTALL --------------
#  JAVA_EXE should indicate the path to a 1.3 (or higher) java executable
#
#  SONIC_CLIENT_LIB_DIR should indicate the path to the Sonic client jar files
#  used for testrunner communication between the agents and the controller
#  ---------------------------------------------------------------------------

JAVA_EXE=

SONIC_CLIENT_LIB_DIR=../lib

# Configure ActiveMQ as the JMS provider
ACTIVEMQ_CLIENT_LIB_DIR=../lib
JMS_PROVIDER_CLASSPATH=$ACTIVEMQ_CLIENT_LIB_DIR/activemq-all.jar
JMS_PROVIDER_CONNECTION_FACTORY=org.apache.activemq.ActiveMQConnectionFactory

# Uncomment and edit the following to use Sonic as the JMS provider
# set SONIC_CLIENT_LIB_DIR=../lib
# set JMS_PROVIDER_CLASSPATH=$SONIC_CLIENT_LIB_DIR/sonic_Client.jar;$SONIC_CLIENT_LIB_DIR/jndi.jar
# set JMS_PROVIDER_CONNECTION_FACTORY=progress.message.jclient.TopicConnectionFactory

# ---------------YOU DO NOT NEED TO CHANGE ANYTHING BELOW --------------------

# ----------------------------------------------------------------------------
# CLASSES contains the classpath required by a testrunner agent. Relative
# paths are relative to the testrunner/bin directory.
# ----------------------------------------------------------------------------
CLASSES=../lib/testrunner.jar:$JMS_PROVIDER_CLASSPATH

echo ------- Starting Agent -------
echo $JAVA_EXE -Dtestrunner.jms.provider=$JMS_PROVIDER_CONNECTION_FACTORY -classpath $CLASSES com.sonicmq.testrunner.TRAgent TRA.ini
$JAVA_EXE -Dtestrunner.jms.provider=$JMS_PROVIDER_CONNECTION_FACTORY -classpath $CLASSES com.sonicmq.testrunner.TRAgent TRA.ini

echo Paused to catch any errors. Press any key to continue.
read ans

