//---------------------------------------------------------------------------------------------------------
// name: CSetVarCommand.java
// desc: Writes/updates a variable in CMemory and emits a temp script to set env vars.
//---------------------------------------------------------------------------------------------------------
package c3dclasses;

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
		if(cargs == null || cargs.length() < 3) {
			__.println("Please supply 2-4 arguments.");
			__.println("setvar <ENVVARNAME> <value>");
			__.println("setvar <ENVVARNAME> <prompt message> <-file|-dir|-path>");
			__.println("setvar JAVA_HOME 'my value'");
			__.println("setvar JAVA_HOME 'please enter JAVA_HOME directory' -path");
			__.println("setvar JAVA_HOME 'please enter JAVA_HOME directory' -file");
			__.println("setvar JAVA_HOME 'please enter JAVA_HOME directory' -dir");
			__.println();
			return;
		} // end if
		__.print("params: ");
		__.println(cargs);
		String strmetapath =  __.dirname(cargs._string(0));
		String strvarname = cargs._string(1);
		String strpromptmsg =  cargs._string(2);
		String strvarspath = cargs._string(0);
		boolean bsetpath = (cargs._string(3) != null) ? cargs._string(3).equals("-path") : false;
		boolean bsetfile = (cargs._string(3) != null) ? cargs._string(3).equals("-file") : false;
		boolean bsetdir = (cargs._string(3) != null) ? cargs._string(3).equals("-dir") : false;
		String strvarvalue = "";
		
		// Include and open memory store.
		if(CMemory.include("cvars", strvarspath, "c3dclasses.CJSONMemoryDriver", null) == null)
			return;
		CMemory cmemory = CMemory.use("cvars");
		
		// Retrieve existing variable or create one when missing.
		CReturn creturn = cmemory.retrieve(strvarname);	
		if(creturn == null || (CHash) creturn.data() == null) {
			__.println("cmemory.retrieve(" + strvarname + ") - Could not retrieve memory location.");
			__.println("cmemory.retrieve(" + strvarname + ") - Creating a new memory location now.....");
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
					__.println("The following path is a directory in the system: " + strpath);
					strvarvalue = strpath;
					break;
				} // end if
				else __.println("The following path is not a directory in the system: " + strpath);
			} // end for
			
			if(strvarvalue.equals("")) {
				// Otherwise prompt user for a path and persist it.
				__.println("Opening the file dialog to get a path to set....");	
				String strpath = "";
				strpath = __.prompt_path(strpromptmsg);
				if(!__.is_directory(strpath)) {
					__.println("The following path is not a directory in the system: " + strpath);	
					cmemory.close();
					return;
				} // end if
				
				// Append selected path and persist updated list.
				paths.push(strpath);
				strpaths = paths.join(";");			
				cmemory.update(strvarname, strpaths, "string", null);
				cmemory.close();
				__.println("cmemory.update(" + strvarname + ") - updating memory location with new contents: " + strpath);
				__.println("cmemory.close() - closing memory location.");
				__.println("The following path is a directory in the system: " + strpath);
				strvarvalue = strpath;			
			} // end if
		} // end if
		else {
			strvarvalue =  cargs._string(2);
			cmemory.update(strvarname, strvarvalue, "string", null);
			cmemory.close();
			__.println("cmemory.update(" + strvarname + ") - updating memory location with new contents: " + strvarvalue);
			__.println("cmemory.close() - closing memory location.");	
		} // end else
		
		// Create temp script that sets environment variables in the caller shell.
		String strscriptfile = strmetapath + "/" + strvarname + "_tmp.bat";
		String strcontents = "echo setting the environment variable:\n";
		strcontents += "set " + strvarname + "=" + strvarvalue + "\n";
		if(bsetpath) {
			strcontents += "echo setting the PATH:\n";
			strcontents += "\nset PATH=%PATH%;%"+strvarname+"%;\n";
		} // end if
		__.println("Creating the script for the environment variable in: " + strscriptfile);
		__.file_set_contents(strscriptfile, strcontents);
		return;
	} // end main()
} // end CSetVarCommand