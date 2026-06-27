@echo off

:: Load the mappings
call "%~dp0cbinaries.create.bat"

echo.
echo ==============================
echo CBINARIES Variable Mappings
echo ==============================
echo.

call :print 7zip
call :print apache-maven
call :print comfyui
call :print docker
call :print git
call :print java
call :print make
call :print ngrok
call :print node
call :print notepad++
call :print other
call :print python

goto :eof

:print
call echo %~1.home = %%%~1.home%%
call echo %~1.bin  = %%%~1.bin%%
echo.
goto :eof