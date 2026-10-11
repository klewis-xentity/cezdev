//---------------------------------------------------------------------------------------------------------
// name: CGetVarCommand.java
// desc: Reads one variable from CMemory and emits a temp script that sets the env var.
//---------------------------------------------------------------------------------------------------------
package c3dclasses;

import java.io.File;

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
		int base = argumentBaseOffset(cargs);
		if(cargs == null || (cargs.length() - base) < 2) {
			__.println("[USAGE] supply 1 argument");
			__.println("getvar <ENVVARNAME>");
			__.println("getvar JAVA_HOME");
			__.println();
			return;
		} // end if
		__.print("[PARAMS] ");
		__.println(cargs);
		String strvarspath = cargs._string(base);
		String strmetapath = new File(strvarspath).getParent();
		if(strmetapath == null || strmetapath.equals(""))
			strmetapath = __.dirname(strvarspath);
		String strvarname = cargs._string(base + 1);
		
		// Include and open memory store.
		if(CMemory.include("cvars", strvarspath, "c3dclasses.CJSONMemoryDriver", null) == null)
			return;
		CMemory cmemory = CMemory.use("cvars");
		
		// Retrieve variable from memory store.
		CReturn creturn = cmemory.retrieve(strvarname);	
		if(creturn == null || (CHash) creturn.data() == null) {
			__.println("[ERROR] could not retrieve memory location");
			__.println("[INFO] use setvar command to create variable first");
			return;
		} // end if		
		cmemory.close();
		__.println("[CLOSED] memory location");
				
		CHash cvar = (CHash) creturn.data();
		String strvarvalue = (cvar != null) ? resolveVarValue(cvar._("m_value")) : "";
			
		// Create temp script that sets the environment variable in the caller shell.
		String strscriptfile = new File(strmetapath, strvarname + "_tmp.bat").getAbsolutePath();
		String strcontents = "echo [SETTING] the environment variable: " + strvarname + " with value: " + strvarvalue + "\n";
		strcontents += "set " + strvarname + "=" + strvarvalue + "\n";
		__.println("[CREATED] script for setting environment variable in: " + strscriptfile);
		__.println("[ENVNAME] " + strvarname);
		__.println("[ENVVAL] " + strvarvalue);
		__.file_set_contents(strscriptfile, strcontents);
		return;
	} // end main()

	private static String resolveVarValue(Object value) {
		if(value == null)
			return "";
		if(value instanceof CHash) {
			Object innerValue = ((CHash) value)._("m_value");
			if(innerValue != null && innerValue != value)
				return resolveVarValue(innerValue);
		}
		return value.toString();
	}

	private static int argumentBaseOffset(CArray cargs) {
		if(cargs == null || cargs.length() < 2)
			return 0;
		String first = cargs._string(0);
		String second = cargs._string(1);
		if(first == null || second == null)
			return 0;
		boolean looksLikeClass = first.contains(".") || first.endsWith("Command");
		boolean looksLikePath = second.endsWith(".json") || second.contains(":\\") || second.startsWith("/") || second.startsWith("\\");
		return (looksLikeClass && looksLikePath) ? 1 : 0;
	}
} // end CGetVarCommand