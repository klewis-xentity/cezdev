::------------------------------------------------------------------------------------------
:: name: cjs.create.bat
:: desc: Creates the JavaScript environment into memory for C3DClasses SDK
::------------------------------------------------------------------------------------------
@echo off
echo [CALLING] %~nx0
set "CJSCREATEHOME=%CD%"

:: create and set the memory environment variables for the JavaScript environment before moving it to the final location in metadata
set "CJS_HOME=%~dp0"
set "C3DCLASSES_JS_ENV_NAME=cjs"
set "C3DCLASSES_JS_ENV_PATH=%CJS_HOME%"
set "C3DCLASSES_JS=%CMETADATA%\c3dclasses_js"
set "C3DCLASSES_JS_SRCPATH=%C3DCLASSES_JS%\src"
set "CONFIG_SRC=%C3DCLASSES_JS_ENV_PATH%\config"
echo [SETTING] JavaScript project: %C3DCLASSES_JS%
echo [SETTING] Source path: %C3DCLASSES_JS_SRCPATH%
echo [SYNCING] JavaScript environment scripts to metadata PATH ...

:: set the scripts for the JavaScript environment in memory before moving to metadata
set PATH=%C3DCLASSES_JS_ENV_PATH%;%PATH%
echo [SETTING] CJS environment scripts added to PATH: %C3DCLASSES_JS_ENV_PATH%

set "CJS_ENV_READY=0"
if exist "%C3DCLASSES_JS%\package.json" if exist "%C3DCLASSES_JS%\node_modules" if exist "%C3DCLASSES_JS%\dist" (
    set "CJS_ENV_READY=1"
)

if "%CJS_ENV_READY%"=="1" (
    echo [OK] JavaScript environment already moved and built at %C3DCLASSES_JS%. Skipping move/build.
    goto cjs_create_done
)

:: move the JavaScript environment files to the metadata directory and generate the source metadata for the JavaScript environment
call cjs.move.bat

:: build the JavaScript environment in metadata and generate the source metadata for the JavaScript environment
call cjs.build.bat

:cjs_create_done
:: Restore original directory
cd /d "%CJSCREATEHOME%"
echo [ENDING] %~nx0
