::-------------------------------------------------------------------------------------------------------
:: Name: grade_submission.bat
:: Usage: grade_submission <submission_id> [meta]
:: Output: Outputs the grading results to a JSON file in the data/grade_submission/<submission_id>/ directory.
:: Example: grade_submission 2
::          grade_submission 2 meta
::-------------------------------------------------------------------------------------------------------

@echo off
setlocal EnableExtensions

set "submissionid=%~1"
set "meta=%~2"
for %%i in ("%~dp0..") do set cautograderdirpath=%%~fi
set "sdkpath=%cautograderdirpath%\src\c3dclassessdk_py"

if "%submissionid%"=="" (
    echo Usage: %~nx0 ^<submission_id^> [meta]
    exit /b 1
)

if defined PYTHONPATH (
    set "PYTHONPATH=%sdkpath%;%PYTHONPATH%"
) else (
    set "PYTHONPATH=%sdkpath%"
)

where pythonx >nul 2>nul
if "%ERRORLEVEL%"=="0" (
    set "pythoncmd=pythonx"
) else (
    set "pythoncmd=python"
)

:: Define each parameter separately for better readability and maintainability
set "submissionspath=%cautograderdirpath%\data\assignment_3\submissions\%submissionid%"
set "rubricpath=%cautograderdirpath%\data\rubric_questions.txt"
set "templatecodepath=%cautograderdirpath%\data\assignment_3\template_code"

if not exist "%submissionspath%\" (
    echo [ERROR] Submission folder not found: "%submissionspath%"
    echo [INFO] Choose an existing folder under "%cautograderdirpath%\data\assignment_3\submissions"
    exit /b 2
)

if not exist "%rubricpath%" (
    echo [ERROR] Rubric file not found: "%rubricpath%"
    exit /b 2
)

if not exist "%templatecodepath%\" (
    echo [ERROR] Template code folder not found: "%templatecodepath%"
    exit /b 2
)

:: Construct the command parameters
set "commonparams=%submissionspath% %rubricpath% %templatecodepath%"

echo.
echo ============================================================
echo   GRADING SUBMISSION: %submissionid%
echo ============================================================
echo.

:: Check if the 'meta' parameter is provided
if "%meta%"=="meta" (
    echo Mode: Meta Data Grading
    echo.
    %pythoncmd% "%cautograderdirpath%\src\grade_submission.py" grade_submission_meta_data "%submissionspath%" "%rubricpath%" "%templatecodepath%"
) else (
    echo Mode: Full Submission Grading
    echo.
    %pythoncmd% "%cautograderdirpath%\src\grade_submission.py" grade_submission "%submissionspath%" "%rubricpath%" "%templatecodepath%"
)

set "GRADE_EXIT_CODE=%ERRORLEVEL%"

echo.
echo ============================================================
echo   GRADING COMPLETE
echo ============================================================
exit /b %GRADE_EXIT_CODE%
