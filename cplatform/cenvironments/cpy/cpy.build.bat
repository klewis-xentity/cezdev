::------------------------------------------------------------------------------------------
:: name: cpy.build.bat
:: desc: installs Python requirements and the editable C3DClasses SDK
::------------------------------------------------------------------------------------------
@echo off
setlocal
echo [CALLING] %~nx0
echo [SETTING] CPYBUILDHOME to current directory: %CD% and C3DCLASSES_PYPATH to destination path in metadata: %C3DCLASSES_PYPATH%
set "CPYBUILDHOME=%CD%"
set "dst=%C3DCLASSES_PYPATH%"

echo [INSTALLING] Python package (editable)...
if not defined dst (
	echo [ERROR] C3DCLASSES_PYPATH is not set.
	endlocal & exit /b 1
)
cd /d "%dst%"
if errorlevel 1 goto cpy_build_failed
call python.bat -m pip install --upgrade setuptools wheel
if errorlevel 1 (
	echo [ERROR] Failed to install Python build dependencies.
	goto cpy_build_failed
)
if exist "%dst%\requirements.txt" (
	echo [INSTALLING] Python requirements from %dst%\requirements.txt...
	call python.bat -m pip install -r "%dst%\requirements.txt"
	if errorlevel 1 (
		echo [ERROR] Failed to install Python requirements from %dst%\requirements.txt.
		goto cpy_build_failed
	)
)
call python.bat -m pip install --no-build-isolation -e .
if errorlevel 1 (
	echo [ERROR] Failed to install Python package from %dst%.
	goto cpy_build_failed
)
echo [RETURNING] to original directory: %CPYBUILDHOME%
cd /d "%CPYBUILDHOME%"
echo [ENDING] %~nx0
endlocal & exit /b 0

:cpy_build_failed
echo [RETURNING] to original directory: %CPYBUILDHOME%
cd /d "%CPYBUILDHOME%"
echo [ERROR] %~nx0 failed.
endlocal & exit /b 1
