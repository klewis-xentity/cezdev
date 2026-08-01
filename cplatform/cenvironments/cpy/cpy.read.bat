::-------------------------------------------------------------------------------------------------
:: name: cpy.exist.bat
:: desc: Checks whether the c3dclasses package is installed.
:: usage: cpy.exist.bat
:: returns:
::    exit code 0 = package exists
::    exit code 1 = package does not exist
::-------------------------------------------------------------------------------------------------

echo [CALLING] %~nx0

call python -m pip show c3dclasses
call python --version
call python -c "import sys; print(sys.executable)"
call python -m pip show ollama
call python -c "import ollama; print(ollama)"

if %ERRORLEVEL% EQU 0 (
    echo [RETURNING] true
    echo [ENDING] %~nx0
    exit /b 0
) else (
    echo [RETURNING] false
    echo [ENDING] %~nx0
    exit /b 1
)