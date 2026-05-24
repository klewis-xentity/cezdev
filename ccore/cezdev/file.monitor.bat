@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "LOG_DIR=%TEMP%\cezdev\logs"
set "LOG_FILE=%LOG_DIR%\file.monitor.log"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%" >nul 2>&1

set "PYTHON_CMD="
where python >nul 2>&1 && set "PYTHON_CMD=python"
if not defined PYTHON_CMD (
	where py >nul 2>&1 && set "PYTHON_CMD=py -3"
)

echo [CALLING] %~nx0
if not defined PYTHON_CMD (
	echo [ERROR] Python runtime not found. Install Python or ensure python/py is on PATH.
	>> "%LOG_FILE%" echo [%date% %time%] [ERROR] file.monitor.bat: Python runtime not found. Args: %*
	endlocal
	exit /b 1
)

>> "%LOG_FILE%" echo.
>> "%LOG_FILE%" echo [%date% %time%] [START] %~nx0
>> "%LOG_FILE%" echo [%date% %time%] [INFO] Launcher: !PYTHON_CMD! "%~dp0file.monitor.py" %*

::start "cezdev-file-monitor" /B cmd /c ""cpy" "%~dp0file.monitor.py" %* >> "%LOG_FILE%" 2>&1"

cpy "%~dp0file.monitor.py" %*

if errorlevel 1 (
	echo [ERROR] Failed to start file monitor process.
	>> "%LOG_FILE%" echo [%date% %time%] [ERROR] Failed to start file monitor process.
	endlocal
	exit /b 1
)

echo [INFO] Monitor logs: "%LOG_FILE%"
echo [ENDING] %~nx0
endlocal
