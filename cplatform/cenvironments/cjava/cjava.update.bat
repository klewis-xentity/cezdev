::-------------------------------------------------------------------------------------------------
:: name: cjava.update.bat
:: desc: updates the java files in c3dclasses project to a maven project
:: usage: cjava.update.bat
::-------------------------------------------------------------------------------------------------

@echo off

set "CJAVAHOME=%CD%"

echo [CALLING] %~nx0

call cjava.build.bat

cd /d "%CJAVAHOME%"

echo [ENDING] %~nx0
