::------------------------------------------------------------------------------------------
:: name: apache-maven.create.bat
:: desc: Sets up Maven environment variables
::------------------------------------------------------------------------------------------
@echo off

:: Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

:: Set Maven paths relative to script directory
set "MAVEN_HOME=%SCRIPT_DIR%\apache-maven-3.9.10"
set "MAVEN_BIN=%MAVEN_HOME%\bin"

:: Add to PATH if not already present
echo %PATH% | find /I "%MAVEN_BIN%" >nul
if errorlevel 1 (
    set "PATH=%PATH%;%MAVEN_BIN%"
    echo [INFO] Added %MAVEN_BIN% to PATH.
) else (
    echo [INFO] %MAVEN_BIN% is already in PATH.
)
