::------------------------------------------------------------------------------------------
:: name: cmvn.bat
:: desc: runs Maven build and generates filenames JSON
::------------------------------------------------------------------------------------------

@echo off

:: Save the current directory
set "CMVN_HOME=%CD%"
echo [CALLING] %~nx0

:: Set defaults for required environment variables
if "%CEZDEV_HOME%"=="" set "CEZDEV_HOME=%CD%"
if "%CMETADATA%"=="" set "CMETADATA=%CEZDEV_HOME%\cdata\cmetadata"
if "%CENVIRONMENTS%"=="" set "CENVIRONMENTS=%CEZDEV_HOME%\cplatform\cenvironments"
if "%C3DCLASSES%"=="" set "C3DCLASSES=%CEZDEV_HOME%\ccore\c3dclasses"
if "%C3DCLASSES_NAME%"=="" set "C3DCLASSES_NAME=c3dclassessdk"
if "%C3DCLASSES_VERSION%"=="" set "C3DCLASSES_VERSION=1.0"
if "%src%"=="" set "src=%C3DCLASSES%"
if "%dst%"=="" set "dst=%CMETADATA%\c3dclasses_java"

echo [INFO] CEZDEV_HOME: %CEZDEV_HOME%
echo [INFO] CMETADATA: %CMETADATA%
echo [INFO] C3DCLASSES_NAME: %C3DCLASSES_NAME%
echo [INFO] C3DCLASSES_VERSION: %C3DCLASSES_VERSION%
echo [INFO] Source directory: %src%
echo [INFO] Destination directory: %dst%

echo [STEP] Generating c3dclassessdk filenames JSON...
call path.list.bat "%CMETADATA%\c3dclassessdk.filenames.json" "%src%"
cd /d "%dst%"
echo [STEP] Running Maven build...
call mvn clean install test -e -Drelease.artifactId=%C3DCLASSES_NAME% -Drelease.version=%C3DCLASSES_VERSION% -Drelease.path=%CEZDEV_HOME%
echo [STEP] Generating c3dclasses_java filenames JSON...
call path.list.bat "%CMETADATA%\c3dclasses_java.filenames.json" "%dst%"
echo [STEP] Generating c3dclasses filenames JSON...
call path.list.bat "%CMETADATA%\c3dclasses.filenames.json" "%src%"

echo [ENDING] %~nx0
cd /d "%CMVN_HOME%"
exit
