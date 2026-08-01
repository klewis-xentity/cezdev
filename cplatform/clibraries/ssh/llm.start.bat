@echo off
setlocal

:: Starts the remote LLM stack:
::   1) Launches "ollama serve" on the BC cluster node in a new window.
::   2) Opens the port-forwarding session so http://localhost:11434 reaches it.
::
::   Usage: llm.start.bat

set "SCRIPT_DIR=%~dp0"

start "bc_ssh.run" cmd /c "%SCRIPT_DIR%bc_ssh.run.bat" "ollama serve"
start "bc_ollama.serve" cmd /c "%SCRIPT_DIR%bc_ollama.serve.bat"

endlocal & exit /b 0
