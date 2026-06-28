::----------------------------------------------------------------------------------------------------------
:: name: path.list.bat
:: desc: Lists all files in a directory and outputs to a JSON file
:: usage: path.list <output.json> <srcdir1> [srcdir2 ...]
:: example: path.list C:\cezdev\meta\files.json C:\src
::----------------------------------------------------------------------------------------------------------
@echo off

echo [CALLING] %~nx0

set "PATHLISTHOME=%CD%"

:: Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

if "%~1"=="" (
    echo [ERROR] Usage: path.list ^<output.json^> ^<srcdir1^> [srcdir2 ...]
    exit /b 1
)

cd /d "%SCRIPT_DIR%"
call pythonx PathListCommand.py %*
cd /d "%PATHLISTHOME%"

echo [ENDING] %~nx0