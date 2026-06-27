::------------------------------------------------------------------------------------------
:: name: notepad++.create.bat
:: desc: Sets up Notepad++ environment variables
::------------------------------------------------------------------------------------------
@echo off

echo [CALLING] %~nx0

:: Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

:: Set Notepad++ paths relative to script directory
set "NOTEPADPP_HOME=%SCRIPT_DIR%\npp.7.6.2.bin.x64"
set "NOTEPADPP_EXE=%NOTEPADPP_HOME%\notepad++.exe"

if not exist "%NOTEPADPP_EXE%" (
    echo [ERROR] Notepad++ executable not found: %NOTEPADPP_EXE%
    exit /b 1
)

:: Add install directory to PATH if not already present
echo %PATH% | find /I "%NOTEPADPP_HOME%" >nul
if errorlevel 1 (
    set "PATH=%PATH%;%NOTEPADPP_HOME%"
    echo [INFO] Added %NOTEPADPP_HOME% to PATH.
) else (
    echo [INFO] %NOTEPADPP_HOME% is already in PATH.
)

echo [ENDING] %~nx0
