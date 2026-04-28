::------------------------------------------------------------------------------------------
:: name: setvar.bat
:: desc: Sets/updates a variable in CMemory and applies it in the current shell.
:: usage: setvar <envvar> <promptmsg|value> <-path|-file|-dir>
::------------------------------------------------------------------------------------------
call cjava c3dclasses.CSetVarCommand "%CMETADATA_CVARS%" %*

:: Run the temp script to apply env var changes, then remove it.
echo Calling the temporary bat file: %1_tmp.bat
call "%CMETADATA%\%1_tmp.bat"
echo Removing the temporary bat file: %1_tmp.bat
del "%CMETADATA%\%1_tmp.bat"
