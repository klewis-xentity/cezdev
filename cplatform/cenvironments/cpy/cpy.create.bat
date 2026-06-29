::------------------------------------------------------------------------------------------
:: name: cpy.create.bat
:: desc: initializes Python environment wiring for C3DClasses SDK
::------------------------------------------------------------------------------------------
@echo off
echo [CREATING] CPythonEnvironment - cezdev python environment ...
set "CPYCREATEHOME=%CD%"
echo [CALLING] %~nx0
echo [STARTED] %date% %time%
set "START_TIME=%time%"

echo [INFO] Current directory: %CD%
echo [INFO] Script directory: %~dp0

set "CPY_HOME=%~dp0"
set "CPY_NAME=cpy"
set "C3DCLASSES_PYPATH=%CMETADATA%\c3dclasses_py"

:: Adding the Java environment to PATH
echo [ADDING] CPythonEnvironment to PATH: %CPY_HOME%
set PATH=%PATH%;%CPY_HOME%

set "CPY_ENV_READY=0"
if exist "%C3DCLASSES_PYPATH%\pyproject.toml" if exist "%C3DCLASSES_PYPATH%\c3dclasses" (
	call pythonx.bat -c "import c3dclasses, PyPDF2" >nul 2>nul
	if not errorlevel 1 set "CPY_ENV_READY=1"
)

if "%CPY_ENV_READY%"=="1" (
	echo [OK] Python environment already moved and built at %C3DCLASSES_PYPATH%. Skipping move/build.
	goto cpy_create_done
)

:: Moving the Python environment from core source to Python project
call cpy.move.bat

:: Building the Python environment
call cpy.build.bat

:cpy_create_done
:: Return to the original directory
echo [RETURNING] to original directory: %CPYCREATEHOME%
cd /d "%CPYCREATEHOME%"
echo [ENDING] %~nx0
