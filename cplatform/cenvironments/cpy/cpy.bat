::------------------------------------------------------------------------------------------
:: name: cpy.bat
:: desc: runs a python program
:: usage: cpy myscript.py arg1 arg2 ...
::------------------------------------------------------------------------------------------
@echo off
setlocal
set "CPYHOME=%CD%"
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

:: If no arguments, launch python REPL
IF "%~1"=="" (
    call python
    GOTO DONE
)

:: Run python directly with passthrough arguments
call python %*

:DONE
cd /d "%CPYHOME%"
endlocal
