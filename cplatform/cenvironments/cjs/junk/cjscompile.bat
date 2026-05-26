::---------------------------------------------------------------------------------------------------
:: name: cjscompile.bat
:: desc: Compiles the JavaScript framework using webpack
:: usage: cjscompile [source] [destination]
::---------------------------------------------------------------------------------------------------

@echo off
setlocal

echo [CALLING] %~nx0

set "CJSCOMPILEHOME=%CD%"
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
set "PROPERTIES_JSON=%SCRIPT_DIR%\config\properties.json"

if "%~1"=="" (
    echo [MODE] Compiling JavaScript with default source and destination
) else (
    echo [MODE] Compiling JavaScript with custom source/destination arguments
)

::------------------------------------------------------
:: Define JavaScript environment paths
::------------------------------------------------------
set "C3DCLASSES_JS=%CMETADATA%\c3dclasses_js"
set "NODE_MODULES_DIR=%_CLIBRARIES%\node_modules"

::------------------------------------------------------
:: Ensure Node modules are installed
::------------------------------------------------------
if not exist "%NODE_MODULES_DIR%" (
    echo [ACTION] Node modules not found, initializing...
    call "%SCRIPT_DIR%\cjs.create.bat"
)

::------------------------------------------------------
:: Update environments
::------------------------------------------------------
echo [ACTION] Updating environments...
call cenvironments-update

::------------------------------------------------------
:: Process template files
::------------------------------------------------------
echo [ACTION] Processing configuration templates...

call ctemplate "%PROPERTIES_JSON%" "%SCRIPT_DIR%\config\package.tmp.json" "%_CLIBRARIES%\package.json"
call ctemplate "%PROPERTIES_JSON%" "%SCRIPT_DIR%\config\webpack.config.tmp.js" "%_CLIBRARIES%\webpack.config.js"
call ctemplate "%PROPERTIES_JSON%" "%SCRIPT_DIR%\config\index.tmp.html" "%_CLIBRARIES%\node_app\index.html"

::------------------------------------------------------
:: Compile JavaScript
::------------------------------------------------------
echo [ACTION] Compiling JavaScript...
call cjava c3dclasses.CJSCompileCommand %1 %2 "%PROPERTIES_JSON%" "%SCRIPT_DIR%\config\index.tmp.js" "%_CLIBRARIES%\node_app\index.js"

::------------------------------------------------------
:: Start the app
::------------------------------------------------------
echo [ACTION] Starting webpack development server...
cd /d "%_CLIBRARIES%"
start npm start

cd /d "%CJSCOMPILEHOME%"
echo [ENDING] %~nx0
endlocal