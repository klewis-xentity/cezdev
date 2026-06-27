::------------------------------------------------------------------------------------------
:: name: node.create.bat
:: desc: Sets up Node.js environment variables
::------------------------------------------------------------------------------------------
@echo off

:: Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

:: Set Node paths relative to script directory
set "NODE_HOME=%SCRIPT_DIR%\node-v22.17.1-win-x64"

:: Add to PATH if not already present
echo %PATH% | find /I "%NODE_HOME%" >nul
if errorlevel 1 (
    set "PATH=%PATH%;%NODE_HOME%"
    echo [INFO] Added %NODE_HOME% to PATH.
) else (
    echo [INFO] %NODE_HOME% is already in PATH.
)


