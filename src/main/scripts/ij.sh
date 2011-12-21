#!/bin/sh

PRGDIR=`dirname $0`
JUKEBOX_HOME=$PRGDIR/..

JAVA_OPTS=-DJUKEBOX_HOME=$JUKEBOX_HOME
DERBY_OPTS=-Dderby.system.home=$JUKEBOX_HOME
PROPS_FILE=$JUKEBOX_HOME/tmp/ij.properties
IJ_CP=.
for f in $JUKEBOX_HOME/lib/derby*.jar
do
	IJ_CP=$IJ_CP:$JUKEBOX_HOME/lib/$f
done
java $JAVA_OPTS $DERBY_OPTS -classpath $IJ_CP org.apache.derby.tools.ij -p PROPS_FILE $*
