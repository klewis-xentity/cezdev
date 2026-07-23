::-------------------------------------------------------------------------------------------------------
:: name: convert_grade_file_from_csv_to_json.bat
:: desc: Converts a grades CSV file into a JSON format.
:: usage: convert_grade_file_from_csv_to_json
:: example: convert_grade_file_from_csv_to_json.bat
::-------------------------------------------------------------------------------------------------------
@echo off
setlocal EnableExtensions

for %%i in ("%~dp0..") do set "cautograderdirpath=%%~fi"
set "sdkpath=%cautograderdirpath%\src\c3dclassessdk_py"

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

:: Define input and output paths
set "csvfile=%cautograderdirpath%\data\osfstorage-archive\grades.csv"
set "jsonfile=%cautograderdirpath%\data\convert_grade_file_from_csv_to_json\grades.json"

if not exist "%csvfile%" (
    echo [ERROR] Grades CSV file not found: "%csvfile%"
    echo [INFO] Place the grades.csv file under "%cautograderdirpath%\data\osfstorage-archive"
    exit /b 2
)

echo.
echo ============================================================
echo   CONVERTING GRADES CSV TO JSON
echo ============================================================
echo   Input:  %csvfile%
echo   Output: %jsonfile%
echo ============================================================
echo.

%pythoncmd% "%cautograderdirpath%\src\convert_grade_file_from_csv_to_json.py" "%csvfile%" "%jsonfile%"

set "CONVERT_EXIT_CODE=%ERRORLEVEL%"

echo.
echo ============================================================
if "%CONVERT_EXIT_CODE%"=="0" (
    echo   CONVERSION COMPLETE
) else (
    echo   CONVERSION FAILED: exit code %CONVERT_EXIT_CODE%
)
echo ============================================================
exit /b %CONVERT_EXIT_CODE%