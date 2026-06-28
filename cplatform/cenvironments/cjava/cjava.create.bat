::------------------------------------------------------------------------------------------
:: name: cjava.create.bat
:: desc: creates the Java environment into memory for C3DClasses SDK
::------------------------------------------------------------------------------------------
@echo off

echo [CREATING] CJavaEnvironment - cezdev java environment ...

:: Save the current directory
set "CJAVACREATEHOME=%CD%"
echo [CALLING] %~nx0
echo [STARTED] %date% %time%
set "START_TIME=%time%"

echo [INFO] Current directory: %CD%
echo [INFO] Script directory: %~dp0
set CJAVA_HOME=%~dp0
set CJAVA_NAME=cjava
set "C3DCLASSES_NAME=c3dclassessdk"
set "C3DCLASSES_VERSION=1.0"
if "%C3DCLASSES_JAVAPATH%"=="" set "C3DCLASSES_JAVAPATH=%CMETADATA%\c3dclasses_java"
set "C3DCLASSES_JARFILE=%C3DCLASSES_JAVAPATH%\target\%C3DCLASSES_NAME%-%C3DCLASSES_VERSION%-jar-with-dependencies.jar"

:: Adding the Java environment to PATH
echo [ADDING] CJavaEnvironment to PATH: %CJAVA_HOME%
call path.set.bat %CJAVA_HOME%

if exist "%C3DCLASSES_JARFILE%" (
   echo [INFO] Found existing JAR file: %C3DCLASSES_JARFILE% skipping Maven build. Delete the JAR file to force a rebuild.
) else (
   :: Moving the Java environment from core source to Java project
   call cjava.move.bat

   :: Building the Java environment
   call cjava.build.bat
)

:: Return to the original directory
cd /d "%CJAVACREATEHOME%"
echo [FINISHED] %date% %time%
