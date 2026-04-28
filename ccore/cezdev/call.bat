@echo off
::------------------------------------------------------------------------------------------
:: name: call.bat
:: desc: Wrapper for scripts.call.bat
:: usage: call.bat <srcpath> <scriptpattern>
::        call.bat <scope>.<scriptpattern>
::------------------------------------------------------------------------------------------

call "%~dp0scripts.call.bat" %*
