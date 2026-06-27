::-------------------------------------------------------------------------------------------------------
:: name: ollama_serve.bat
:: desc: starts the remote Ollama server, opens the SSH tunnel, and can test llama3.1 locally
:: usage: ollama_serve
::-------------------------------------------------------------------------------------------------------
@echo off
setlocal

echo Starting Ollama on bc_cs_cluster_node4...
start "Ollama server - bc_cs_cluster_node4" wsl.exe bash -lc "ssh bc_cs_cluster_node4 '~/bin/ollama serve'; exec bash"

echo Opening SSH tunnel through bc_cs_cluster_node4_ollama...
start "Ollama tunnel - localhost:11434" wsl.exe bash -lc "ssh bc_cs_cluster_node4_ollama; exec bash"

echo.
echo Give the server and tunnel a few seconds to finish connecting.
choice /M "Run a quick llama3.1 test request now"
if errorlevel 2 goto :done

wsl.exe bash -lc "curl -X POST http://localhost:11434/v1/chat/completions -H 'Content-Type: application/json' -d '{\"model\":\"llama3.1\",\"messages\":[{\"role\":\"user\",\"content\":\"Hi\"}]}'"

echo.

:done
endlocal
