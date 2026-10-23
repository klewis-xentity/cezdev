::------------------------------------------------------------------------------------------
:: name: cjava.build.quick.bat
:: desc: quick-builds the Java environment for C3DClasses SDK (no install, skip tests)
::------------------------------------------------------------------------------------------
@echo off

if "%src%"=="" set "src=%C3DCLASSES%"
if "%dst%"=="" set "dst=%CMETADATA%\c3dclasses_java"

echo [GENERATING] Generating c3dclassessdk filenames JSON...
call path.list.bat "%CMETADATA%\c3dclassessdk.filenames.json" "%src%"

echo [BUILDING] Maven quick ..........
call cmvn.quick.bat
if errorlevel 1 exit /b %ERRORLEVEL%

echo [GENERATING] Generating c3dclasses_java filenames JSON...
call path.list.bat "%CMETADATA%\c3dclasses_java.filenames.json" "%dst%"

echo [GENERATING] Generating c3dclasses filenames JSON...
call path.list.bat "%CMETADATA%\c3dclasses.filenames.json" "%src%"
