::------------------------------------------------------------------------------------------
:: name: cjava.read.bat
:: desc: Displays the Java environment configuration for C3DClasses SDK
::------------------------------------------------------------------------------------------

@echo off
setlocal

echo [CALLING] %~nx0

if "%CMETADATA%"=="" (
    echo [ERROR] CMETADATA environment variable is not set.
    endlocal
    exit /b 1
)

::------------------------------------------------------
:: Define C3DClasses SDK metadata
::------------------------------------------------------
if "%C3DCLASSES_NAME%"=="" set "C3DCLASSES_NAME=c3dclassessdk"
if "%C3DCLASSES_VERSION%"=="" set "C3DCLASSES_VERSION=1.0"

::------------------------------------------------------
:: Define Java environment paths
::------------------------------------------------------
set "C3DCLASSES_JAVA=%CMETADATA%\c3dclasses_java"
set "C3DCLASSES_JAR=%C3DCLASSES_JAVA%\target\%C3DCLASSES_NAME%-%C3DCLASSES_VERSION%-jar-with-dependencies.jar"
set "C3DCLASSES_SRCPATH=%C3DCLASSES_JAVA%\src"
set "C3DCLASSES_CLASSPATH=%C3DCLASSES_JAR%;."

echo [INFO] C3DCLASSES_NAME: %C3DCLASSES_NAME%
echo [INFO] C3DCLASSES_VERSION: %C3DCLASSES_VERSION%
echo [INFO] C3DCLASSES: %C3DCLASSES%
echo [INFO] Java project: %C3DCLASSES_JAVA%
echo [INFO] JAR file: %C3DCLASSES_JAR%
echo [INFO] Source path: %C3DCLASSES_SRCPATH%
echo [INFO] Classpath: %C3DCLASSES_CLASSPATH%

::------------------------------------------------------
:: Check if environment exists
::------------------------------------------------------
if exist "%C3DCLASSES_JAVA%" (
    echo [OK] Java environment directory exists.
) else (
    echo [INFO] Java environment directory does NOT exist.
)

if exist "%C3DCLASSES_JAR%" (
    echo [OK] JAR file exists.
) else (
    echo [INFO] JAR file does NOT exist.
)

echo [ENDING] %~nx0
endlocal
exit /b 0
