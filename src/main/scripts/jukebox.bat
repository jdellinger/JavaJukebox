
set "PRGDIR=%~dp0"
set "JUKEBOX_HOME=%PRGDIR%\.."

set "JAVA_OPTS=-DJUKEBOX_HOME=%JUKEBOX_HOME%"
@For %%a in ("%JUKEBOX_HOME%\lib\JavaJukebox-*.jar") do set "JARFILE=%%a"
java %JAVA_OPTS% -jar %JARFILE% %*
