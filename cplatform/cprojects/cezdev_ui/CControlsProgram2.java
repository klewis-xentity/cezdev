//-------------------------------------------------------
// name: CControlsProgram.java
// desc: 
//-------------------------------------------------------
import c3dclasses.*;


public class CControlsProgram2 {				
	public static void main(String[] args) {
		CControls ccontrols = new CControls();
		ccontrols.form("myform", "This is the form title", null);			
			// create menu
			ccontrols.menubar("menubar", "This is my menubar", null);
				ccontrols.menu("menu1", "Menu1", null);
					ccontrols.menuitem_checkbox("item1", "item1", null);
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
	
			// create button
			ccontrols.section("section-control", "section-control", null);
				ccontrols.button("button-control", "button-control", null);
			ccontrols.endsection();	

			// create radio buttons
			ccontrols.section("section-control2", "section-control2", null);
				ccontrols.radio("radio-control", "radio-control1", "Numbers", null);
				ccontrols.radio("radio-control", "radio-control2", "Alphabet", null);
				ccontrols.radio("radio-control", "radio-control3", "Symbols", null);
			ccontrols.endsection();	

			ccontrols.section("section-control3", "section-control3", null);
				ccontrols.checkbox("checkbox-control1", "checkbox-control1", null);	
				ccontrols.checkbox("checkbox-control2", "checkbox-control2", null);	
				ccontrols.checkbox("checkbox-control3", "checkbox-control3", null);	
			ccontrols.endsection();

			ccontrols.section("section-control4", "section-control4", null);
				CHash options = __.chash( 
						"HELLO5","C:/Users/kevle/Desktop/cezdev/cprojects/autograder", 
						"HELLO1","C:/Users/kevle/Desktop/cezdev/cprojects/autograder",
						"HELLO2","C:/Users/kevle/Desktop/cezdev/cprojects/autograder",
						"HELLO3","C:/Users/kevle/Desktop/cezdev/cprojects/autograder"
					); // end __.chash() 
				ccontrols.select("combobox-control", "combobox-control", options, null);
			ccontrols.endsection();

			ccontrols.section("section-control5", "section-control5", null);
				ccontrols.text("text-control", "text-control", null);
			ccontrols.endsection();

			
		ccontrols.endform();

			

			/*
			CHash options = __.chash( 
					"HELLO5","C:/Users/kevle/Desktop/cezdev/cprojects/autograder", 
					"HELLO1","C:/Users/kevle/Desktop/cezdev/cprojects/autograder",
					"HELLO2","C:/Users/kevle/Desktop/cezdev/cprojects/autograder",
					"HELLO3","C:/Users/kevle/Desktop/cezdev/cprojects/autograder"
				); // end __.chash() 
			*/



//			ccontrols.oninit("C:/Users/kevle/Desktop/init.bat");
//			ccontrols.ondeinit("C:/Users/kevle/Desktop/test.bat");
			
			/*
			CHash options = __.chash(
				"C:/Users/kevle/Desktop/cezdev/cmeta/cprojects.json"
			);
			__.print(options.toString());

			ccontrols.select(
				"control4", 
				"control4", 
				options,
			null);
			
	
			ccontrols.checkbox("checkbox-control", "This is my Checkbox Control", null);				
		
			ccontrols.section("section-control", "section-control", null);
				ccontrols.radio("radio-control", "radio-control1", "Numbers", null);
				ccontrols.radio("radio-control", "radio-control2", "Alphabet", null);
				ccontrols.radio("radio-control", "radio-control3", "Symbols", null);
			ccontrols.endsection();	
			
/*
			//ccontrols.submit("submit", "SUBMIT", null);			
			//ccontrols.text("text-control", "This is my Text Control", null);	
			//ccontrols.textarea("textarea-control", "This is my TextArea Control", null);	
			//ccontrols.checkbox("checkbox-control", "This is my Checkbox Control", null);				
			//ccontrols.label("label-control", "This is my Label", null);			
			
			//ccontrols.controlFromJSONFile("C://Users//developer//Desktop//button.json");
			//ccontrols.controlFromJSONFile("C://Users//developer//Desktop//select.json");
		ccontrols.endform();
		
		__.println(ccontrols.toStringContents(50));
		__.alert("hello");
		
	
	/*
		ccontrols.sysmenubar("menubar", "C://Users//developer//Desktop//icon.png", null);
			ccontrols.sysmenu("menu1", "Menu1", null);
				ccontrols.sysmenuitem("item1", "item1", null);
				ccontrols.sysmenuitem_checkbox("checkboxitem", "checkboxitem", null);
				ccontrols.sysmenuitem_seperator();
				ccontrols.sysmenuitem("item2", "item2", null);
			ccontrols.endmenu();
		ccontrols.endsysmenubar();
	*/	


		ccontrols.retrieve("myform").setProp("grid","true");
		ccontrols.retrieve("myform").setProp("visible","true");
		ccontrols.retrieve("myform").setProp("pack","true");
		ccontrols.retrieve("myform").setProp("close","true");
		ccontrols.retrieve("myform section-control button-control").setProp("onclick", "./test.bat");
		ccontrols.retrieve("myform menubar menu1 item1").setProp("onclick", "./test.bat");


//		ccontrols.retrieve("myform control4").setProp("onchange", "C:/Users/kevle/Desktop/test.bat");
		
//		__.alert("hello, deleting....");
//		ccontrols.retrieve("myform control4").delete();
		
		//ccontrols.retrieve("myform control4").setProp("oninit", "C:/Users/kevle/Desktop/test.bat");
	
	
		//ccontrols.retrieve("myform control5").setProp("onclick", "C:/Users/kevle/Desktop/test.bat");
		//ccontrols.retrieve("myform radio-control1").setProp("onclick", "C:/Users/kevle/Desktop/test.bat");
//		ccontrols.retrieve("myform menubar menu1 item1").setProp("onclick", "C:/Users/kevle/Desktop/test.bat");
//		ccontrols.retrieve("myform menubar menu1 item2").setProp("onclick", "C:/Users/kevle/Desktop/test.bat");
//		ccontrols.retrieve("myform control5").setProp("onclick", "C:/Users/kevle/Desktop/test.bat");

//		ccontrols.retrieve("myform radio-control2").setProp("onclick", "C:/Users/kevle/Desktop/test.bat"); // end onclick


//		__.println(ccontrols.toStringContents());

//		ccontrols.retrieve("myform control5").setProp("text", "This was just set");
	
		//ccontrols.retrieve("myform control5").setProp("visible","false");
		/*
		ccontrols.retrieve("myform control5").setProp("onclick", new CFunction() { public CReturn call(CObject obj) {
			__.alert("myform control5");
			__.println("myform control5");
			return null;
		}}); // end onclick
		*/
		/*
		ccontrols.retrieve("myform control5").setProp("onclick", new CFunction() { public Object _(Object obj) {
			__.execCommand("C:/Users/developer/Desktop/test.bat");
			return null;
		}}); // end onclick
		*/
		
		
		
		/*
		ccontrols.retrieve("myform control5").setProp("onclick", "C:/Users/developer/Desktop/test.bat");
		
		ccontrols.retrieve("myform menubar menu1 menu2 item1").setProp("onclick", new CFunction() { public Object _(Object obj) {
			__.execCommand("alert mannnnnnnnnnnnnnnnnnn");
			return null;
		}}); // end onclick
		
		ccontrols.retrieve("myform section-control radio-control1").setProp("onclick",  new CFunction() { public Object _(Object obj) {
			__.execCommand("C:/Users/developer/Desktop/test.bat");
			return null;
		}}); // end onclick
		
		ccontrols.retrieve("myform section-control radio-control2").setProp("onclick",  new CFunction() { public Object _(Object obj) {
			__.execCommand("C:/Users/developer/Desktop/test.bat");
			return null;
		}}); // end onclick
		
		ccontrols.retrieve("myform control4").setProp("onclick",  new CFunction() { public Object _(Object obj) {
			__.execCommand("C:/Users/developer/Desktop/test.bat");
			return null;
		}}); // end onclick	
			*/
		
		
		//ccontrols.retrieve("myform text-control").setProp("onclick",  new CFunction() { public Object _(Object obj) {
		//	__.exec_command("C:/Users/developer/Desktop/test.bat");
		//	return null;
		//}}); // end onclick
		
		
		/*
		ccontrols.retrieve("myform textarea-control").setProp("onclick",  new CFunction() { public Object _(Object obj) {
			__.execCommand("C:/Users/developer/Desktop/test.bat");
			return null;
		}}); // end onclick
		
		ccontrols.retrieve("myform checkbox-control").setProp("onclick",  new CFunction() { public Object _(Object obj) {
			__.execCommand("C:/Users/developer/Desktop/test.bat");
			return null;
		}}); // end onclick
		
		ccontrols.retrieve("myform").setProp("title","FRAME");
		ccontrols.retrieve("myform control5").setProp("title","BUTTON");
		
		__.alert(ccontrols.retrieve("myform control5").getProp("visible"));
		__.println(ccontrols.toStringContents());
		*/
		
		//CHash chash = ccontrols.retrieve("ccontrolsprogram myform");
		//__.println(ccontrols.toStringContents(chash));
		/*
		ccontrols.hidden("cprogramtype","CControlsProgram", null);
		ccontrols.label("control2", "Control2 with attributes: ", null);
		ccontrols.set("data-attr1", "value1");
		ccontrols.set("data-attr2", "value2");
		ccontrols.set("data-attr3", "value3");	
		ccontrols.text("control2", "This is my Text Control With Html Attributes", null);
		ccontrols.clear(); // clear the attributes
		ccontrols.label("control3", "Control3 radio buttons: ", null);
		ccontrols.radio("control3","red", null);
		ccontrols.radio("control3","green", null);
		ccontrols.set("checked", "checked");
		ccontrols.radio("control3","blue", null);
		ccontrols.clear(); // clear the attributes
		ccontrols.label("control4", "Control7 select control: ", null);
		ccontrols.select( "control4", "HELLO3", {"HELLO5":"WORLD5", "HELLO1":"WORLD1","HELLO2":"WORLD2","HELLO3":"WORLD3"}, null);
		ccontrols.button("control5", "Control5", null);
		ccontrols.submit("control6", "Control6", null);
		*/	
		/*
		CControls ccontrols2 = new CControls();
		ccontrols2.create("ccontrolsprogram-js", null);	
		CControls ccontrols2 = ccontrols.getCControls();	
		ccontrols2.form("form", "myform", null);
		ccontrols2.endform();
		*/
		//System.gc(); // request GC
	} // end main()
} // end CMessageBox
