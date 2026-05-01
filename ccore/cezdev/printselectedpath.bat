@echo off
::------------------------------------------------------------------------------------------
:: name: printselectedpath.bat
:: desc: Prints only the selected folder path from select onchange payload.
:: usage: printselectedpath <controlId> <selectedKey> <selectedPath>
::------------------------------------------------------------------------------------------
echo [CALLING] %~nx0

:: Save the current directory
set "PRINT_SELECTED_PATH_HOME=%CD%"

:: Change to the script's directory
cd /d "%~dp0"

:: Compile if class does not exist
if exist "PrintSelectedPathCommand.class" goto :run
javac PrintSelectedPathCommand.java
if errorlevel 1 (
    echo [ERROR] Failed to compile PrintSelectedPathCommand.java
    cd /d "%PRINT_SELECTED_PATH_HOME%"
    exit /b 1
)

:run
:: Run the Java command (pass all arguments)
java PrintSelectedPathCommand %*

echo [ENDING] %~nx0

:: Return to the original directory
cd /d "%PRINT_SELECTED_PATH_HOME%"
