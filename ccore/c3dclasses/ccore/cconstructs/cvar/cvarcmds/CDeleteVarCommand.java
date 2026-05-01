//---------------------------------------------------------------------------------------------------------
// name: CDeleteVarCommand.java
// desc: Deletes a variable from CMemory and emits a temp script that unsets the env var.
//---------------------------------------------------------------------------------------------------------
package c3dclasses;

//-----------------------------------------------------------------------------------------------------------
// name: CDeleteVarCommand
// desc: Command entry point for deleting one variable from persistent metadata.
//-----------------------------------------------------------------------------------------------------------
public class CDeleteVarCommand {
	//-------------------------------------------------------------------------------------
	// name: main()
	// desc: Args: <memory-file> <env-var-name>
	//-------------------------------------------------------------------------------------
	public static void main(String[] args) {
		// Read command line arguments.
		CArray cargs = __.carray(args);
		if(cargs == null || cargs.length() < 2) {
			__.println("[USAGE] supply 1 argument");
			__.println("delvar <ENVVARNAME>");
			__.println("delvar JAVA_HOME");
			__.println();
			return;
		} // end if
		__.print("[PARAMS] ");
		__.println(cargs);
		
		String strmetapath =  __.dirname(cargs._string(0));
		String strvarname = cargs._string(1);
		String strvarspath = cargs._string(0);
		
		// Include and open memory store.
		if(CMemory.include("cvars.js", strvarspath, "c3dclasses.CJSONMemoryDriver", null) == null)
			return;
		CMemory cmemory = CMemory.use("cvars.js");
		
		// Delete variable from memory store.
		CReturn creturn = cmemory.delete(strvarname);	
		if(creturn != null && creturn._boolean())
			__.println("[DELETED] memory location for: " + strvarname);
		else __.println("[ERROR] could not delete memory location: " + strvarname);
		cmemory.close();
		__.println("[CLOSED] memory location");
				
		// Create temp script that unsets the environment variable in the caller shell.
		String strscriptfile = strmetapath + "/" + strvarname + "_tmp.bat";
		String strcontents = "echo 'deleting the environment variable:'\n";
		strcontents += "set " + strvarname + "=\n";
		__.println("[CREATED] script for deleting environment variable in: " + strscriptfile);
		__.file_set_contents(strscriptfile, strcontents);
		return;
	} // end main()
} // end CDeleteVarCommand