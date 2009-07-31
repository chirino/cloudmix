echo off
TITLE TR Agent

REM  runTRAgent.bat

REM  ---------------------------------------------------------------------------
REM  Usage:   runTRAgent
REM  Purpose: See the testrunner documentation for the explanation
REM  ---------------------------------------------------------------------------

REM  -------- USER MUST SET THE FOLLOWING VARIABLES AFTER INSTALL --------------
REM  JAVA_EXE should indicate the path to a 1.3 (or higher) java executable
REM
REM  SONIC_CLIENT_LIB_DIR should indicate the path to the Sonic client jar files
REM  used for testrunner communication between the agents and the controller
REM  ---------------------------------------------------------------------------

REM Set this to a particular JAVA_EXE if you wish
set JAVA_EXE=

REM Setup the Java Virtual Machine

goto BEGIN

:warn
    echo TRAgent: %*
goto :EOF

:BEGIN

if not "%JAVA_EXE%" == "" goto :Check_JAVA_END
	set JAVA_EXE=%JAVA%
	if not "%JAVA_EXE%" == "" goto :Check_JAVA_END
    	set JAVA_EXE=java
    	if "%JAVA_HOME%" == "" call :warn JAVA_HOME not set; results may vary
    	if not "%JAVA_HOME%" == "" set JAVA_EXE=%JAVA_HOME%\bin\java.exe
    	if not exist "%JAVA_HOME%" (
        	call :warn %JAVA_HOME% does not exist
        	goto END
    	)
:Check_JAVA_END

REM Configure ActiveMQ as the JMS provider
set ACTIVEMQ_CLIENT_LIB_DIR=..\lib
set JMS_PROVIDER_CLASSPATH=%ACTIVEMQ_CLIENT_LIB_DIR%\activemq-all.jar
set JMS_PROVIDER_CONNECTION_FACTORY=org.apache.activemq.ActiveMQConnectionFactory

REM Uncomment and edit the following to use Sonic as the JMS provider
REM set SONIC_CLIENT_LIB_DIR=..\lib
REM set JMS_PROVIDER_CLASSPATH=%SONIC_CLIENT_LIB_DIR%\sonic_Client.jar;%SONIC_CLIENT_LIB_DIR%\jndi.jar
REM set JMS_PROVIDER_CONNECTION_FACTORY=progress.message.jclient.ConnectionFactory

REM ---------------YOU DO NOT NEED TO CHANGE ANYTHING BELOW --------------------

REM ---------------------------------------------------------------------------
REM CLASSES contains the classpath required by a testrunner agent. Relative
REM paths are relative to the testrunner\bin directory.
REM ---------------------------------------------------------------------------
set CLASSES=..\lib\testrunner.jar;..\lib\rmiviajms.jar;%JMS_PROVIDER_CLASSPATH%

echo ------- Starting Agent -------
echo %JAVA_EXE% -Dtestrunner.jms.provider=%JMS_PROVIDER_CONNECTION_FACTORY% -classpath %CLASSES% org.fusesource.testrunner.rmi.RemoteProcessLauncher
%JAVA_EXE% -Dtestrunner.jms.provider=%JMS_PROVIDER_CONNECTION_FACTORY% -classpath %CLASSES% org.fusesource.testrunner.rmi.RemoteProcessLauncher

:END
echo Paused to catch any errors. Press any key to continue.
pause
