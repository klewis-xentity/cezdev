::------------------------------------------------------------------------------------------
:: name: getvar.bat
:: desc: Gets a variable from CMemory and sets it in the current shell.
:: usage: getvar <envvar>
::------------------------------------------------------------------------------------------
call cjava c3dclasses.CGetVarCommand "%CMETADATA_CVARS%" %*

:: Run the temp script to apply env var changes, then remove it.
set "TMP_VAR_NAME=%~1"
echo [CALLING] temporary bat file: %TMP_VAR_NAME%_tmp.bat
call "%CMETADATA%\%TMP_VAR_NAME%_tmp.bat"
echo [REMOVING] temporary bat file: %TMP_VAR_NAME%_tmp.bat
del "%CMETADATA%\%TMP_VAR_NAME%_tmp.bat"
