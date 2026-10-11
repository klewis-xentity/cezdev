::----------------------------------------------------------------------------------------------------------
:: name: path.list.bat
:: desc: Lists all files in a directory and outputs to a JSON file
:: usage: path.list <output.json> <srcdir1> [srcdir2 ...]
:: example: path.list C:\cezdev\meta\files.json C:\src
::----------------------------------------------------------------------------------------------------------
@echo off

echo [CALLING] %~nx0

set "PATHLISTHOME=%CD%"

:: Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

if "%~1"=="" (
    echo [ERROR] Usage: path.list ^<output.json^> ^<srcdir1^> [srcdir2 ...]
    exit /b 1
)

cd /d "%SCRIPT_DIR%"
set "PYTHON_CMD="
set "PYTHON_ARGS="

where pythonx >nul 2>nul
if "%ERRORLEVEL%"=="0" (
    set "PYTHON_CMD=pythonx"
) else (
    where python >nul 2>nul
    if "%ERRORLEVEL%"=="0" (
        set "PYTHON_CMD=python"
    ) else (
        where py >nul 2>nul
        if "%ERRORLEVEL%"=="0" (
            set "PYTHON_CMD=py"
            set "PYTHON_ARGS=-3"
        )
    )
)

if not defined PYTHON_CMD (
    if defined PYTHON_HOME if exist "%PYTHON_HOME%\python.exe" (
        set "PYTHON_CMD=%PYTHON_HOME%\python.exe"
    )
)

if not defined PYTHON_CMD (
    if defined python.home if exist "%python.home%\python.exe" (
        set "PYTHON_CMD=%python.home%\python.exe"
    )
)

if not defined PYTHON_CMD (
    if defined python.bin if exist "%python.bin%\python.exe" (
        set "PYTHON_CMD=%python.bin%\python.exe"
    )
)

if not defined PYTHON_CMD (
    if defined CBINARIES_HOME (
        for /d %%D in ("%CBINARIES_HOME%\python\python-*") do (
            if exist "%%~fD\python.exe" if not defined PYTHON_CMD set "PYTHON_CMD=%%~fD\python.exe"
        )
    )
)

if not defined PYTHON_CMD (
    for /d %%D in ("%SCRIPT_DIR%\..\..\cplatform\cbinaries\python\python-*") do (
        if exist "%%~fD\python.exe" if not defined PYTHON_CMD set "PYTHON_CMD=%%~fD\python.exe"
    )
)

if not defined PYTHON_CMD (
    if exist "%SCRIPT_DIR%\..\..\cplatform\cbinaries\cbinaries.create.bat" (
        call "%SCRIPT_DIR%\..\..\cplatform\cbinaries\cbinaries.create.bat" >nul 2>nul
    )
    if exist "%SCRIPT_DIR%\..\..\cplatform\clibraries\python\python.create.bat" (
        call "%SCRIPT_DIR%\..\..\cplatform\clibraries\python\python.create.bat" >nul 2>nul
    )

    where pythonx >nul 2>nul
    if "%ERRORLEVEL%"=="0" set "PYTHON_CMD=pythonx"
    if not defined PYTHON_CMD (
        where python >nul 2>nul
        if "%ERRORLEVEL%"=="0" set "PYTHON_CMD=python"
    )
    if not defined PYTHON_CMD (
        where py >nul 2>nul
        if "%ERRORLEVEL%"=="0" (
            set "PYTHON_CMD=py"
            set "PYTHON_ARGS=-3"
        )
    )
    if not defined PYTHON_CMD if defined PYTHON_HOME if exist "%PYTHON_HOME%\python.exe" set "PYTHON_CMD=%PYTHON_HOME%\python.exe"
    if not defined PYTHON_CMD if defined python.home if exist "%python.home%\python.exe" set "PYTHON_CMD=%python.home%\python.exe"
    if not defined PYTHON_CMD if defined python.bin if exist "%python.bin%\python.exe" set "PYTHON_CMD=%python.bin%\python.exe"
)

if not defined PYTHON_CMD (
    echo [ERROR] Python runtime not found. Tried PATH, env vars, local cbinaries, and bootstrap scripts.
    cd /d "%PATHLISTHOME%"
    exit /b 1
)

call "%PYTHON_CMD%" %PYTHON_ARGS% "%SCRIPT_DIR%\PathListCommand.py" %*
if errorlevel 1 (
    echo [ERROR] Failed to run PathListCommand.py with "%PYTHON_CMD%" %PYTHON_ARGS%
    cd /d "%PATHLISTHOME%"
    exit /b 1
)
cd /d "%PATHLISTHOME%"

echo [ENDING] %~nx0