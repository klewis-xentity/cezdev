@echo off
setlocal

:: Starts "ollama serve" on the BC cluster node4 and forwards the Ollama
:: API port (11434) to this machine so http://localhost:11434 reaches it.
::
::   Usage: bc_ollama.serve.bat
::
:: Leave this window open while you use Ollama locally. Press Ctrl+C to stop.

:: Alias defined in the ssh config file (includes LocalForward 11434)
set "BC_HOST=bc_cs_cluster_node4_ollama"

:: Detect binaries drive (prefer D:, fallback to E:) and locate the ssh config
if exist "D:\cbinaries\ssh\config" (
    set "SSH_CONFIG=D:\cbinaries\ssh\config"
) else if exist "E:\cbinaries\ssh\config" (
    set "SSH_CONFIG=E:\cbinaries\ssh\config"
) else (
    echo [ERROR] SSH config not found at D:\cbinaries\ssh\config or E:\cbinaries\ssh\config
    exit /b 2
)

echo [OLLAMA] Opening interactive session on %BC_HOST% (forwarding localhost:11434)
echo [OLLAMA] Keep this window open. Type 'exit' or press Ctrl+D to close.
ssh -t -F "%SSH_CONFIG%" %BC_HOST%

set "RC=%ERRORLEVEL%"
endlocal & exit /b %RC%
