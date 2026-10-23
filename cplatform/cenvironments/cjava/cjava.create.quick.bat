::------------------------------------------------------------------------------------------
:: name: cjava.create.quick.bat
:: desc: creates the Java environment into memory using quick Maven build
::------------------------------------------------------------------------------------------
@echo off

echo [CREATING] CJavaEnvironment - cezdev java environment (quick) ...

:: Save the current directory
set "CJAVACREATEHOME=%CD%"
echo [CALLING] %~nx0
echo [STARTED] %date% %time%
set "START_TIME=%time%"

echo [INFO] Current directory: %CD%
echo [INFO] Script directory: %~dp0
set CJAVA_HOME=%~dp0
set CJAVA_NAME=cjava
set "C3DCLASSES_NAME=c3dclassessdk"
set "C3DCLASSES_VERSION=1.0"
if "%C3DCLASSES_JAVAPATH%"=="" set "C3DCLASSES_JAVAPATH=%CMETADATA%\c3dclasses_java"
set "C3DCLASSES_JARFILE=%C3DCLASSES_JAVAPATH%\target\%C3DCLASSES_NAME%-%C3DCLASSES_VERSION%-jar-with-dependencies.jar"

:: Adding the Java environment to PATH
echo [ADDING] CJavaEnvironment to PATH: %CJAVA_HOME%
call path.set.bat %CJAVA_HOME%

if exist "%C3DCLASSES_JARFILE%" (
   echo [INFO] Found existing JAR file: %C3DCLASSES_JARFILE% skipping Maven build. Delete the JAR file to force a rebuild.
) else (
   :: Moving the Java environment from core source to Java project
   call cjava.move.bat

   :: Quick building the Java environment
   call cjava.build.quick.bat
   if errorlevel 1 (
      cd /d "%CJAVACREATEHOME%"
      exit /b %ERRORLEVEL%
   )
)

echo [GENERATING] Generating c3dclassessdk filenames JSON...
call path.list.bat "%CMETADATA%\c3dclassessdk.filenames.json" "%src%"

echo [GENERATING] Generating c3dclasses_java filenames JSON...
call path.list.bat "%CMETADATA%\c3dclasses_java.filenames.json" "%dst%"

echo [GENERATING] Generating c3dclasses filenames JSON...
call path.list.bat "%CMETADATA%\c3dclasses.filenames.json" "%src%"

:: Return to the original directory
cd /d "%CJAVACREATEHOME%"
echo [FINISHED] %date% %time%
