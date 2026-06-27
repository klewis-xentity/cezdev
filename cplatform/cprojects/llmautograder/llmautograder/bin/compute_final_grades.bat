::-------------------------------------------------------------------------------------------------------
:: Name: compute_final_grades.bat
:: Usage: compute_final_grades <submission_filename_of_ids> <rubricfilename.json>
:: Output: Outputs the grading results to a JSON file in the data/compute_final_grade/m_grades.json file.
:: Example: compute_final_grades filename_of_ids.txt rubricfilename.json
:: Example: compute_final_grades submission_ids_test.txt rubric.json
::-------------------------------------------------------------------------------------------------------
@echo off
:: Check if a file was provided as an argument
if "%~1"=="" (
    echo Usage: %~nx0 [filename]
    echo Please specify the path to the submission IDs file.
    exit /b
)

set cautograderdirpath=C:/Users/kevle/Desktop/cezdev/cprojects/autograder
set idfilepath=%cautograderdirpath%/data/submission_ids/%~1

:: Check if the specified file existscgenera
if not exist "%idfilepath%" (
    echo File "%idfilepath%" not found!
    exit /b
)

:: set the second argument if it exist
set rubricfilename=%2

:: Process the file
for /F %%i in (%idfilepath%) do (
    :: Check if the 'meta' parameter is provided
    echo Running :: Name: compute_final_grade.bat for ID %%i
    call compute_final_grade.bat %%i %rubricfilename%  
)
echo All commands executed.