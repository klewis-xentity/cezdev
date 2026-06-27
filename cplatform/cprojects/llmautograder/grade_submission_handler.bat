@echo off
setlocal EnableExtensions

set "HANDLER_DIR=%~dp0"
set "LOG_FILE=%HANDLER_DIR%grade_submission.handler.log"
set "OUTPUT_FILE=%HANDLER_DIR%grade_submission.output.txt"
set "CONTROL_NAME=%~1"
set "TEXT_VALUE=%~2"
set "STATE_KEY=ccontrols.state.main-form.cprojects-select-loader-panel.cprojects-select-loader-panel-content.submission_id"
set "SUBMISSION_ID=%TEXT_VALUE%"

if not "%SUBMISSION_ID%"=="" goto run_grade

call cvar.get %STATE_KEY%
call set "SUBMISSION_ID=%%%STATE_KEY%%%"

:run_grade
if "%SUBMISSION_ID%"=="" (
    echo [%DATE% %TIME%] empty submission id for control %CONTROL_NAME% >> "%LOG_FILE%"
    exit /b 1
)

echo [%DATE% %TIME%] grading submission %SUBMISSION_ID% >> "%LOG_FILE%"

(
    echo [%DATE% %TIME%] Running grade_submission.bat for submission %SUBMISSION_ID%
    echo.
    call "%HANDLER_DIR%llmautograder\bin\grade_submission.bat" "%SUBMISSION_ID%"
) > "%OUTPUT_FILE%" 2>&1

set "GRADE_EXIT_CODE=%ERRORLEVEL%"
type "%OUTPUT_FILE%" >> "%LOG_FILE%"
exit /b %GRADE_EXIT_CODE%
