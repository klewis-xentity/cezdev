::------------------------------------------------------------------------------------------
:: name: apache-CODE.create.bat
:: desc: Sets up CODE environment variables
::------------------------------------------------------------------------------------------
@echo off

:: Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

:: Set CODE paths relative to script directory
set "CODE_HOME=%SCRIPT_DIR%\VSCode-win32-x64-1.119.1"
set "CODE_BIN=%CODE_HOME%\bin"

:: Add to PATH if not already present
echo %PATH% | find /I "%CODE_BIN%" >nul
if errorlevel 1 (
    set "PATH=%PATH%;%CODE_BIN%"
    echo [INFO] Added %CODE_BIN% to PATH.
) else (
    echo [INFO] %CODE_BIN% is already in PATH.
)
