::------------------------------------------------------------------------------------------
:: name: cjs.destroy.bat
:: desc: Removes the JavaScript environment from C3DClasses SDK
::------------------------------------------------------------------------------------------
@echo off
echo [CALLING] %~nx0

if exist "%C3DCLASSES_JS%" (
    echo [REMOVING] %C3DCLASSES_JS%
    rmdir /s /q "%C3DCLASSES_JS%"
    echo [OK] JavaScript environment removed.
) else (
    echo [INFO] JavaScript environment directory does not exist.
)
 
echo [ENDING] %~nx0
