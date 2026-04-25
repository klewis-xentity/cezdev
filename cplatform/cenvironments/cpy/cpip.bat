::------------------------------------------------------------------------------------------
:: name: cpip.bat
:: desc: runs the pip program
:: usage cpip install <module-name>
::------------------------------------------------------------------------------------------
@echo off
setlocal
set "CPIPHOME=%CD%"

:: If no arguments, show usage
IF "%~1"=="" (
    echo [USAGE] cpip install ^<module-name^>
    echo [USAGE] cpip --version
    GOTO DONE
)

:: runs pip to do things like install
call python -m pip %*

GOTO DONE
:DONE
cd /d "%CPIPHOME%"
endlocal
