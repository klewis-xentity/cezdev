::---------------------------------------------------------------------------------------------------
:: name: cjsinit.bat
:: desc: Initializes the JavaScript environment
::       (node, webpack, js, html, css, sass, angular, react, etc.)
:: note: This script is deprecated. Use cjs.create.bat instead.
::---------------------------------------------------------------------------------------------------
@echo off

echo [INFO] cjsinit.bat is deprecated. Calling cjs.create.bat instead...
call "%~dp0cjs.create.bat"