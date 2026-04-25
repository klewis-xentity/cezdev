::-------------------------------------------------------------------------------------------------
:: name: cpy.update.bat
:: desc: updates the python files in c3dclasses project to a python package project
:: usage: cpy.update.bat
::-------------------------------------------------------------------------------------------------

@echo off

echo [CALLING] %~nx0

call "%~dp0cpy.create.bat" %1 %2

echo [ENDING] %~nx0