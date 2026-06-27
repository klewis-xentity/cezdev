@echo off

if "%~1"=="" (
    echo [ERROR] Usage: path.set.bat ^<folder_path^>
    exit /b 1
)

set "FOLDER=%~1"

if not exist "%FOLDER%" (
    echo [ERROR] Directory does not exist: "%FOLDER%"
    exit /b 2
)

echo ;%PATH%; | find /I ";%FOLDER%;" >nul
if errorlevel 1 (
    set "PATH=%FOLDER%;%PATH%"
    echo [OK] Added to PATH: "%FOLDER%"
) else (
    echo [SKIP] Already in PATH: "%FOLDER%"
)

exit /b 0