::-------------------------------------------------------------------------------------------------
:: name: cjs.update.bat
:: desc: updates the javascript files in c3dclasses project to a webpack project structure
:: usage: cjs.update.bat
::-------------------------------------------------------------------------------------------------

@echo off

echo [CALLING] %~nx0

call "%~dp0cjs.create.bat" %1 %2

echo [ENDING] %~nx0
