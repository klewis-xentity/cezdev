//---------------------------------------------------------------------------------------------------------
// name: CListVarsCommand.java
// desc: Lists all variables currently stored in the configured CMemory file.
//---------------------------------------------------------------------------------------------------------
package c3dclasses;

//-----------------------------------------------------------------------------------------------------------
// name: CListVarsCommand
// desc: Command entry point for printing memory-backed variables as JSON.
//-----------------------------------------------------------------------------------------------------------
public class CListVarsCommand {
	//-------------------------------------------------------------------------------------
	// name: main()
	// desc: Args: <memory-file>
	//-------------------------------------------------------------------------------------
	public static void main(String[] args) {
		// Read command line arguments.
		CArray cargs = __.carray(args);
		int base = argumentBaseOffset(cargs);
		if(cargs == null || (cargs.length() - base) < 1) {
			__.println("[USAGE] supply 1 argument");
			__.println("lsvars <memory-file>");
			__.println("lsvars C:/path/to/cvars.json");
			__.println();
			return;
		} // end if
		String strvarspath = cargs._string(base);
		
		// Include and open memory store.
		if(CMemory.include("cvars", strvarspath, "c3dclasses.CJSONMemoryDriver", null) == null)
			return;
		CMemory cmemory = CMemory.use("cvars");
		
		// Print memory contents.
		__.println(cmemory.cache().toJSON(false));
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
} // end CListVarsCommand