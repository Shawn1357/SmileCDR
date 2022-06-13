#!/bin/bash
# Be sure EOL is set to Linux format (LF)

JAVA=java
LOGBACK_FILE=DLQWatcherControl-logback.xml
JAR_FILE=dlqutilities.jar
MAIN_CLASS_NAME=ca.ontariohealth.smilecdr.dlqwatchercontrol.DLQWatcherControl
CFG_FILE=./DLQWatcher.properties
CMD_LINE_OPTS="--operation QUIT"

ENV_NAME=cwm-dev

SMILECDR_ROOT=/home/smile/smilecdr
SMILECDR_LIB=$SMILECDR_ROOT/lib
THIRDPARTY_LIB=./lib

JVM_OPTS="-Dfile.encoding=UTF-8 -Dlogback.configurationFile=$LOGBACK_FILE -XX:+ShowCodeDetailsInExceptionMessages"

CLSPATH=$JAR_FILE:.
CLSPATH=$CLSPATH:$SMILECDR_LIB/commons-cli-1.5.0.jar
CLSPATH=$CLSPATH:$SMILECDR_LIB/commons-lang3-3.12.0.jar
CLSPATH=$CLSPATH:$SMILECDR_LIB/gson-2.8.9.jar
CLSPATH=$CLSPATH:$SMILECDR_LIB/kafka-clients-2.5.1.jar
CLSPATH=$CLSPATH:$SMILECDR_LIB/kafka-streams-2.5.1.jar
CLSPATH=$CLSPATH:$SMILECDR_LIB/logback-core-1.2.10.jar
CLSPATH=$CLSPATH:$SMILECDR_LIB/logback-classic-1.2.10.jar
CLSPATH=$CLSPATH:$SMILECDR_LIB/slf4j-api-1.7.33.jar
CLSPATH=$CLSPATH:$THIRDPARTY_LIB/activation.jar
CLSPATH=$CLSPATH:$THIRDPARTY_LIB/javax.mail-1.6.2.jar


$JAVA $JVM_OPTS -classpath $CLSPATH $MAIN_CLASS_NAME --envName $ENV_NAME --cfgFile $CFG_FILE $CMD_LINE_OPTS
