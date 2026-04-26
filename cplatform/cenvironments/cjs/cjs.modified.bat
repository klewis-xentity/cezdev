::------------------------------------------------------------------------------------------
:: name: cjs.modified.bat
:: desc: Callback script triggered when JavaScript environment files are modified
:: params: %1=modified_type %2=filepath %3=platform %4=platform_name
::------------------------------------------------------------------------------------------

@echo off
setlocal

echo [CALLING] %~nx0
echo [PARAM] modified_type: %~1
echo [PARAM] filepath: %~2
echo [PARAM] platform: %~3
echo [PARAM] platform_name: %~4

call "%~dp0cjs.update.bat"

:end
echo [ENDING] %~nx0
endlocal
