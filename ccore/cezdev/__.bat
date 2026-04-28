@echo off
::------------------------------------------------------------------------------------------
:: name: __.bat
:: desc: Short alias wrapper for scripts.call.bat
:: usage: __.bat <srcpath> <scriptpattern>
::        __.bat <scope>.<scriptpattern>
::------------------------------------------------------------------------------------------

call "%~dp0scripts.call.bat" %*
