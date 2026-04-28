::------------------------------------------------------------------------------------------
:: name: getvar.bat
:: desc: Gets a variable from CMemory and sets it in the current shell.
:: usage: getvar <envvar>
::------------------------------------------------------------------------------------------
call cjava c3dclasses.CGetVarCommand "%CMETADATA_CVARS%" %*

:: Run the temp script to apply env var changes, then remove it.
echo Calling the temporary bat file: %1_tmp.bat
call "%CMETADATA%\%1_tmp.bat"
echo Removing the temporary bat file: %1_tmp.bat
del "%CMETADATA%\%1_tmp.bat"
