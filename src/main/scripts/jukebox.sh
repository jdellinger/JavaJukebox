#!/bin/sh

PRGDIR=`dirname $0`
JUKEBOX_HOME=$PRGDIR/..

JAVA_OPTS=-DJUKEBOX_HOME=$JUKEBOX_HOME
JARFILE=`find $JUKEBOX_HOME/lib -name JavaJukebox-*.jar -print0`
java $JAVA_OPTS -jar $JARFILE $*

