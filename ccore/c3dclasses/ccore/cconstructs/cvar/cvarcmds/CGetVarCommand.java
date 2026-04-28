//---------------------------------------------------------------------------------------------------------
// name: CGetVarCommand.java
// desc: Reads one variable from CMemory and emits a temp script that sets the env var.
//---------------------------------------------------------------------------------------------------------
package c3dclasses;

//-----------------------------------------------------------------------------------------------------------
// name: CGetVarCommand
// desc: Command entry point for exporting a persisted variable to shell context.
//-----------------------------------------------------------------------------------------------------------
public class CGetVarCommand {
	//-------------------------------------------------------------------------------------
	// name: main()
	// desc: Args: <memory-file> <env-var-name>
	//-------------------------------------------------------------------------------------
	public static void main(String[] args) {
		// Read command line arguments.
		CArray cargs = __.carray(args);
		if(cargs == null || cargs.length() < 2) {
			__.println("Please supply 1 arguments.");
			__.println("getvar <ENVVARNAME>");
			__.println("getvar JAVA_HOME");
			__.println();
			return;
		} // end if
		__.print("params: ");
		__.println(cargs);
		String strmetapath =  __.dirname(cargs._string(0));
		String strvarname = cargs._string(1);
		String strvarspath = cargs._string(0);
		
		// Include and open memory store.
		if(CMemory.include("cvars", strvarspath, "c3dclasses.CJSONMemoryDriver", null) == null)
			return;
		CMemory cmemory = CMemory.use("cvars");
		
		// Retrieve variable from memory store.
		CReturn creturn = cmemory.retrieve(strvarname);	
		if(creturn == null || (CHash) creturn.data() == null) {
			__.println("cmemory.retrieve(" + strvarname + ") - Could not retrieve memory location.");
			__.println("Try using setvar command to create a variable before using getvar command.");
			return;
		} // end if		
		cmemory.close();
		__.println("cmemory.close() - closing memory location.");
				
		CHash cvar = (CHash) creturn.data();
		String strvarvalue = (cvar != null) ? cvar._string("m_value") : "";
			
		// Create temp script that sets the environment variable in the caller shell.
		String strscriptfile = strmetapath + "/" + strvarname + "_tmp.bat";
		String strcontents = "echo setting the environment variable:\n";
		strcontents += "set " + strvarname + "=" + strvarvalue + "\n";
		__.println("Creating the script for setting the environment variable in: " + strscriptfile);
		__.file_set_contents(strscriptfile, strcontents);
		return;
	} // end main()
} // end CGetVarCommand