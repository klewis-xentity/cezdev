::---------------------------------------------------------------------------------------------------
:: name: cjavac.bat
:: desc: Compiles the entire C3DClasses framework or compiles single files against the framework
:: usage: cjavac [JavaFile.java ...]
::---------------------------------------------------------------------------------------------------
@echo off
set "CJAVACHOME=%CD%"
echo [CALLING] %~nx0

if "%1"=="" goto NOPARAM
echo [MODE] Compile provided Java source file(s)
javac -classpath "%C3DCLASSES_CLASSPATH%" %*
goto DONE

:NOPARAM
echo [MODE] No file arguments provided - refreshing Java environment
call cjava.update.bat
goto DONE

:DONE
echo [ENDING] %~nx0
cd /d "%CJAVACHOME%"
