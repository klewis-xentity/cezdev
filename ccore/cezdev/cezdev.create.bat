@echo off
echo [CALLING] %~nx0
::------------------------------------------------------
:: Save baseline environment variables
::------------------------------------------------------
set CBASEENVVARS=%~1cdata\cmetadata\baseline_vars.txt
echo [SAVE] %CBASEENVVARS%
set > "%CBASEENVVARS%"

::------------------------------------------------------
:: Initialize CEZDEV variables
::------------------------------------------------------
echo [INIT] CEZDEV variables...
set CEZDEV_VERSION=1.0
set CEZDEV_NAME=CEZDEV
set CEZDEV_DEBUG=true
set CEZDEV_HOME=%~1
set CCORE=%CEZDEV_HOME%ccore
set CPLATFORM=%CEZDEV_HOME%cplatform
set CEZDEV=%CCORE%\cezdev
set C3DCLASSES=%CCORE%\c3dclasses
set CLIBRARIES=%CPLATFORM%\clibraries
set _CLIBRARIES=%CPLATFORM%\clibraries
set CBOOT=%_CLIBRARIES%\cboot
set CVIDEOS=%CEZDEV_HOME%cdata\cvideo
set CMETADATA=%CEZDEV_HOME%cdata\cmetadata
set CDATA=%CEZDEV_HOME%cdata\cdata
set CPROJECTS=%CPLATFORM%\cprojects
set CENVIRONMENTS=%CPLATFORM%\cenvironments
set CMETADATA_CVARS=%CMETADATA%\cvars.json
set CMEMORY_DRIVER=json
set CWSL=\\wsl.localhost\Ubuntu\home\c3dclasses
set PATH=%CEZDEV%;%PATH%

::------------------------------------------------------
:: Ensure required directories exist
::------------------------------------------------------
IF NOT EXIST "%CMETADATA%" (
    mkdir "%CMETADATA%"
    echo [MKDIR] %CMETADATA%
)
IF NOT EXIST "%CLIBRARIES%" (
    mkdir "%CLIBRARIES%"
    echo [MKDIR] %CLIBRARIES%
)
IF NOT EXIST "%CVIDEOS%" (
    mkdir "%CVIDEOS%"
    echo [MKDIR] %CVIDEOS%
)

scripts.copy.bat "%C3DCLASSES%" "%CMETADATA%\c3dclasses_bat"

echo [ENDING] %~nx0
