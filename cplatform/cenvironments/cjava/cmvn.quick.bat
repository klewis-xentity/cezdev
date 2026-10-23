::------------------------------------------------------------------------------------------
:: name: cmvn.quick.bat
:: desc: runs a quick Maven build (no install, skip tests)
::------------------------------------------------------------------------------------------
@echo off

set "C3DCLASSES_NAME=c3dclassessdk"
set "C3DCLASSES_VERSION=1.0"
set "C3DCLASSES_JARFILE=%C3DCLASSES_JAVAPATH%\target\%C3DCLASSES_NAME%-%C3DCLASSES_VERSION%-jar-with-dependencies.jar"
set "src=%C3DCLASSES%"
set "dst=%C3DCLASSES_JAVAPATH%"

if "%C3DCLASSES_JAVAPATH%"=="" (
   echo [ERROR] C3DCLASSES_JAVAPATH is not set.
   exit /b 1
)
if not exist "%C3DCLASSES_JAVAPATH%\pom.xml" (
   echo [ERROR] pom.xml not found in %C3DCLASSES_JAVAPATH%
   exit /b 1
)

echo [BUILDING] Maven quick build (clean package, skip tests)...
pushd "%dst%"
call mvn clean package -DskipTests -e -Drelease.artifactId=%C3DCLASSES_NAME% -Drelease.version=%C3DCLASSES_VERSION% -Drelease.path=%CEZDEV_HOME% -Dother.home=%other.home%
set "MVN_EXIT_CODE=%ERRORLEVEL%"
if errorlevel 1 (
   echo [ERROR] Maven quick build failed with exit code %MVN_EXIT_CODE%.
)
popd

exit /b %MVN_EXIT_CODE%
