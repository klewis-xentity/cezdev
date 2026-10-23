@echo off
::------------------------------------------------------------------------------------------
:: name: echof.bat
:: desc: Echoes a text file's contents to the screen
:: usage: echof.bat <filename>
::------------------------------------------------------------------------------------------

echo [CALLING] %~nx0

set "INPUT_FILE=%~1"

if "%INPUT_FILE%"=="" (
    echo [ERROR] Missing filename.
    echo [USAGE] %~nx0 ^<filename^>
    exit /b 1
)

if not exist "%INPUT_FILE%" (
    echo [ERROR] File not found: %INPUT_FILE%
    exit /b 1
)

type "%INPUT_FILE%"

echo [ENDING] %~nx0
