::------------------------------------------------------------------------------------------
:: name: java.create.bat
:: desc: Sets up Java environment variables
::------------------------------------------------------------------------------------------
@echo off

:: Get the directory where this script is located
set "JAVA_SCRIPT_DIR=%~dp0"

:: Remove trailing backslash
if "%JAVA_SCRIPT_DIR:~-1%"=="\" set "JAVA_SCRIPT_DIR=%JAVA_SCRIPT_DIR:~0,-1%"

:: Set JAVA_HOME to the JDK folder within the script's directory
set "JAVA_HOME=%JAVA_SCRIPT_DIR%\jdk-24.0.1"
set "JAVA_BIN=%JAVA_HOME%\bin"

:: Add to PATH if not already present
echo %PATH% | find /I "%JAVA_BIN%" >nul
if errorlevel 1 (
    set "PATH=%PATH%;%JAVA_BIN%"
    echo [INFO] Added %JAVA_BIN% to PATH.
) else (
    echo [INFO] %JAVA_BIN% is already in PATH.
)