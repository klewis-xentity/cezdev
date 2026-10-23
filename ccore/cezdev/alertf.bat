@echo off
::------------------------------------------------------------------------------------------
:: name: alertf.bat
:: desc: Reads a text file and shows its content via AlertFileCommand
:: usage: alertf.bat <filename> [title] [type]
::------------------------------------------------------------------------------------------

echo [CALLING] %~nx0

set "ALERTF_HOME=%CD%"
cd /d "%~dp0"

set "INPUT_FILE=%~1"
set "TITLE=%~2"
set "TYPE=%~3"

if "%INPUT_FILE%"=="" (
    echo [ERROR] Missing filename.
    echo [USAGE] %~nx0 ^<filename^> [title] [type]
    cd /d "%ALERTF_HOME%"
    exit /b 1
)

if not exist "%INPUT_FILE%" (
    echo [ERROR] File not found: %INPUT_FILE%
    cd /d "%ALERTF_HOME%"
    exit /b 1
)

if exist "AlertFileCommand.class" goto :run
javac AlertFileCommand.java
if errorlevel 1 (
    echo [ERROR] Failed to compile AlertFileCommand.java
    cd /d "%ALERTF_HOME%"
    exit /b 1
)

:run
java AlertFileCommand "%INPUT_FILE%" "%TITLE%" "%TYPE%"

cd /d "%ALERTF_HOME%"

echo [ENDING] %~nx0
