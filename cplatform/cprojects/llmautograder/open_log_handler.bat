@echo off
setlocal EnableExtensions

set "HANDLER_DIR=%~dp0"
set "LOG_FILE=%HANDLER_DIR%grade_submission.handler.log"

if not exist "%LOG_FILE%" (
    > "%LOG_FILE%" echo grade_submission.handler.log
)

start "" notepad.exe "%LOG_FILE%"
exit /b 0
