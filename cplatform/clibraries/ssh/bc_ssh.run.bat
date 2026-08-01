@echo off
setlocal

if "%~1"=="" (
    echo [ERROR] Usage: bc_ssh.run.bat "^<command^>"
    echo         Example: bc_ssh.run.bat "ollama serve"
    exit /b 1
)

:: Command to run remotely (outer quotes stripped)
set "REMOTE_CMD=%~1"

:: Alias defined in the ssh config file
set "BC_HOST=bc_cs_cluster_node4"

:: Detect binaries drive (prefer D:, fallback to E:) and locate the ssh config
if exist "D:\cbinaries\ssh\config" (
    set "SSH_CONFIG=D:\cbinaries\ssh\config"
) else if exist "E:\cbinaries\ssh\config" (
    set "SSH_CONFIG=E:\cbinaries\ssh\config"
) else (
    echo [ERROR] SSH config not found at D:\cbinaries\ssh\config or E:\cbinaries\ssh\config
    exit /b 2
)

echo [SSH] %BC_HOST% : %REMOTE_CMD%
ssh -F "%SSH_CONFIG%" %BC_HOST% "bash -lc '%REMOTE_CMD%'"

set "RC=%ERRORLEVEL%"
endlocal & exit /b %RC%
