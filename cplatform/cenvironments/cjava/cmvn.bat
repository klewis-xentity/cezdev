::------------------------------------------------------------------------------------------
:: name: cmvn.bat
:: desc: runs Maven build and generates filenames JSON
::------------------------------------------------------------------------------------------
@echo off

set "C3DCLASSES_NAME=c3dclassessdk"
set "C3DCLASSES_VERSION=1.0"
set "C3DCLASSES_JARFILE=%C3DCLASSES_JAVAPATH%\target\%C3DCLASSES_NAME%-%C3DCLASSES_VERSION%-jar-with-dependencies.jar"
set "src=%C3DCLASSES%"
set "dst=%C3DCLASSES_JAVAPATH%"

if "%C3DCLASSES_JAVAPATH%"=="" (
   echo [ERROR] C3DCLASSES_JAVAPATH is not set.
   endlocal
   exit /b 1
)

if not exist "%C3DCLASSES_JAVAPATH%\pom.xml" (
   echo [ERROR] pom.xml not found in %C3DCLASSES_JAVAPATH%
   endlocal
   exit /b 1
)

if exist "%C3DCLASSES_JARFILE%" (
   echo [INFO] Found existing JAR file: %C3DCLASSES_JARFILE% skipping Maven build. Delete the JAR file to force a rebuild.
) else (
   echo [WARNING] JAR file not found: %C3DCLASSES_JARFILE%
   echo [WARNING] Maven build may fail if the JAR file is required for compilation.
   echo [BUILDING] Maven build...
   pushd "%dst%"
   call mvn clean install test -e -Drelease.artifactId=%C3DCLASSES_NAME% -Drelease.version=%C3DCLASSES_VERSION% -Drelease.path=%CEZDEV_HOME% -Dother.home=%other.home%
   set "MVN_EXIT_CODE=%ERRORLEVEL%"
   popd
   if not "%MVN_EXIT_CODE%"=="0" (
      echo [ERROR] Maven build failed with exit code %MVN_EXIT_CODE%.
      endlocal
      exit /b %MVN_EXIT_CODE%
   )
)


