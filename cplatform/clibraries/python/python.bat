@echo off
setlocal

if not exist "%python.home%\python.exe" (
    echo [ERROR] python.exe not found at "%python.home%\python.exe"
    endlocal & exit /b 1
)

"%python.home%\python.exe" %*
endlocal & exit /b %ERRORLEVEL%
