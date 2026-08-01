::------------------------------------------------------------------------------------------
:: name: cpy.create.bat
:: desc: initializes Python environment wiring for C3DClasses SDK
::------------------------------------------------------------------------------------------
@echo off
echo [CALLING] %~nx0
echo [SETTING] CPYBUILDHOME to current directory: %CD% and C3DCLASSES_PYPATH to destination path in metadata: %C3DCLASSES_PYPATH%
set "CPYBUILDHOME=%CD%"
set "dst=%C3DCLASSES_PYPATH%"

echo [INSTALLING] Python package (editable)...
cd /d "%dst%"
call python.bat -m pip install --upgrade setuptools wheel
if exist "%dst%\requirements.txt" (
	echo [INSTALLING] Python requirements from %dst%\requirements.txt...
	call python.bat -m pip install -r "%dst%\requirements.txt"
	if errorlevel 1 (
		echo [ERROR] Failed to install Python requirements from %dst%\requirements.txt.
	)
)
call python.bat -m pip install --no-build-isolation -e .
if errorlevel 1 (
	echo [ERROR] Failed to install Python package from %dst%.
)
:: Note: We do not exit with error here because the move operation was successful, and the package may already be installed. The user can address installation issues separately if needed.
:: Returning to original directory after installation attempt
echo [RETURNING] to original directory: %CPYBUILDHOME%
cd /d "%CPYBUILDHOME%"
echo [ENDING] %~nx0
endlocal
