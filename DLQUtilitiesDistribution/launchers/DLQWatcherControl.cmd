@echo off
setlocal enableextensions

set MAIN_JAR_FILE_NAME=dlqutilities.jar
set CONFIG_FILE_NAME=DLQWatcher.properties
set LOGBACK_FILE_NAME=DLQWatcherControl-logback.xml

set STARTUP_CLASS_NAME=ca.ontariohealth.smilecdr.dlqwatchercontrol.DLQWatcherControl

set SUPPORTING_JARS=activation.jar ^
                    commons-cli-1.5.0.jar ^
					commons-lang3-3.12.0.jar ^
					gson-2.8.9.jar ^
					javax.mail-1.6.2.jar ^
					json-20210307.jar ^
					kafka-clients-2.5.1.jar ^
					kafka-streams-2.5.1.jar ^
					logback-classic-1.2.10.jar ^
					logback-core-1.2.10.jar ^
					slf4j-api-1.7.33.jar

rem echo SUPPORTING_JARS %SUPPORTING_JARS%

rem
rem Set the JVM related parameters
rem
rem

for /F "usebackq tokens=*" %%J in (`where java.exe`) do set JAVA_EXE=%%J
if "%JAVA_EXE%" == "" set JAVA_EXE=java

set JVM_ARGS=--add-opens java.base/java.util=ALL-UNNAMED



call :FullyQualify THIS_DIR "%~dp0."

rem
rem Verify the important distribution files can be found.
rem
rem

call :LocateFileDir MAIN_JAR_DIR     "%MAIN_JAR_FILE_NAME%"
call :LocateFileDir CONFIG_FILE_DIR  "%CONFIG_FILE_NAME%"
call :LocateFileDir LOGBACK_FILE_DIR "%LOGBACK_FILE_NAME%"

if "%MAIN_JAR_DIR%"     == "" set MISSING_FILE=%MAIN_JAR_FILE_NAME%
if "%CONFIG_FILE_DIR%"  == "" set MISSING_FILE=%CONFIG_FILE_NAME%
if "%LOGBACK_FILE_DIR%" == "" set MISSING_FILE=%LOGBACK_FILE_NAME%

rem
rem Build the class path.
rem
rem

set CP=%MAIN_JAR_DIR%\%MAIN_JAR_FILE_NAME%;%CONFIG_FILE_DIR%
call :BuildClassPath %SUPPORTING_JARS%

goto :RunWatcherControl


rem
rem Run the DLQ Watcher Control App
rem
rem

:RunWatcherControl

rem Abort if we did not find one of the necessary files.
if not "%MISSING_FILE%" == "" goto :FileNotFound

rem echo ClassPath %CP%
set CMD_LINE="%JAVA_EXE%" -classpath "%CP%" %JVM_ARGS% "-Dlogback.configurationFile=%LOGBACK_FILE_DIR%\%LOGBACK_FILE_NAME%" %STARTUP_CLASS_NAME% %*
rem echo Command Line: %CMD_LINE%
%CMD_LINE%

goto :AllDone



rem
rem Generate the JAVA Class Path from the list of supplied JAR files.
rem Each command line argument is a new JAR file to be added to the class path
rem variable.
rem
rem The class path variable is named: CP
rem

:BuildClassPath

:CPBuilderLoop
if "%~1" == "" goto :EOF
call :LocateFileDir JAR_DIR "%~1"

if     "%JAR_DIR%" == "" set MISSING_FILE=%~1
if not "%JAR_DIR%" == "" set CP=%CP%;%JAR_DIR%\%~1

shift
goto :CPBuilderLoop


:LocateFileDir
REM Directory Tests should be ordered in order of least to most preferred location
REM Argument 1 is the environment variable to set.
REM Argument 2 is the base file name to search for (ie: name.ext)
if "%~1" == "" goto :EOF
if "%~2" == "" goto :EOF

if exist "%THIS_DIR%\..\lib\%~2"       call :FullyQualify %~1 "%THIS_DIR%\..\lib"
if exist "%THIS_DIR%\..\dist\lib\%~2"  call :FullyQualify %~1 "%THIS_DIR%\..\dist\lib"
if exist "%THIS_DIR%\lib\%~2"          call :FullyQualify %~1 "%THIS_DIR%\lib"
if exist "%THIS_DIR%\dist\lib\%~2"     call :FullyQualify %~1 "%THIS_DIR%\dist\lib"
if exist "%THIS_DIR%\dist\%~2"         call :FullyQualify %~1 "%THIS_DIR%\dist"
if exist "%THIS_DIR%\..\dist\%~2"      call :FullyQualify %~1 "%THIS_DIR%\..\dist"
if exist "%THIS_DIR%\..\%~2"           call :FullyQualify %~1 "%THIS_DIR%\.."
if exist "%THIS_DIR%\%~2"              set  %~1=%THIS_DIR%

goto :EOF

:FullyQualify
set %1=%~f2
goto :EOF

:FileNotFound
echo FATAL Unable to find the File: %MISSING_FILE%
echo.
exit /b 1

:Usage
%~n0 - Launch the DLQ Watcher 

:AllDone
endlocal
