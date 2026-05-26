
@echo off
setlocal

:: move the JavaScript source files and configuration from the C3DCLASSES directory to the metadata directory for the JavaScript environment
:: this is done after the JavaScript environment is created in memory and the scripts are added to
set "src=%C3DCLASSES%"
set "dst=%C3DCLASSES_JS%"

if exist "%dst%" (
    echo [OK] JavaScript package already exists at %dst%. Skipping create/build.
    goto DONE_CJS_CREATE
)

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

:DONE_CJS_CREATE
endlocal