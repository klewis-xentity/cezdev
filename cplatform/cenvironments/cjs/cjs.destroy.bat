::------------------------------------------------------------------------------------------
:: name: cjs.destroy.bat
:: desc: Removes the JavaScript environment from C3DClasses SDK
::------------------------------------------------------------------------------------------

@echo off

echo [INFO] Starting %~nx0

::------------------------------------------------------
:: Validate required variables
::------------------------------------------------------
if "%CMETADATA%"=="" (
    echo [ERROR] CMETADATA environment variable is not set.
    exit /b 1
)

::------------------------------------------------------
:: Remove JavaScript environment directory
::------------------------------------------------------
set "C3DCLASSES_JS=%CMETADATA%\c3dclasses_js"

if exist "%C3DCLASSES_JS%" (
    echo [ACTION] Removing: %C3DCLASSES_JS%
    rmdir /s /q "%C3DCLASSES_JS%"
    echo [OK] JavaScript environment removed.
) else (
    echo [INFO] JavaScript environment directory does not exist.
)

::------------------------------------------------------
:: Optionally remove node_modules
::------------------------------------------------------
set "NODE_MODULES_DIR=%_CLIBRARIES%\node_modules"

if exist "%NODE_MODULES_DIR%" (
    echo [INFO] Node modules directory: %NODE_MODULES_DIR%
    echo [INFO] To remove, run: rmdir /s /q "%NODE_MODULES_DIR%"
)

echo [INFO] Ending %~nx0
