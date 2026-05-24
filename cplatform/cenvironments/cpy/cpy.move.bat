::------------------------------------------------------------------------------------------
:: name: cpy.move.bat
:: desc: moves Python source files and pyproject.toml to the Python metadata project
::------------------------------------------------------------------------------------------
@echo off

:: This script is called by cpy.create.bat to move the Python environment files from the core source directory to the Python project directory in metadata. It also installs the Python package in editable mode after copying.
echo [MOVING] CPythonEnvironment - moving Python environment from core source to Python project...

:: Saving the current directory to return to it later
set "CPYMOVEHOME=%CD%"
set "CPY_ENVPATH=%~dp0"
set "C3DCLASSES_COREPATH=%C3DCLASSES%"
set "C3DCLASSES_PYPATH=%CMETADATA%\c3dclasses_py"
echo [SETTING] Python core source path from C3DCLASSES: %C3DCLASSES_COREPATH%
echo [SETTING] Python project path: %C3DCLASSES_PYPATH%
echo [SETTING] Python environment path: %CPY_ENVPATH%
echo [SETTING] Current directory: %CD%
echo [SETTING] Script directory: %~dp0

:: setting the src and dst directories to write from and to
echo [STARTING] Moving Python environment from core source to Python project...
set "src=%C3DCLASSES_COREPATH%"
set "dst=%C3DCLASSES_PYPATH%"
if not exist "%dst%" mkdir "%dst%"

:: Copying Python source files from core to Python project
echo [MOVING] Python source files from %src% to %dst%
call scripts.copy.bat "%src%" "%dst%"

:: Copying python project files from c3dclassessdk path to Python project
if exist "%src%" (
	echo [COPYING] Python source files...
	call directory.copy.bat "%src%" "%dst%\c3dclasses" ".py" "test_,_test.py" "unit_test.py,mock_test.py"
	echo [COPYING] Python test files...
	call directory.copy.bat "%src%" "%dst%\tests" ".py" "" "unit_test.py,mock_test.py"
) else (
	echo [ERROR] Source directory does not exist: %src%
	cd /d "%CPYMOVEHOME%"
	echo [ENDING] %~nx0
	exit /b 1
)

:: Copying pyproject.toml from script directory to Python project
echo [COPYING] pyproject.toml from script directory to Python project...
set "PYPROJECT_SRC=%CPY_ENVPATH%\pyproject.toml"
echo [COPYING] pyproject.toml from: %PYPROJECT_SRC%
echo [COPYING] pyproject.toml to: %dst%\pyproject.toml
if exist "%PYPROJECT_SRC%" (
	copy /Y "%PYPROJECT_SRC%" "%dst%\pyproject.toml" >nul
) else (
	echo [ERROR] Missing pyproject.toml: %PYPROJECT_SRC%
	cd /d "%CPYMOVEHOME%"
	echo [ENDING] %~nx0
	exit /b 1
)

:: Copying additional project files if they exist
echo [COPYING] Additional project files if they exist including setup.cfg, README.md, LICENSE...
if exist "%CPY_ENVPATH%\setup.cfg" copy /Y "%CPY_ENVPATH%\setup.cfg" "%dst%" >nul
if exist "%CPY_ENVPATH%\README.md" copy /Y "%CPY_ENVPATH%\README.md" "%dst%" >nul
if exist "%CPY_ENVPATH%\LICENSE" copy /Y "%CPY_ENVPATH%\LICENSE" "%dst%" >nul

:: end of move, now install the package
cd /d "%CPYCREATEHOME%"
echo [ENDING] %~nx0
