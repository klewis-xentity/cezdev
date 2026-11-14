::------------------------------------------------------------------
:: name: cezdev.vars.bat
:: desc: sets up the global environment vars to be used by cezdev
::------------------------------------------------------------------

:: CEZDEV_HOME
echo [SETTING] CEZDEV_HOME to %~dp0
set CEZDEV_HOME=%~dp0

:: defines where to find all the binary files
echo [SETTING] CBINARIES_DRIVE to %CEZDEV_HOME%cplatform\cbinaries\cbinaries
set CBINARIES_HOME=%CEZDEV_HOME%cplatform\cbinaries\cbinaries

::-----------------------------------------------------------------
:: setup other vars below to be used globally thorughout cezdev
::-----------------------------------------------------------------