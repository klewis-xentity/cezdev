::---------------------------------------------------------------------------------------------------
:: name: cjs.bat
:: desc: Compiles (syntax check) and runs JavaScript using Node.js
:: usage: cjs <script.js> [args...]
::---------------------------------------------------------------------------------------------------

@echo off
setlocal
set "SCRIPT_NAME=%~nx0"
echo [CALLING] %SCRIPT_NAME%
set "CJSHOME=%CD%"

if "%~1"=="" goto NOPARAM

set "TARGET=%~1"
shift

set "RUN_ARGS="
:CAPTURE_ARGS
if "%~1"=="" goto CAPTURE_DONE
set "RUN_ARGS=%RUN_ARGS% \"%~1\""
shift
goto CAPTURE_ARGS

:CAPTURE_DONE

if not exist "%TARGET%" (
    echo [ERROR] JavaScript file not found: %TARGET%
    goto DONE
)

for %%I in ("%TARGET%") do set "TARGET_DIR=%%~dpI"
if "%TARGET_DIR:~-1%"=="\" set "TARGET_DIR=%TARGET_DIR:~0,-1%"
set "PROJECT_DIR=%TARGET_DIR%"
set "PACKAGE_JSON="

:FIND_PACKAGE
if exist "%PROJECT_DIR%\package.json" (
    set "PACKAGE_JSON=%PROJECT_DIR%\package.json"
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
echo [MODE] Package-based JavaScript detected
echo [INFO] Using package.json: %PACKAGE_JSON%
echo [BUILD] npm run build
cd /d "%PROJECT_DIR%"
call npm run build
if errorlevel 1 (
    echo [ERROR] Build failed.
    goto DONE
)

if /I "%CJS_RUN_AFTER_BUILD%"=="true" (
    echo [RUN] npm start
    call npm start
) else (
    echo [INFO] Build succeeded. Set CJS_RUN_AFTER_BUILD=true to also run npm start.
)
goto DONE

:NO_PROJECT
echo [MODE] Plain JavaScript file - compile and run with Node
echo [CHECK] node --check "%TARGET%"
node --check "%TARGET%"
if errorlevel 1 (
    echo [ERROR] Syntax check failed.
    goto DONE
)

echo [RUN] node "%TARGET%"%RUN_ARGS%
node "%TARGET%"%RUN_ARGS%
goto DONE

:NOPARAM
echo [MODE] No script provided
echo [USAGE] cjs ^<script.js^> [args...]

:DONE
cd /d "%CJSHOME%"
echo [ENDING] %SCRIPT_NAME%
endlocal
