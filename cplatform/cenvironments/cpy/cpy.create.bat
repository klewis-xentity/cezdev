::------------------------------------------------------------------------------------------
:: name: cpy.create.bat
:: desc: initializes Python environment wiring for C3DClasses SDK
::------------------------------------------------------------------------------------------
@echo off
setlocal
echo [CALLING] %~nx0
set "CPYCREATEHOME=%CD%"

set "CPY_ENV_NAME=cpy"
set "CPY_ENV_PATH=%CENVIRONMENTS%\%CPY_ENV_NAME%"
set "CPY_ENV_META=%CMETADATA%\cenvironments\%CPY_ENV_NAME%"
set "C3DCLASSESSDK_PY=%CMETADATA%\c3dclassessdk_py"
set "SCRIPTS_COPY=%CEZDEV_HOME%\ccore\cezdev\scripts.copy.bat"
if exist "%SCRIPTS_COPY%" (
	rem keep resolved absolute path
) else (
	set "SCRIPTS_COPY=scripts.copy.bat"
)

mkdir "%CMETADATA%" 2>nul
mkdir "%CMETADATA%\cenvironments" 2>nul

if exist "%CPY_ENV_PATH%" (
	if exist "%CPY_ENV_META%" (
		rem environment metadata scripts already present
	) else (
		call "%SCRIPTS_COPY%" "%CPY_ENV_PATH%" "%CPY_ENV_META%"
	)
) else (
	echo [WARN] Python environment path not found: %CPY_ENV_PATH%
)

mkdir "%C3DCLASSESSDK_PY%" 2>nul

where python >nul 2>nul
if errorlevel 1 (
	echo [ERROR] python is not available on PATH.
	cd /d "%CPYCREATEHOME%"
	echo [ENDING] %~nx0
	endlocal
	exit /b 1
)

if "%C3DCLASSESSDK_PATH%"=="" (
	if "%C3DCLASSES%"=="" (
		set "C3DCLASSESSDK_PATH=%CEZDEV_HOME%\ccore\c3dclasses"
	) else (
		set "C3DCLASSESSDK_PATH=%C3DCLASSES%"
	)
)

set "src=%C3DCLASSESSDK_PATH%"
set "dst=%C3DCLASSESSDK_PY%"
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

set "DIRECTORY_COPY=%CEZDEV_HOME%\ccore\cezdev\directory.copy.bat"
if exist "%DIRECTORY_COPY%" (
	rem use absolute helper path when available
) else (
	set "DIRECTORY_COPY=directory.copy.bat"
)

if exist "%src%" (
	echo [COPYING] Python source files...
	call "%DIRECTORY_COPY%" "%src%" "%dst%\c3dclasses" ".py" "test_,_test.py" "unit_test.py,mock_test.py"
	echo [COPYING] Python test files...
	call "%DIRECTORY_COPY%" "%src%" "%dst%\tests" ".py" "" "unit_test.py,mock_test.py"
) else (
	echo [ERROR] Source directory does not exist: %src%
	cd /d "%CPYCREATEHOME%"
	echo [ENDING] %~nx0
	endlocal
	exit /b 1
)

set "PYPROJECT_SRC=%SCRIPT_DIR%\pyproject.toml"
echo [COPYING] pyproject.toml from: %PYPROJECT_SRC%
echo [COPYING] pyproject.toml to: %dst%\pyproject.toml
if exist "%PYPROJECT_SRC%" (
	copy /Y "%PYPROJECT_SRC%" "%dst%\pyproject.toml" >nul
) else (
	echo [ERROR] Missing pyproject.toml: %PYPROJECT_SRC%
	cd /d "%CPYCREATEHOME%"
	echo [ENDING] %~nx0
	endlocal
	exit /b 1
)

if exist "%SCRIPT_DIR%\setup.cfg" copy /Y "%SCRIPT_DIR%\setup.cfg" "%dst%" >nul
if exist "%SCRIPT_DIR%\README.md" copy /Y "%SCRIPT_DIR%\README.md" "%dst%" >nul
if exist "%SCRIPT_DIR%\LICENSE" copy /Y "%SCRIPT_DIR%\LICENSE" "%dst%" >nul

echo [INSTALLING] Python package (editable)...
cd /d "%dst%"
call python -m pip install -e .
if errorlevel 1 (
	echo [ERROR] Failed to install Python package from %dst%.
	cd /d "%CPYCREATEHOME%"
	echo [ENDING] %~nx0
	endlocal
	exit /b 1
)



cd /d "%CPYCREATEHOME%"
echo [ENDING] %~nx0
endlocal