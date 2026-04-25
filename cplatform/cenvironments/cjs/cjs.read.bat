::------------------------------------------------------------------------------------------
:: name: cjs.read.bat
:: desc: Displays the JavaScript environment configuration for C3DClasses SDK
::------------------------------------------------------------------------------------------

@echo off

echo [INFO] Starting %~nx0

::------------------------------------------------------
:: Define C3DClasses SDK metadata
::------------------------------------------------------
if "%C3DCLASSES_NAME%"=="" set "C3DCLASSES_NAME=c3dclassessdk"
if "%C3DCLASSES_VERSION%"=="" set "C3DCLASSES_VERSION=1.0"

::------------------------------------------------------
:: Define JavaScript environment paths
::------------------------------------------------------
set "C3DCLASSES_JS=%CMETADATA%\c3dclasses_js"
set "C3DCLASSES_JS_SRCPATH=%C3DCLASSES_JS%\src"
set "NODE_MODULES_DIR=%_CLIBRARIES%\node_modules"
set "NODE_APP_DIR=%_CLIBRARIES%\node_app"

echo [INFO] C3DCLASSES_NAME: %C3DCLASSES_NAME%
echo [INFO] C3DCLASSES_VERSION: %C3DCLASSES_VERSION%
echo [INFO] JavaScript project: %C3DCLASSES_JS%
echo [INFO] Source path: %C3DCLASSES_JS_SRCPATH%
echo [INFO] Node modules: %NODE_MODULES_DIR%
echo [INFO] Node app: %NODE_APP_DIR%

::------------------------------------------------------
:: Check if environments exist
::------------------------------------------------------
if exist "%C3DCLASSES_JS%" (
    echo [OK] JavaScript environment directory exists.
) else (
    echo [INFO] JavaScript environment directory does NOT exist.
)

if exist "%NODE_MODULES_DIR%" (
    echo [OK] Node modules directory exists.
) else (
    echo [INFO] Node modules directory does NOT exist.
)

echo [INFO] Ending %~nx0
