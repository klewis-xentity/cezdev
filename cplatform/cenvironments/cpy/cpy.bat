::---------------------------------------------------------------------------------------------------
:: name: cpy.bat
:: desc: Syntax-checks and runs a Python script
:: usage: cpy <script.py> [args...]
::---------------------------------------------------------------------------------------------------

@echo off
setlocal
set "SCRIPT_NAME=%~nx0"
echo [CALLING] %SCRIPT_NAME%
set "CPYHOME=%CD%"

if "%~1"=="" goto NOPARAM

set "TARGET=%~1"
shift

set "RUN_ARGS="
:CAPTURE_ARGS
if "%~1"=="" goto CAPTURE_DONE
set "RUN_ARGS=%RUN_ARGS% "%~1""
shift
goto CAPTURE_ARGS

:CAPTURE_DONE

if not exist "%TARGET%" (
    echo [ERROR] Python file not found: %TARGET%
    goto DONE
)

for %%I in ("%TARGET%") do set "TARGET_DIR=%%~dpI"
if "%TARGET_DIR:~-1%"=="\" set "TARGET_DIR=%TARGET_DIR:~0,-1%"
set "PROJECT_DIR=%TARGET_DIR%"
set "SETUP_CFG="

:FIND_PACKAGE
if exist "%PROJECT_DIR%\setup.cfg" (
    set "SETUP_CFG=%PROJECT_DIR%\setup.cfg"
    goto PROJECT_FOUND
)
if exist "%PROJECT_DIR%\pyproject.toml" (
    set "SETUP_CFG=%PROJECT_DIR%\pyproject.toml"
    goto PROJECT_FOUND
)

pushd "%PROJECT_DIR%\.." >nul 2>nul
if errorlevel 1 goto NO_PROJECT
set "PARENT_DIR=%CD%"
popd >nul

if /I "%PARENT_DIR%"=="%PROJECT_DIR%" goto NO_PROJECT
if "%PARENT_DIR%"=="" goto NO_PROJECT
set "PROJECT_DIR=%PARENT_DIR%"
goto FIND_PACKAGE

:PROJECT_FOUND
echo [MODE] Package-based Python project detected
echo [INFO] Using package config: %SETUP_CFG%
echo [INSTALL] pip install -e "%PROJECT_DIR%"
pip install -e "%PROJECT_DIR%" --quiet
if errorlevel 1 (
    echo [ERROR] Package install failed.
    goto DONE
)
echo [CHECK] python -m py_compile "%TARGET%"
python -m py_compile "%TARGET%"
if errorlevel 1 (
    echo [ERROR] Syntax check failed.
    goto DONE
)
echo [RUN] python "%TARGET%"%RUN_ARGS%
python "%TARGET%"%RUN_ARGS%
goto DONE

:NO_PROJECT
echo [MODE] Plain Python script - syntax check and run
echo [CHECK] python -m py_compile "%TARGET%"
python -m py_compile "%TARGET%"
if errorlevel 1 (
    echo [ERROR] Syntax check failed.
    goto DONE
)
echo [RUN] python "%TARGET%"%RUN_ARGS%
python "%TARGET%"%RUN_ARGS%
goto DONE

:NOPARAM
echo [MODE] No script provided - launching Python REPL
call python

:DONE
cd /d "%CPYHOME%"
echo [ENDING] %SCRIPT_NAME%
endlocal
