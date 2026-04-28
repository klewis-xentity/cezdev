@echo off
setlocal enabledelayedexpansion

::------------------------------------------------------------------------------------------
:: name: setvarx.bat
:: desc: Captures first-line output of a command and forwards it to setvar.
:: usage: setvarx <envvar> <command...>
::------------------------------------------------------------------------------------------

:: First arg is the environment variable name.
set "envvar=%~1"

:: Remove first arg from %*; remaining args are treated as the command.
set "allargs=%*"
for /f "tokens=1* delims= " %%A in ("!allargs!") do set "cmd=%%B"

:: Execute command and capture only the first output line.
for /f "usebackq delims=" %%A in (`!cmd!`) do (
    set "value=%%A"
    goto :done
)

:done
call setvar "%envvar%" "!value!"
endlocal
