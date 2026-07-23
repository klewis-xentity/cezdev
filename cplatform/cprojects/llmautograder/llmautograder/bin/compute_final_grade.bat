::-------------------------------------------------------------------------------------------------------
:: Name: compute_final_grade.bat
:: Usage: compute_final_grade <submission_id>
:: Output: Outputs the grading results to a JSON file in the data/compute_final_grade/m_grades.json file.
:: Example: compute_final_grade 2
:: usage: python.exe ./src/compute_final_grade.py main \
::   "C:/Users/klewi/Desktop/cautograder/data/grade_submission/0.json" 
::   "C:/Users/klewi/Desktop/cautograder/data/rubic/markingRubric/rubric.json"
:: example: compute_final_grade 0 rubric.json
::-------------------------------------------------------------------------------------------------------
@echo off
setlocal EnableExtensions

set "submissionid=%~1"
set "rubricfilename=%~2"
for %%i in ("%~dp0..") do set "cautograderdirpath=%%~fi"
set "sdkpath=%cautograderdirpath%\src\c3dclassessdk_py"

if "%submissionid%"=="" (
    echo Usage: %~nx0 ^<submission_id^> ^<rubric_filename^>
    exit /b 1
)

if "%rubricfilename%"=="" (
    echo Usage: %~nx0 ^<submission_id^> ^<rubric_filename^>
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
    where py >nul 2>nul
    if "%ERRORLEVEL%"=="0" (
        set "pythoncmd=py"
    ) else (
        set "pythoncmd="
        for /f "delims=" %%i in ('where python 2^>nul') do (
            if not defined pythoncmd (
                echo %%i | findstr /i "\\WindowsApps\\python.exe" >nul
                if errorlevel 1 set "pythoncmd=%%i"
            )
        )
        if not defined pythoncmd if exist "%USERPROFILE%\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" (
            set "pythoncmd=%USERPROFILE%\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe"
        )
    )
)

if not defined pythoncmd (
    echo [ERROR] Python was not found. Install Python 3 or add python.exe to PATH.
    exit /b 127
)

if "%pythoncmd%"=="py" (
    set "pythoncmd=py -3"
) else (
    set "pythoncmd="%pythoncmd%""
)

:: Define each parameter separately for better readability and maintainability
set "gradedsubmissionfile=%cautograderdirpath%\data\grade_submission\%submissionid%.json"
if exist "%rubricfilename%" (
    for %%i in ("%rubricfilename%") do set "rubricfile=%%~fi"
) else (
    set "rubricfile=%cautograderdirpath%\data\rubic\markingRubric\%rubricfilename%"
)

if not exist "%gradedsubmissionfile%" (
    echo [ERROR] Graded submission file not found: "%gradedsubmissionfile%"
    echo [INFO] Choose an existing JSON file under "%cautograderdirpath%\data\grade_submission"
    exit /b 2
)

if not exist "%rubricfile%" (
    echo [ERROR] Rubric file not found: "%rubricfile%"
    echo [INFO] You can also pass a full or relative path to a rubric JSON file.
    echo [INFO] Choose an existing rubric file under "%cautograderdirpath%\data\rubic\markingRubric"
    exit /b 2
)

:: Construct the command parameters
set "commonparams=%gradedsubmissionfile% %rubricfile%"

:: execute the command
%pythoncmd% "%cautograderdirpath%\src\compute_final_grade.py" main "%gradedsubmissionfile%" "%rubricfile%"

set "GRADE_EXIT_CODE=%ERRORLEVEL%"

echo.
echo ============================================================
if "%GRADE_EXIT_CODE%"=="0" (
    echo   FINAL GRADE COMPLETE
) else (
    echo   FINAL GRADE FAILED: exit code %GRADE_EXIT_CODE%
)
echo ============================================================
exit /b %GRADE_EXIT_CODE%
