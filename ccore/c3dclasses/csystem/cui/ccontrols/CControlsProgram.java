//-------------------------------------------------------
// name: CControlsProgram.java
// desc: 
//-------------------------------------------------------
package c3dclasses;

public class CControlsProgram {				
	public static void main(String[] args) {
		String directoryPath = System.getenv("CPANEL_DIRECTORY_PATH");
		if ((directoryPath == null || directoryPath.trim().isEmpty()) && args != null && args.length > 0) {
			directoryPath = args[0];
		}
		if (directoryPath == null || directoryPath.trim().isEmpty()) {
			directoryPath = "C:\\Users\\oyole\\OneDrive\\Desktop\\cezdev\\cplatform";
		}

		CControls ccontrols = new CControls();
		ccontrols.form("myform", "This is the form title", null);	
		ccontrols.label("directory-path-label", "Directory Path: " + directoryPath, null);
			
			// create the menu items
			ccontrols.menubar("menubar", "This is my menubar", null);
				ccontrols.menu("menu1", "Menu1", null);
					ccontrols.menuitem("item1", "item1", null);
					ccontrols.menuitem("item2", "item2", null);
					ccontrols.menu("menu2", "Menu1", null);
						ccontrols.menuitem("item1", "item1", null);
						ccontrols.menuitem_seperator();
						ccontrols.menuitem("item2", "item2", null);
						ccontrols.menuitem_seperator();
						ccontrols.menuitem("item3", "item3", null);
					ccontrols.endmenu();
					ccontrols.menuitem("item3", "item3", null);
				ccontrols.endmenu();
				ccontrols.menu("menu3", "Menu2", null);
					ccontrols.menuitem("item1", "item1", null);
					ccontrols.menuitem_seperator();
					ccontrols.menuitem("item2", "item2", null);
					ccontrols.menuitem("item3", "item3", null);
				ccontrols.endmenu();
			ccontrols.endmenubar();


			ccontrols.button("control5", "Control5", null);
			
			ccontrols.select("control4", "HELLO3", 
				__.chash( 
					"HELLO5","WORLD5", 
					"HELLO1","WORLD1",
					"HELLO2","WORLD2",
					"HELLO3","WORLD3"
				), // end __.chash() 
			null);

			ccontrols.section("section-control", "section-control", null);
				ccontrols.radio("radio-control", "radio-control1", "Numbers", null);
				ccontrols.radio("radio-control", "radio-control2", "Alphabet", null);
				ccontrols.radio("radio-control", "radio-control3", "Symbols", null);
			ccontrols.endsection();	
			
			ccontrols.submit("submit", "SUBMIT", null);			
			ccontrols.text("text-control", "This is my Text Control", null);	
			ccontrols.textarea("textarea-control", "This is my TextArea Control", null);	
			ccontrols.checkbox("checkbox-control", "This is my Checkbox Control", null);				
			ccontrols.label("label-control", "This is my Label", null);						
		ccontrols.endform();
		
		__.println(ccontrols.toStringContents());
		
		ccontrols.retrieve("myform").setProp("grid","true");
		ccontrols.retrieve("myform").setProp("visible","true");
		ccontrols.retrieve("myform").setProp("pack","true");
		ccontrols.retrieve("myform").setProp("close","true");
		ccontrols.retrieve("myform control5").setProp("onclick", "C:/Users/kevle/Desktop/test.bat");
		ccontrols.retrieve("myform control5").setProp("text", "This was just set");	
		ccontrols.retrieve("myform control5").setProp("onclick", new CFunction() { public CReturn call(CObject obj) {
			__.alert("myform control5");
			__.println("myform control5");
			return null;
		}}); // end onclick
	} // end main()
} // end CMessageBox
