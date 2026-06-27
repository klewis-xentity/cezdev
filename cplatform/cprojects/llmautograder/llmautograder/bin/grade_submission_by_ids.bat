::-------------------------------------------------------------------------------------------------------
:: Name: grade_a_submission.bat
:: Usage: grade_a_submission <submission_id> [meta]
:: Output: Outputs the grading results to a JSON file in the data/grade_submission/<submission_id>/ directory.
:: Example: grade_a_submissions filename_of_ids.txt
::          grade_a_submissions filename_of_ids.txt meta
:: Example grade_submission_by_ids
::-------------------------------------------------------------------------------------------------------
set cautograderdirpath=C:/Users/kevle/Desktop/cezdev/cprojects/autograder
set submissionidsfilename=%~1
set submissionidsfilepath=%cautograderdirpath%/data/submission_ids/%~1

echo "name: %submissionidsfilename%"
echo "path: %submissionidsfilepath%"

:: Check if a file was provided as an argument
if "%submissionidsfilepath%"=="" (
    echo Usage: %~nx0 [filename]
    echo Please specify the path to the submission IDs file.
    exit /b
)


:: Check if the specified file existscgenera
if not exist %submissionidsfilepath% (
    echo File %submissionidsfilepath% not found!
    exit /b
)

echo %submissionidsfilename%
echo %submissionidsfilepath%

:: set the second argument if it exist
set meta=%2

:: Process the file
for /F %%i in (%submissionidsfilepath%) do (
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