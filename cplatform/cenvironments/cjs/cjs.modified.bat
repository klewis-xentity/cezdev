::------------------------------------------------------------------------------------------
:: name: cjs.modified.bat
:: desc: Callback script triggered when JavaScript environment files are modified
:: params: %1=modified_type %2=filepath %3=platform %4=platform_name
::------------------------------------------------------------------------------------------

@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

set "CORE_ROOT=%C3DCLASSES%"
if "%CORE_ROOT%"=="" set "CORE_ROOT=%SCRIPT_DIR%\..\..\..\ccore\c3dclasses"
for %%I in ("%CORE_ROOT%") do set "CORE_ROOT=%%~fI"

set "CMETA_ROOT=%CMETADATA%"
if "%CMETA_ROOT%"=="" set "CMETA_ROOT=%SCRIPT_DIR%\..\..\..\cdata\cmetadata"
for %%I in ("%CMETA_ROOT%") do set "CMETA_ROOT=%%~fI"

set "META_JS_SRC=%CMETA_ROOT%\c3dclasses_js\src"
for %%I in ("%META_JS_SRC%") do set "META_JS_SRC=%%~fI"

echo [CALLING] %~nx0
echo [PARAM] modified_type: %~1
echo [PARAM] filepath: %~2
echo [PARAM] platform: %~3
echo [PARAM] platform_name: %~4

if /I "%~3"=="clibraries" (
	echo [SKIP] Ignoring library change to avoid callback loops.
	goto end
)

echo %~2 | findstr /I /C:"\\site-packages\\" /C:"\\__pycache__\\" >nul
if not errorlevel 1 (
	echo [SKIP] Ignoring Python cache/site-packages change.
	goto end
)

set "SRC_FILE=%~2"
for %%I in ("%SRC_FILE%") do set "SRC_FILE=%%~fI"
for %%I in ("%SRC_FILE%") do set "SRC_EXT=%%~xI"

if /I not "%SRC_EXT%"==".js" (
	echo [SKIP] Non-JavaScript file change.
	goto update
)

if not exist "%SRC_FILE%" (
	echo [SKIP] Source file does not exist: %SRC_FILE%
	goto update
)

set "REL_PATH=%SRC_FILE%"
call set "REL_PATH=%%REL_PATH:%CORE_ROOT%\=%%"
if /I "%REL_PATH%"=="%SRC_FILE%" (
	echo [SKIP] File not under core JavaScript source root: %CORE_ROOT%
	goto update
)

set "DST_FILE=%META_JS_SRC%\%REL_PATH%"
for %%I in ("%DST_FILE%") do set "DST_DIR=%%~dpI"
if not exist "%DST_DIR%" mkdir "%DST_DIR%" >nul 2>&1

copy /Y "%SRC_FILE%" "%DST_FILE%" >nul
if errorlevel 1 (
	echo [ERROR] Failed to mirror file: %SRC_FILE%
	echo [ERROR] Destination: %DST_FILE%
) else (
	echo [MIRROR] %SRC_FILE%
	echo [TO]     %DST_FILE%
)

:update
call "%~dp0cjs.update.bat"

:end
echo [ENDING] %~nx0
endlocal
