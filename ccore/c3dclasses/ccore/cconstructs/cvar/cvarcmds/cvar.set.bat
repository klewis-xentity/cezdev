::------------------------------------------------------------------------------------------
:: name: setvar.bat
:: desc: Sets/updates a variable in CMemory and applies it in the current shell.
:: usage: setvar <envvar> <promptmsg|value> <-path|-file|-dir>
::------------------------------------------------------------------------------------------
call cjava c3dclasses.CSetVarCommand "%CMETADATA_CVARS%" %*

:: Run the temp script to apply env var changes, then remove it.
set "TMP_VAR_NAME=%~1"
echo [CALLING] temporary bat file: %TMP_VAR_NAME%_tmp.bat
call "%CMETADATA%\%TMP_VAR_NAME%_tmp.bat"
echo [REMOVING] temporary bat file: %TMP_VAR_NAME%_tmp.bat
del "%CMETADATA%\%TMP_VAR_NAME%_tmp.bat"
