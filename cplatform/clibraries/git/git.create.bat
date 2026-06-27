::------------------------------------------------------------------------------------------
:: name: git.create.bat
:: desc: Sets up Git environment variables
::------------------------------------------------------------------------------------------
@echo off

:: Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

:: Set Git paths relative to script directory
set "GIT_HOME=%SCRIPT_DIR%\PortableGit-2.51.0-64-bit"
set "GIT_BIN=%GIT_HOME%\bin"

:: Add to PATH if not already present
echo %PATH% | find /I "%GIT_BIN%" >nul
if errorlevel 1 (
    set "PATH=%PATH%;%GIT_BIN%"
    echo [INFO] Added %GIT_BIN% to PATH.
) else (
    echo [INFO] %GIT_BIN% is already in PATH.
)
