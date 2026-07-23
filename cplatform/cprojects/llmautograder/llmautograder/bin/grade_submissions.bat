::-------------------------------------------------------------------------------------------------------
:: Name: grade_submissions.bat
:: Usage: grade_submissions <ids_filename> [meta]
:: Output: Grades every submission id listed in the ids file by calling grade_submission.bat for each id.
:: Example: grade_submissions submission_ids_test.txt
::          grade_submissions submission_ids_test.txt meta
::-------------------------------------------------------------------------------------------------------

@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "idsfilename=%~1"
set "meta=%~2"
for %%i in ("%~dp0..") do set "cautograderdirpath=%%~fi"
set "idsdirpath=%cautograderdirpath%\data\submission_ids"
set "idfilepath=%idsdirpath%\%idsfilename%"

:: Check if a file was provided as an argument
if "%idsfilename%"=="" (
    echo Usage: %~nx0 ^<ids_filename^> [meta]
    echo Please specify the name of the submission IDs file.
    echo [INFO] Choose an existing file under "%idsdirpath%"
    exit /b 1
)

:: Check if the specified file exists
if not exist "%idfilepath%" (
    echo [ERROR] Submission IDs file not found: "%idfilepath%"
    echo [INFO] Choose an existing file under "%idsdirpath%"
    exit /b 2
)

echo.
echo ============================================================
echo   BATCH GRADING
echo ============================================================
echo   IDs file:   %idfilepath%
echo   Autograder: %cautograderdirpath%
if "%meta%"=="meta" (
    echo   Mode:       Meta Data Grading
) else (
    echo   Mode:       Full Submission Grading
)
echo ============================================================
echo.

set /a total=0, passed=0, failed=0
set "failedids="

:: Process the file
for /F "usebackq eol=# tokens=* delims=" %%i in ("%idfilepath%") do (
    set /a total+=1
    echo ------------------------------------------------------------
    echo   [!total!] Grading submission ID %%i
    echo ------------------------------------------------------------
    if "%meta%"=="meta" (
        call "%~dp0grade_submission.bat" %%i meta
    ) else (
        call "%~dp0grade_submission.bat" %%i
    )
    if errorlevel 1 (
        set /a failed+=1
        set "failedids=!failedids! %%i"
        echo   [FAILED] ID %%i
    ) else (
        set /a passed+=1
        echo   [OK] ID %%i
    )
    echo.
)

echo ============================================================
echo   BATCH GRADING SUMMARY
echo ============================================================
echo   Total processed: !total!
echo   Succeeded:       !passed!
echo   Failed:          !failed!
if not "!failedids!"=="" echo   Failed IDs:     !failedids!
echo ============================================================
endlocal