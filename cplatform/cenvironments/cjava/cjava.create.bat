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

:: Adding the Java environment to PATH
echo [ADDING] CJavaEnvironment to PATH: %CJAVA_HOME%
set PATH=%PATH%;%CJAVA_HOME%

:: Moving the Java environment from core source to Java project
call cjava.move.bat

:: Building the Java environment
call cjava.build.bat

:: Return to the original directory
cd /d "%CJAVACREATEHOME%"
echo [FINISHED] %date% %time%