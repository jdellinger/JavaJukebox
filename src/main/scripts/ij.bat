@echo off
setlocal
set "PRGDIR=%~dp0"
set "JUKEBOX_HOME=%PRGDIR%\.."

set "JAVA_OPTS=-DJUKEBOX_HOME=%JUKEBOX_HOME%"
set "DERBY_OPTS=-Dderby.system.home=%JUKEBOX_HOME%"
java %JAVA_OPTS% %DERBY_OPTS% -classpath "%JUKEBOX_HOME%\lib\*" org.apache.derby.tools.ij %*