::------------------------------------------------------------------------------------------
:: name: cpy.create.bat
:: desc: initializes Python environment wiring for C3DClasses SDK
::------------------------------------------------------------------------------------------
@echo off
echo [CREATING] CPythonEnvironment - cezdev python environment ...
set "CPYCREATEHOME=%CD%"
echo [CALLING] %~nx0
echo [STARTED] %date% %time%
set "START_TIME=%time%"

echo [INFO] Current directory: %CD%
echo [INFO] Script directory: %~dp0

set "CPY_HOME=%~dp0"
set "CPY_NAME=cpy"

:: Adding the Java environment to PATH
echo [ADDING] CPythonEnvironment to PATH: %CPY_HOME%
set PATH=%PATH%;%CPY_HOME%

:: Moving the Python environment from core source to Python project
call cpy.move.bat

:: Building the Python environment
call cpy.build.bat

:: Return to the original directory
echo [RETURNING] to original directory: %CPYCREATEHOME%
cd /d "%CPYCREATEHOME%"
echo [ENDING] %~nx0