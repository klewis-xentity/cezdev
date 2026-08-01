@echo off

:: Detect binaries drive (prefer D:, fallback to E:)
if exist "D:\cbinaries" (
    set "CBINARIES_DRIVE=D:"
) else if exist "E:\cbinaries" (
    set "CBINARIES_DRIVE=E:"
) else (
    echo ERROR: Neither D: nor E: drive is available.
    exit /b 1
)

:: Root directory for all binaries
set "CBINARIES_HOME=%CBINARIES_DRIVE%\cbinaries"

set "code.home=%CBINARIES_HOME%\code\VSCode-win32-x64-1.119.1"
set "code.bin=%CBINARIES_HOME%\code\VSCode-win32-x64-1.119.1\bin"

set "7zip.home=%CBINARIES_HOME%\7zip"
set "7zip.bin=%CBINARIES_HOME%\7zip"

set "apache-maven.home=%CBINARIES_HOME%\apache-maven"
set "apache-maven.bin=%CBINARIES_HOME%\apache-maven\apache-maven-3.9.10\bin"

set "comfyui.home=%CBINARIES_HOME%\comfyui"
set "comfyui.bin=%CBINARIES_HOME%\comfyui"

set "docker.home=%CBINARIES_HOME%\docker"
set "docker.bin=%CBINARIES_HOME%\docker"

set "git.home=%CBINARIES_HOME%\git"
set "git.bin=%CBINARIES_HOME%\git"

set "java.home=%CBINARIES_HOME%\java\jdk-24.0.1"
set "java.bin=%CBINARIES_HOME%\java\jdk-24.0.1\bin"

set "make.home=%CBINARIES_HOME%\make"
set "make.bin=%CBINARIES_HOME%\make\make-3.81-bin\bin"

set "ngrok.home=%CBINARIES_HOME%\ngrok"
set "ngrok.bin=%CBINARIES_HOME%\ngrok"

set "node.home=%CBINARIES_HOME%\node\node-v24.18.0-win-x64"
set "node.bin=%CBINARIES_HOME%\node\node-v24.18.0-win-x64"

set "notepad++.home=%CBINARIES_HOME%\notepad++"
set "notepad++.bin=%CBINARIES_HOME%\notepad++\npp.7.6.2.bin.x64"

set "other.home=%CBINARIES_HOME%\other"
set "other.bin=%CBINARIES_HOME%\other"

set "python.home=%CBINARIES_HOME%\python\python-3.13.13-embed-amd64"
set "python.bin=%CBINARIES_HOME%\python\python-3.13.13-embed-amd64"

set "ssh.home=%CBINARIES_HOME%\ssh"
set "ssh.bin=%CBINARIES_HOME%\ssh"