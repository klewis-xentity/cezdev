::------------------------------------------------------------------------------------------
:: name: cjava.move.bat
:: desc: moves Java source files and pom.xml to the Java metadata project
::------------------------------------------------------------------------------------------
@echo off

echo [MOVING] CJavaEnvironment - moving Java environment from core source to Java project...

set "CJAVA_ENVPATH=%~dp0"
set "C3DCLASSES_COREPATH=%C3DCLASSES%"
set "C3DCLASSES_JAVAPATH=%CMETADATA%\c3dclasses_java"
echo [SETTING] Java core source path from C3DCLASSES: %C3DCLASSES_COREPATH%
echo [SETTING] Java project path: %C3DCLASSES_JAVAPATH%

:: setting the src and dst directories to write from and to
echo [STARTING] Moving Java environment from core source to Java project...
set "src=%C3DCLASSES_COREPATH%"
set "dst=%C3DCLASSES_JAVAPATH%"
if not exist "%dst%" mkdir "%dst%"

:: Copying Java source files from core to Java project
echo [MOVING] Java source files from %src% to %dst%
echo [COPYING] Java source files from %src% to %dst%\src\main\java...
call directory.copy.bat "%src%" "%dst%\src\main\java" ".java" "UnitTest.java,unittest.java" "CUnitTest.java,CMockUnitTest.java"
echo [COPYING] Java test files from %src% to %dst%\src\test\java...
call directory.copy.bat "%src%" "%dst%\src\test\java" ".java" "" "UnitTest.java,unittest.java"

:: Copying pom.xml from Java environment path to Java project
echo [COPYING] pom.xml from: %CJAVA_ENVPATH%\pom.xml to: %dst%\pom.xml
set "POM_SRC=%CJAVA_ENVPATH%\pom.xml"
echo [COPYING] pom.xml from: %POM_SRC%
echo [COPYING] pom.xml to: %dst%\pom.xml
if not exist "%POM_SRC%" (
    echo [ERROR] pom.xml not found: %POM_SRC%
    endlocal
    exit /b 1
)
copy /Y "%POM_SRC%" "%dst%\pom.xml"
if not exist "%dst%\pom.xml" (
    echo [ERROR] Failed to copy pom.xml to %dst%
    endlocal
    exit /b 1
)

echo [ENDING] %~nx0
endlocal
exit /b 0