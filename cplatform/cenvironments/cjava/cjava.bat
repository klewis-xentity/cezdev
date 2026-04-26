::------------------------------------------------------------------------------------------
:: name: cjava.bat
:: desc: Runs a Java program or class file
:: usage: cjava ClassName [args...]
::------------------------------------------------------------------------------------------

@echo off
setlocal
echo [CALLING] %~nx0
set "CJAVAHOME=%CD%"

if "%C3DCLASSES_JAVA%"=="" (
    echo [ERROR] C3DCLASSES_JAVA environment variable is not set.
    endlocal
    exit /b 1
)

if "%C3DCLASSES_JAR%"=="" (
    echo [ERROR] C3DCLASSES_JAR environment variable is not set.
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
call java -cp "%C3DCLASSES_JAR%;%CJAVAHOME%" %*

cd /d "%CJAVAHOME%"
echo [ENDING] %~nx0
endlocal