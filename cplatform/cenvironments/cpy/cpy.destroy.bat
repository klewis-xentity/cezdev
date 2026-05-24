::------------------------------------------------------------------------------------------
:: name: cpy.destroy.bat
:: desc: Removes the Python environment from C3DClasses SDK
::------------------------------------------------------------------------------------------

@echo off
setlocal

echo [CALLING] %~nx0

::------------------------------------------------------
:: Validate required variables
::------------------------------------------------------
if "%CMETADATA%"=="" (
    echo [ERROR] CMETADATA environment variable is not set.
    endlocal
    exit /b 1
)

::------------------------------------------------------
:: Remove Python environment directory
::------------------------------------------------------
set "C3DCLASSES_PY=%CMETADATA%\c3dclasses_py"

if exist "%C3DCLASSES_PY%" (
    echo [REMOVING] %C3DCLASSES_PY%
    rmdir /s /q "%C3DCLASSES_PY%"
    if exist "%C3DCLASSES_PY%" (
        echo [ERROR] Failed to remove Python environment: %C3DCLASSES_PY%
        endlocal
        exit /b 1
    )
    echo [OK] Python environment removed.
) else (
    echo [INFO] Python environment directory does not exist.
)

echo [ENDING] %~nx0
endlocal
exit /b 0
