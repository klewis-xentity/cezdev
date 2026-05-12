::------------------------------------------------------------------------------------------
:: name: cjava.bat
:: desc: Runs a Java program or class file
:: usage: cjava ClassName [args...]
::------------------------------------------------------------------------------------------

@echo off
setlocal
echo [CALLING] %~nx0
set "CJAVAHOME=%CD%"

if "%C3DCLASSES_NAME%"=="" set "C3DCLASSES_NAME=c3dclassessdk"
if "%C3DCLASSES_VERSION%"=="" set "C3DCLASSES_VERSION=1.0"

if "%C3DCLASSES_JAVA%"=="" (
    set "C3DCLASSES_JAVA=%~dp0..\..\..\cdata\cmetadata\c3dclasses_java"
)

if "%C3DCLASSES_JAR%"=="" (
    set "C3DCLASSES_JAR=%C3DCLASSES_JAVA%\target\%C3DCLASSES_NAME%-%C3DCLASSES_VERSION%-jar-with-dependencies.jar"
)

echo [INFO] Using C3DCLASSES_JAVA: %C3DCLASSES_JAVA%
echo [INFO] Using C3DCLASSES_JAR: %C3DCLASSES_JAR%

if not exist "%C3DCLASSES_JAVA%\target" (
    echo [ERROR] Java target directory does not exist: %C3DCLASSES_JAVA%\target
    endlocal
    exit /b 1
)

if "%~1"=="" (
    echo [MODE] No class argument provided - running java with current arguments
) else (
    echo [MODE] Running Java class: %~1
)

echo [INFO] Launching from: %C3DCLASSES_JAVA%\target
cd /d "%C3DCLASSES_JAVA%\target"
call java -cp "%CJAVAHOME%;%C3DCLASSES_JAR%" %*

cd /d "%CJAVAHOME%"
echo [ENDING] %~nx0
endlocal