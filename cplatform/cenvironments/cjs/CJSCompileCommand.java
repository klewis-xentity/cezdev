//------------------------------------------------------------------------------
// name: CJSCompileCommand.java
// desc: 
//------------------------------------------------------------------------------
package c3dclasses;

//--------------------------------------------------------
// name: CJSCompileCommand
// desc:
//--------------------------------------------------------
public class CJSCompileCommand {
	//-------------------------------------------------------
	// name: main()
	// desc: 
	//-------------------------------------------------------
	public static void main(String[] args) {
		CArray cargs = __.carray(args);
		if(cargs == null || cargs.length() < 5) {
			__.println("Please supply 5 or more arguments. Example > cmp <entry-js-file> <entry-class-name> <entry-html-file> <entry-div-sel>");
			return;
		} // end if
		String strcomponentfile = cargs._string(1);
		String strcomponentclassname = cargs._string(2);
		String strpropfile = cargs._string(3);
		String strjsinfile = cargs._string(4);
		String strjsoutfile = cargs._string(5);
		
		CHash keysvalues = __.chash();
		keysvalues._("[component-file]", __.file_path(strcomponentfile));
		keysvalues._("[component-class]", strcomponentclassname);	
		if(!__.file_replace_key_with_contents(strpropfile, strjsinfile, strjsoutfile, keysvalues))
			__.alert("error replacing content in the file");
		return;
	} // end main()
} // end CJavaD