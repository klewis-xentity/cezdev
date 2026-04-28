::------------------------------------------------------------------------------------------
:: name: delvar.bat
:: desc: Removes a variable from CMemory.
:: usage: delvar <envvar>
::------------------------------------------------------------------------------------------
call cjava c3dclasses.CDeleteVarCommand "%CMETADATA_CVARS%" %*