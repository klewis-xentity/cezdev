@echo off
::------------------------------------------------------------------------------------------
:: name: scripts.call.bat
:: desc: Finds and executes create scripts matching a pattern in a directory
:: usage: scripts.call.bat <srcpath> <scriptpattern>
::        scripts.call.bat <scope>.<scriptpattern>
:: example: scripts.call.bat "%CENVIRONMENTS%" "*.create.bat"
::          scripts.call.bat "cenvironments.*.create"
::------------------------------------------------------------------------------------------

echo [CALLING] %~nx0

set "SRCPATH=%~1"
set "SCRIPTPATTERN=%~2"

if "%SCRIPTPATTERN%"=="" (
    call :parse_shorthand "%~1"
)

if "%SRCPATH%"=="" (
    echo [ERROR] Source path of scripts to call not provided.
    goto :end
)

if "%SCRIPTPATTERN%"=="" (
    echo [ERROR] Script pattern of scripts to call not provided.
    goto :end
)

echo [INFO] Source path of scripts to call: %SRCPATH%
echo [INFO] Pattern of scripts to call: %SCRIPTPATTERN%

for /r "%SRCPATH%" %%F in (%SCRIPTPATTERN%) do (
    if exist "%%F" (
        echo [EXECUTING] %%~nxF
        call "%%F"
    )
)

:end
echo [ENDING] %~nx0
goto :eof

:parse_shorthand
set "SCOPE="

for /f "tokens=1* delims=." %%A in ("%~1") do (
    set "SCOPE=%%~A"
    set "SCRIPTPATTERN=%%~B"
)

call :resolve_scope_path "%SCOPE%"
if defined RESOLVED_SCOPE_PATH (
    set "SRCPATH=%RESOLVED_SCOPE_PATH%"
)

if defined SCRIPTPATTERN (
    if /i not "%SCRIPTPATTERN:~-4%"==".bat" set "SCRIPTPATTERN=%SCRIPTPATTERN%.bat"
)

goto :eof

:resolve_scope_path
set "RESOLVED_SCOPE_PATH="

if /i "%~1"=="clibraries" set "RESOLVED_SCOPE_PATH=%CLIBRARIES%"
if /i "%~1"=="cenvironments" set "RESOLVED_SCOPE_PATH=%CENVIRONMENTS%"
if /i "%~1"=="cprojects" set "RESOLVED_SCOPE_PATH=%CPROJECTS%"

goto :eof