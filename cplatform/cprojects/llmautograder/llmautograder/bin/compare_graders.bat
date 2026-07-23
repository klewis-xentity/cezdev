::-------------------------------------------------------------------------------------------------------
:: Name: compare_graders.bat
:: Usage: compare_graders <submission_ids_filename>
:: Output: Compares human and machine grades and writes results to data/compare_graders/.
:: Example: compare_graders submission_ids_test.txt
::-------------------------------------------------------------------------------------------------------
@echo off
setlocal EnableExtensions

set "submissionidsfilename=%~1"
for %%i in ("%~dp0..") do set "cautograderdirpath=%%~fi"
set "sdkpath=%cautograderdirpath%\src\c3dclassessdk_py"

if "%submissionidsfilename%"=="" (
    echo Usage: %~nx0 ^<submission_ids_filename^>
    exit /b 1
)

if defined PYTHONPATH (
    set "PYTHONPATH=%sdkpath%;%PYTHONPATH%"
) else (
    set "PYTHONPATH=%sdkpath%"
)

python "%cautograderdirpath%\src\compare_graders.py" main "%submissionidsfilename%"