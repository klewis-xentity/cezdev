::---------------------------------------------------------------------------------------------------
:: name: cjsrun.bat
:: desc: Runs the JavaScript/webpack development server
:: usage: cjsrun
::---------------------------------------------------------------------------------------------------

@echo off
setlocal

echo [INFO] Starting %~nx0

set "CJSRUNHOME=%CD%"

::------------------------------------------------------
:: Validate required environment variables
::------------------------------------------------------
if "%CMETADATA%"=="" (
    echo [ERROR] CMETADATA environment variable is not set.
    endlocal
    exit /b 1
)

::------------------------------------------------------
:: Define JavaScript environment paths
::------------------------------------------------------
set "C3DCLASSES_JS=%CMETADATA%\c3dclasses_js"

::------------------------------------------------------
:: Check if project exists
::------------------------------------------------------
if not exist "%C3DCLASSES_JS%" (
    echo [ERROR] JavaScript project directory does not exist:
    echo        %C3DCLASSES_JS%
    echo [INFO] Run cjs.create.bat first.
    endlocal
    exit /b 1
)

::------------------------------------------------------
:: Start development server
::------------------------------------------------------
echo [ACTION] Starting webpack development server...
cd /d "%C3DCLASSES_JS%"
call npm start

cd /d "%CJSRUNHOME%"
echo [INFO] Ending %~nx0
endlocal
