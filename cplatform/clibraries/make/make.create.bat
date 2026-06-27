::------------------------------------------------------------------------------------------
:: name: make.create.bat
:: desc: Sets up Make environment variables
::------------------------------------------------------------------------------------------
@echo off

:: Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

:: Set Make paths relative to script directory
set "MAKE_HOME=%SCRIPT_DIR%\make-3.81-bin\bin"

:: Add to PATH if not already present
echo %PATH% | find /I "%MAKE_HOME%" >nul
if errorlevel 1 (
    set "PATH=%PATH%;%MAKE_HOME%"
    echo [INFO] Added %MAKE_HOME% to PATH.
) else (
    echo [INFO] %MAKE_HOME% is already in PATH.
)