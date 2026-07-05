@echo off
echo [CALLING] %~nx0

::------------------------------------------------------
:: Initialize CEZDEV variables
::------------------------------------------------------
echo [SETTING] CEZDEV environment variables ...
set CEZDEV_VERSION=1.0
set CEZDEV_NAME=CEZDEV
set CEZDEV_DEBUG=true
set CCORE=%CEZDEV_HOME%ccore
set CPLATFORM=%CEZDEV_HOME%cplatform
set CEZDEV=%CCORE%\cezdev
set C3DCLASSES=%CCORE%\c3dclasses
set CBINARIES=%CPLATFORM%\cbinaries
set CLIBRARIES=%CPLATFORM%\clibraries
set CBOOT=%_CLIBRARIES%\cboot
set CVIDEOS=%CEZDEV_HOME%cdata\cvideo
set CMETADATA=%CEZDEV_HOME%cdata\cmetadata
set CDATA=%CEZDEV_HOME%cdata\cdata
set CPROJECTS=%CPLATFORM%\cprojects
set CENVIRONMENTS=%CPLATFORM%\cenvironments
set CMETADATA_CVARS=%CMETADATA%\cvars.json
set CMEMORY_DRIVER=json
set CWSL=\\wsl.localhost\Ubuntu\home\c3dclasses

echo [ADDING] cezdev scripts directory to PATH ...
set PATH=%CEZDEV%;%PATH%

::------------------------------------------------------
:: Initialize cbinaries environment variables
::------------------------------------------------------
echo [SETTING] cbinaries environment variables %CBINARIES%\cbinaries.create.bat
call "%CBINARIES%\cbinaries.create.bat"
echo [ADDING] cbinaries scripts directory to PATH ...
set PATH=%CBINARIES%;%PATH%

::------------------------------------------------------
:: Ensure required directories exist
::------------------------------------------------------
echo [CHECKING] Required directories ...
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

::------------------------------------------------------
:: Save baseline environment variables
::------------------------------------------------------
echo [SAVING] Baseline environment variables ...
set CBASEENVVARS=%~1cdata\cmetadata\baseline_vars.txt
echo [SAVING] %CBASEENVVARS%
set > "%CBASEENVVARS%"

echo [MOVING] C3DClasses Commands (.bat) - moving commands from core source to projects...
scripts.copy.bat "%C3DCLASSES%" "%CMETADATA%\c3dclasses_bat"

echo [ENDING] %~nx0
