::------------------------------------------------------------------------------------------
:: name: python.create.bat
:: desc: Sets up Python environment variables
::------------------------------------------------------------------------------------------
@echo off

:: Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

:: Set Python paths relative to script directory
set "PYTHON_HOME=%SCRIPT_DIR%\python-3.13.13-embed-amd64"
set "PYTHON_SCRIPTS=%PYTHON_HOME%\Scripts"

:: Add to PYTHONPATH
set "PYTHONPATH=%PYTHONPATH%;%PYTHON_HOME%"

:: Add to PATH if not already present
echo %PATH% | find /I "%PYTHON_HOME%" >nul
if errorlevel 1 (
    set "PATH=%PATH%;%PYTHON_HOME%"
    echo [INFO] Added %PYTHON_HOME% to PATH.
) else (
    echo [INFO] %PYTHON_HOME% is already in PATH.
)

echo %PATH% | find /I "%PYTHON_SCRIPTS%" >nul
if errorlevel 1 (
    set "PATH=%PATH%;%PYTHON_SCRIPTS%"
    echo [INFO] Added %PYTHON_SCRIPTS% to PATH.
) else (
    echo [INFO] %PYTHON_SCRIPTS% is already in PATH.
)