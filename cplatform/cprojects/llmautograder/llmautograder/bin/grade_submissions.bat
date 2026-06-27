::-------------------------------------------------------------------------------------------------------
:: Name: grade_a_submission.bat
:: Usage: grade_a_submission <submission_id> [meta]
:: Output: Outputs the grading results to a JSON file in the data/grade_submission/<submission_id>/ directory.
:: Example: grade_a_submission_by_ids filename_of_ids.txt
::          grade_a_submission_by_ids filename_of_ids.txt meta
::-------------------------------------------------------------------------------------------------------
@echo off

call alert "[INFO] Starting grading process for submission IDs from file: %~1"

:: Check if a file was provided as an argument
if "%~1"=="" (
    echo Usage: %~nx0 [filename]
    echo Please specify the path to the submission IDs file.
    exit /b
)

set idfilepath=C:/Users/klewi/Desktop/cautograder/data/submission_ids/%~1

:: Check if the specified file existscgenera
if not exist "%idfilepath%" (
    echo File "%idfilepath%" not found!
    exit /b
)


:: set the second argument if it exist
set meta=%2

:: Process the file
for /F %%i in (%idfilepath%) do (
    :: Check if the 'meta' parameter is provided
    if "%meta%"=="meta" (
        echo Running grade_submission.bat meta for ID %%i
        call grade_submission.bat %%i meta
    ) else (
        echo Running grade_submission.bat for ID %%i
        call grade_submission.bat %%i 
    )
)
echo All commands executed.