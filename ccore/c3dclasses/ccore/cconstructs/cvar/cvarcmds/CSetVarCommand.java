//---------------------------------------------------------------------------------------------------------
// name: CSetVarCommand.java
// desc: Writes/updates a variable in CMemory and emits a temp script to set env vars.
//---------------------------------------------------------------------------------------------------------
package c3dclasses;

import java.io.File;

//-----------------------------------------------------------------------------------------------------------
// name: CSetVarCommand
// desc: Command entry point for persisting and exporting environment variable values.
//-----------------------------------------------------------------------------------------------------------
public class CSetVarCommand {
	//-------------------------------------------------------------------------------------
	// name: main()
	// desc: Args: <memory-file> <env-var-name> <value|prompt> [mode-flag]
	//-------------------------------------------------------------------------------------
	public static void main(String[] args) {
		// Read command line arguments.
		CArray cargs = __.carray(args);
		int base = argumentBaseOffset(cargs);
		if(cargs == null || (cargs.length() - base) < 3) {
			__.println("[USAGE] supply 2-4 arguments");
			__.println("setvar <ENVVARNAME> <value>");
			__.println("setvar <ENVVARNAME> <prompt message> <-file|-dir|-path>");
			__.println("setvar JAVA_HOME 'my value'");
			__.println("setvar JAVA_HOME 'please enter JAVA_HOME directory' -path");
			__.println("setvar JAVA_HOME 'please enter JAVA_HOME directory' -file");
			__.println("setvar JAVA_HOME 'please enter JAVA_HOME directory' -dir");
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
		String strpromptmsg =  cargs._string(base + 2);
		boolean bsetpath = (cargs._string(base + 3) != null) ? cargs._string(base + 3).equals("-path") : false;
		boolean bsetfile = (cargs._string(base + 3) != null) ? cargs._string(base + 3).equals("-file") : false;
		boolean bsetdir = (cargs._string(base + 3) != null) ? cargs._string(base + 3).equals("-dir") : false;
		String strvarvalue = "";
		
		// Include and open memory store.
		if(CMemory.include("cvars", strvarspath, "c3dclasses.CJSONMemoryDriver", null) == null)
			return;
		CMemory cmemory = CMemory.use("cvars");
		
		// Retrieve existing variable or create one when missing.
		CReturn creturn = cmemory.retrieve(strvarname);	
		if(creturn == null || (CHash) creturn.data() == null) {
		__.println("[ERROR] cmemory could not retrieve memory location");
		__.println("[CREATING] cmemory new memory location");
			creturn = cmemory.create(strvarname, "", "string", null);
		} // end if		
		
		if(bsetfile || bsetdir || bsetpath) {
			CHash cvar = (CHash) creturn.data();
			String strpaths = (cvar != null) ? cvar._string("m_value") : "";
		
			// Reuse first valid cached path if present.
			CArray paths = __.split(";", strpaths);
			if(paths == null) 
				paths = __.carray();
			for(int i=0; i<paths.length(); i++) {
				String strpath = paths._string(i);
				if(strpath == "" || strpath == null)
					continue;
				if(__.is_directory(strpath)) {
					__.println("[VALID] path is a directory: " + strpath);
					strvarvalue = strpath;
					break;
				} // end if
				else __.println("[INVALID] path is not a directory: " + strpath);
			} // end for
			
			if(strvarvalue.equals("")) {
				// Otherwise prompt user for a path and persist it.
				__.println("[OPENING] file dialog to get path");	
				String strpath = "";
				strpath = __.prompt_path(strpromptmsg);
				if(!__.is_directory(strpath)) {
					__.println("[INVALID] path is not a directory: " + strpath);	
					cmemory.close();
					return;
				} // end if
				
				// Append selected path and persist updated list.
				paths.push(strpath);
				strpaths = paths.join(";");			
				cmemory.update(strvarname, strpaths, "string", null);
				cmemory.close();
				__.println("[UPDATING] cmemory with new path: " + strpath);
				__.println("[CLOSED] cmemory");
				__.println("[VALID] path is a directory: " + strpath);
				strvarvalue = strpath;			
			} // end if
		} // end if
		else {
			strvarvalue =  cargs._string(base + 2);
			cmemory.update(strvarname, strvarvalue, "string", null);
			cmemory.close();
			__.println("[UPDATING] cmemory with new value: " + strvarvalue);
			__.println("[CLOSED] cmemory");	
		} // end else
		
		// Create temp script that sets environment variables in the caller shell.
		String strscriptfile = new File(strmetapath, strvarname + "_tmp.bat").getAbsolutePath();
		String strvaluefile = new File(strmetapath, strvarname + ".txt").getAbsolutePath();
		String strcontents = "echo [SETTING] the environment variable: " + strvarname + " with value: " + strvarvalue + "\n";
		strcontents += "set " + strvarname + "=" + strvarvalue + "\n";
		strcontents += "set " + strvarname + ".txt=" + strvaluefile + "\n";
		if(bsetpath) {
			strcontents += "echo [SETTING] the PATH with: " + strvarname + "\n";
			strcontents += "\nset PATH=%PATH%;%"+strvarname+"%;\n";
		} // end if
		__.println("[CREATED] script for environment variable in: " + strscriptfile);
		__.println("[CREATED] value file for environment variable in: " + strvaluefile);
		__.file_set_contents(strscriptfile, strcontents);
		__.file_set_contents(strvaluefile, strvarvalue);
		return;
	} // end main()

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
} // end CSetVarCommand