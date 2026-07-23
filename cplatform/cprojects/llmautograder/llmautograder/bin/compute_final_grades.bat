::-------------------------------------------------------------------------------------------------------
:: Name: compute_final_grades.bat
:: Usage: compute_final_grades <submission_filename_of_ids> <rubricfilename.json>
:: Output: Outputs the grading results to a JSON file in the data/compute_final_grade/m_grades.json file.
:: Example: compute_final_grades filename_of_ids.txt rubricfilename.json
:: Example: compute_final_grades submission_ids_test.txt rubric.json
::-------------------------------------------------------------------------------------------------------
@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "idsfilename=%~1"
set "rubricfilename=%~2"
for %%i in ("%~dp0..") do set "cautograderdirpath=%%~fi"
set "idsdirpath=%cautograderdirpath%\data\submission_ids"
set "idfilepath=%idsdirpath%\%idsfilename%"

:: Check if an ids file was provided as an argument
if "%idsfilename%"=="" (
    echo Usage: %~nx0 ^<submission_filename_of_ids^> ^<rubricfilename.json^>
    echo Please specify the name of the submission IDs file.
    echo [INFO] Choose an existing file under "%idsdirpath%"
    exit /b 1
)

:: Check if a rubric filename was provided
if "%rubricfilename%"=="" (
    echo Usage: %~nx0 ^<submission_filename_of_ids^> ^<rubricfilename.json^>
    echo Please specify the rubric filename.
    exit /b 1
)

:: Check if the specified ids file exists
if not exist "%idfilepath%" (
    echo [ERROR] Submission IDs file not found: "%idfilepath%"
    echo [INFO] Choose an existing file under "%idsdirpath%"
    exit /b 2
)

echo.
echo ============================================================
echo   BATCH FINAL GRADING
echo ============================================================
echo   IDs file:   %idfilepath%
echo   Rubric:     %rubricfilename%
echo   Autograder: %cautograderdirpath%
echo ============================================================
echo.

set /a total=0, passed=0, failed=0
set "failedids="

:: Process the file
for /F "usebackq eol=# tokens=* delims=" %%i in ("%idfilepath%") do (
    set /a total+=1
    echo ------------------------------------------------------------
    echo   [!total!] Computing final grade for ID %%i
    echo ------------------------------------------------------------
    call "%~dp0compute_final_grade.bat" %%i %rubricfilename%
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
echo   BATCH FINAL GRADING SUMMARY
echo ============================================================
echo   Total processed: !total!
echo   Succeeded:       !passed!
echo   Failed:          !failed!
if not "!failedids!"=="" echo   Failed IDs:     !failedids!
echo ============================================================
endlocal