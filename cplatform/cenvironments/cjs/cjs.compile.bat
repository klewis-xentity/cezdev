::------------------------------------------------------------------------------------------
:: name: cjs.compile.bat
:: desc: Creates a production build for the JavaScript environment
::------------------------------------------------------------------------------------------
@echo off
echo [CALLING] %~nx0
set "CJSCOMPILEHOME=%CD%"

echo [BUILDING] Creating production webpack bundle...
cd /d "%C3DCLASSES_JS%"
call npm run build

echo [GENERATING] project metadata...
call path.list.bat "%CMETADATA%\c3dclasses_js.filenames.json" "%C3DCLASSES_JS%"
call path.list.bat "%CMETADATA%\c3dclasses.filenames.json" "%C3DCLASSES%"

echo [SUCCESS] Production build completed.
cd /d "%CJSCOMPILEHOME%"
echo [ENDING] %~nx0