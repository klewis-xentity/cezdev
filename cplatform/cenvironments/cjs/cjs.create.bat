::------------------------------------------------------------------------------------------
:: name: cjs.create.bat
:: desc: Creates the JavaScript environment into memory for C3DClasses SDK
::------------------------------------------------------------------------------------------

@echo off

echo [CALLING] %~nx0

::------------------------------------------------------
:: Save current working directory
::------------------------------------------------------
set "CJSCREATEHOME=%CD%"
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

::------------------------------------------------------
:: Validate required environment variables
::------------------------------------------------------
if "%_CLIBRARIES%"=="" (
    echo [ERROR] _CLIBRARIES environment variable is not set.
    exit /b 1
)

if "%CMETADATA%"=="" (
    echo [ERROR] CMETADATA environment variable is not set.
    exit /b 1
)

if "%C3DCLASSES%"=="" (
    echo [ERROR] C3DCLASSES environment variable is not set.
    exit /b 1
)

::------------------------------------------------------
:: Define C3DClasses SDK metadata
::------------------------------------------------------
if "%C3DCLASSES_NAME%"=="" set "C3DCLASSES_NAME=c3dclassessdk"
if "%C3DCLASSES_VERSION%"=="" set "C3DCLASSES_VERSION=1.0"

::------------------------------------------------------
:: Define JavaScript environment paths
::------------------------------------------------------
set "C3DCLASSES_JS_ENV=cjs"
set "C3DCLASSES_JS_ENV_PATH=%CENVIRONMENTS%\cjs"
set "C3DCLASSES_JS=%CMETADATA%\c3dclasses_js"
set "C3DCLASSES_JS_SRCPATH=%C3DCLASSES_JS%\src"
set "NODE_MODULES_DIR=%_CLIBRARIES%\node_modules"
set "NODE_APP_DIR=%_CLIBRARIES%\node_app"
set "src=%C3DCLASSES%"
set "dst=%C3DCLASSES_JS%"
set "CONFIG_SRC=%SCRIPT_DIR%\config"

echo [INFO] JavaScript project: %C3DCLASSES_JS%
echo [INFO] Source path: %C3DCLASSES_JS_SRCPATH%
echo [INFO] Node modules: %NODE_MODULES_DIR%
echo [INFO] Node app: %NODE_APP_DIR%

echo [ACTION] Syncing JavaScript environment scripts to metadata PATH...
call scripts.copy.bat "%C3DCLASSES_JS_ENV_PATH%" "%CMETADATA%\cenvironments\%C3DCLASSES_JS_ENV%"

::------------------------------------------------------
:: Ensure node_app directory exists
::------------------------------------------------------
if not exist "%NODE_APP_DIR%" (
    echo [ACTION] Creating node_app directory...
    mkdir "%NODE_APP_DIR%"
)

::------------------------------------------------------
:: Install Node modules if not present
::------------------------------------------------------
if exist "%NODE_MODULES_DIR%" (
    echo [OK] Node modules already installed.
    goto DONE_NODE_INIT
)

echo [ACTION] Installing Node modules...
cd /d "%_CLIBRARIES%"

echo [ACTION] Installing React dependencies...
call npm i react react-dom

echo [ACTION] Installing Webpack dependencies...
call npm i --save-dev webpack webpack-dev-server webpack-cli

echo [ACTION] Installing Babel dependencies...
call npm i --save-dev @babel/core babel-loader @babel/preset-env @babel/preset-react html-webpack-plugin

echo [OK] Node modules installed.

:DONE_NODE_INIT

::------------------------------------------------------
:: Build JavaScript package (webpack project)
::------------------------------------------------------
if not exist "%dst%" mkdir "%dst%"
if not exist "%dst%\src" mkdir "%dst%\src"
if not exist "%dst%\tests" mkdir "%dst%\tests"

echo [COPYING] JavaScript source files...
call directory.copy.bat "%src%" "%dst%\src" ".js" "test_,_test.js,UnitTest.js" "unit_test.js"

echo [COPYING] JavaScript test files...
call directory.copy.bat "%src%" "%dst%\tests" ".js" "" "test_,_test.js,UnitTest.js"

echo [COPYING] webpack.config.js...
if exist "%CONFIG_SRC%\webpack.config.tmp.js" copy /Y "%CONFIG_SRC%\webpack.config.tmp.js" "%dst%\webpack.config.js" >nul

echo [COPYING] package.json...
if exist "%CONFIG_SRC%\package.tmp.json" copy /Y "%CONFIG_SRC%\package.tmp.json" "%dst%\package.json" >nul

echo [COPYING] index.html...
if exist "%CONFIG_SRC%\index.tmp.html" copy /Y "%CONFIG_SRC%\index.tmp.html" "%dst%\src\index.html" >nul

echo [COPYING] index.js...
if exist "%CONFIG_SRC%\index.tmp.js" copy /Y "%CONFIG_SRC%\index.tmp.js" "%dst%\src\index.js" >nul

echo [COPYING] App.js...
if exist "%CONFIG_SRC%\App.js" copy /Y "%CONFIG_SRC%\App.js" "%dst%\src\App.js" >nul

echo [COPYING] .babelrc...
if exist "%CONFIG_SRC%\.babelrc" copy /Y "%CONFIG_SRC%\.babelrc" "%dst%\.babelrc" >nul

echo [ACTION] Generating source metadata...
call path.list.bat "%CMETADATA%\c3dclassessdk.filenames.json" "%src%"

cd /d "%dst%"
if not exist "node_modules" (
    echo [ACTION] Installing npm dependencies...
    call npm install
)

echo [ACTION] Building webpack project...
call npm run build

echo [ACTION] Generating project metadata...
call path.list.bat "%CMETADATA%\c3dclasses_js.filenames.json" "%dst%"
call path.list.bat "%CMETADATA%\c3dclasses.filenames.json" "%src%"

::------------------------------------------------------
:: Restore original directory
::------------------------------------------------------
cd /d "%CJSCREATEHOME%"

echo [ENDING] %~nx0
