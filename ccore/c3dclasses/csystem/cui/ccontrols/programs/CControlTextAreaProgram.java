//-------------------------------------------------------
// name: CControlTextAreaProgram.java
// desc: Demonstrates multi-line text area usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlTextAreaProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("text-area-form", "Text Area Demo", null);
			ccontrols.label("text-area-label", "Type a note and click Show Text.", null);
			ccontrols.textarea("notes", "Write your notes here...", null);
			ccontrols.button("show-text", "Show Text", null);
		ccontrols.endform();

		ccontrols.retrieve("text-area-form").setProp("grid", "true");
		ccontrols.retrieve("text-area-form").setProp("visible", "true");
		ccontrols.retrieve("text-area-form").setProp("pack", "true");
		ccontrols.retrieve("text-area-form").setProp("close", "true");

		ccontrols.retrieve("text-area-form show-text").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				String value = (String) ccontrols.retrieve("text-area-form notes").getProp("value");
				__.alert("Text area value: " + value);
				__.println("Text area value: " + value);
				return null;
			}
		});
	}
}
