::------------------------------------------------------------------------------------------
:: name: cjs.run.bat
:: desc: Runs the JavaScript environment for C3DClasses SDK
::------------------------------------------------------------------------------------------
@echo off
echo [CALLING] %~nx0
set "CJSRUNHOME=%CD%"

echo [RUNNING] webpack dev server...
cd /d "%C3DCLASSES_JS%"
call npm start

cd /d "%CJSRUNHOME%"
echo [ENDING] %~nx0