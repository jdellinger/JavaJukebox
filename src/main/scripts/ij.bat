set "PRGDIR=%~dp1"
set "JUKEBOX_HOME=%PRGDIR%\.."

set "JAVA_OPTS=-DJUKEBOX_HOME=%JUKEBOX_HOME%"
set "DERBY_OPTS=-Dij.connection.jukebox=jdbc:derby:db -Dderby.system.home=%JUKEBOX_HOME%"
unset IJ_CP
@For %%a in ("%JUKEBOX_HOME%\lib\derby*.jar") do set "IJ_CP=%IJ_CP%;%%a"
java %JAVA_OPTS% %DERBY_OPTS% -classpath %IJ_CP% org.apache.derby.tools.ij %*