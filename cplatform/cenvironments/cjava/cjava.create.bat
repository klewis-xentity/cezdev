::------------------------------------------------------------------------------------------
:: name: cjava.create.bat
:: desc: creates the Java environment into memory for C3DClasses SDK
::------------------------------------------------------------------------------------------

@echo off

:: Save the current directory
set "CJAVACREATEHOME=%CD%"
echo [CALLING] %~nx0

if "%C3DCLASSES_NAME%"=="" set "C3DCLASSES_NAME=c3dclassessdk"
if "%C3DCLASSES_VERSION%"=="" set "C3DCLASSES_VERSION=1.0"
set "C3DCLASSES_JAVA_ENV=cjava"
set "C3DCLASSES_JAVA_ENV_PATH=%CENVIRONMENTS%\cjava"
set "C3DCLASSES_JAVA=%CMETADATA%\c3dclasses_java"
set "CJAVA_MODIFIED_BAT=%C3DCLASSES_JAVA_ENV_PATH%\cjava.modified.bat"
set "C3DCLASSES_JAR=%C3DCLASSES_JAVA%\target\%C3DCLASSES_NAME%-%C3DCLASSES_VERSION%-jar-with-dependencies.jar"
set "C3DCLASSES_SRCPATH=%C3DCLASSES_JAVA%\src"
set "C3DCLASSES_CLASSPATH=%C3DCLASSES_JAR%;."

echo [INFO] Java project: %C3DCLASSES_JAVA%
echo [INFO] JAR file: %C3DCLASSES_JAR%
echo [INFO] Core Source (C3DCLASSES): %C3DCLASSES%
echo [INFO] Metadata Source path: %C3DCLASSES_SRCPATH%
echo [INFO] Classpath: %C3DCLASSES_CLASSPATH%
echo [INFO] Java environment path: %C3DCLASSES_JAVA_ENV_PATH%
echo [INFO] Java environment name: %C3DCLASSES_JAVA_ENV%

echo [CREATING] Java environment...

call scripts.copy.bat "%C3DCLASSES_JAVA_ENV_PATH%" "%CMETADATA%\cenvironments\%C3DCLASSES_JAVA_ENV%"
set "PATH=%PATH%;%CMETADATA%\cenvironments\%C3DCLASSES_JAVA_ENV%"

if exist "%C3DCLASSES_JAR%" (
    echo [INFO] JAR file already exists at: %C3DCLASSES_JAR%
    echo [SKIPPING] Java environment creation
    goto end
)

echo [REMOVING] Old Java project directory...
if exist "%C3DCLASSES_JAVA%" (
    rmdir /s /q "%C3DCLASSES_JAVA%"
)

:: set the src and dst directories to write from and to
set "src=%C3DCLASSES%"
set "dst=%C3DCLASSES_JAVA%"
if not exist "%dst%" mkdir "%dst%"

echo [COPYING] Java source files...
call directory.copy.bat "%src%" "%dst%\src\main\java" ".java" "UnitTest.java,unittest.java" "CUnitTest.java,CMockUnitTest.java"
echo [COPYING] Java test files...
call directory.copy.bat "%src%" "%dst%\src\test\java" ".java" "" "UnitTest.java,unittest.java"

set "POM_SRC=%C3DCLASSES_JAVA_ENV_PATH%\pom.xml"
echo [COPYING] pom.xml from: %POM_SRC%
echo [COPYING] pom.xml to: %dst%\pom.xml
copy /Y "%POM_SRC%" "%dst%\pom.xml"
if not exist "%dst%\pom.xml" (
    echo [ERROR] Failed to copy pom.xml to %dst%
    exit /b 1
)

start cmvn.bat

:end
echo [ENDING] %~nx0
cd /d "%CJAVACREATEHOME%"