::------------------------------------------------------------------------------------------
:: name: cjs.build.bat
:: desc: Builds the JavaScript environment for C3DClasses SDK
::------------------------------------------------------------------------------------------
@echo off
echo [CALLING] %~nx0
set "CJSBUILDHOME=%CD%"

cd /d "%C3DCLASSES_JS%"
if not exist "node_modules" (
    echo [INSTALLING] npm dependencies...
    call npm install
)

echo [BUILDING] webpack project...
call npm run build

echo [GENERATING] project metadata...
call path.list.bat "%CMETADATA%\c3dclasses_js.filenames.json" "%C3DCLASSES_JS%"
call path.list.bat "%CMETADATA%\c3dclasses.filenames.json" "%C3DCLASSES%"

cd /d "%CJSBUILDHOME%"
echo [ENDING] %~nx0
