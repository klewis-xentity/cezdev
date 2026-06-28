@echo off
setlocal

if exist "%python.home%\python.exe" (
    "%python.home%\python.exe" %*
    set "EXIT_CODE=%ERRORLEVEL%"
    endlocal & exit /b %EXIT_CODE%
) else (
    echo [ERROR] python.exe not found at "%python.home%\python.exe"
    endlocal & exit /b 1
)
