::------------------------------------------------------
:: name: CEZDEV.bat
:: desc: starts the CEZDEV application
::------------------------------------------------------

@echo off

echo.
echo ::---------------------------------------------------------------------------------------
echo ::  (cezdev.bat - begin) Starting EasyDeveloper (CEZDEV)...
echo ::---------------------------------------------------------------------------------------
echo [CALLING] %~nx0

echo [SETTING] CEZDEV_HOME to %~dp0
set CEZDEV_HOME=%~dp0

::--------------------------------------------------------------------------------------------
:: Call the create script to initialize environment variables and directories
::--------------------------------------------------------------------------------------------
echo.
echo ::---------------------------------------------------------------------------------------
echo :: (cezdev.create.bat - begin) Initializing environment variables and directories...
echo ::---------------------------------------------------------------------------------------
call "%CEZDEV_HOME%ccore\cezdev\cezdev.create.bat"
echo ::---------------------------------------------------------------------------------------
echo :: (cezdev.create.bat - end) Initializing environment variables and directories...
echo ::---------------------------------------------------------------------------------------
echo.

echo.
echo ::---------------------------------------------------------------------------------------
echo :: (clibraries.*.create.bat - begin) Initializing libraries...
echo ::---------------------------------------------------------------------------------------
call scripts.call clibraries.*.create.bat
echo ::---------------------------------------------------------------------------------------
echo :: (clibraries.*.create.bat - end) Initializing libraries...
echo ::---------------------------------------------------------------------------------------
echo.

echo.
echo ::---------------------------------------------------------------------------------------
echo :: (cenvironments.*.create.bat - begin) Initializing environments...
echo ::---------------------------------------------------------------------------------------
call scripts.call cenvironments.*.create.bat
echo ::---------------------------------------------------------------------------------------
echo :: (cenvironments.*.create.bat - end) Initializing environments...
echo ::---------------------------------------------------------------------------------------
echo.

echo.
echo ::---------------------------------------------------------------------------------------  
echo :: (cprojects.*.create.bat - begin) Initializing projects...
echo ::---------------------------------------------------------------------------------------
call scripts.call cprojects.*.create.bat
echo ::---------------------------------------------------------------------------------------
echo :: (cprojects.*.create.bat - end) Initializing projects...
echo ::---------------------------------------------------------------------------------------
echo.

::------------------------------------------------------
:: Optional: Start file monitoring (uncomment to enable)
::------------------------------------------------------
::set "MONITOR_CALLBACK=%CENVIRONMENTS%\cjava\cjava.modified.bat,%CENVIRONMENTS%\cjs\cjs.modified.bat,%CENVIRONMENTS%\cpy\cpy.modified.bat"
::start file.monitor.bat "%CCORE%,%CPLATFORM%" "%MONITOR_CALLBACK%"

::------------------------------------------------------
:: Return to CEZDEV home
::------------------------------------------------------
cd /d "%CEZDEV_HOME%"
echo [INFO] Current directory: %CD%
echo [ENDING] %~nx0
echo ::---------------------------------------------------------------------------------------
echo ::  (cezdev.bat - end) System ready
echo ::---------------------------------------------------------------------------------------
echo.


::------------------------------------------------------
:: Launch interactive terminal
::------------------------------------------------------
cmd