//-------------------------------------------------------
// name: CControlTextFieldProgram.java
// desc: Demonstrates single-line text field usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlTextFieldProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("text-field-form", "Text Field Demo", null);
			ccontrols.label("text-field-label", "Type a value and click Show Text.", null);
			ccontrols.text("customer-name", "Jane Doe", null);
			ccontrols.button("show-text", "Show Text", null);
		ccontrols.endform();

		ccontrols.retrieve("text-field-form").setProp("grid", "true");
		ccontrols.retrieve("text-field-form").setProp("visible", "true");
		ccontrols.retrieve("text-field-form").setProp("pack", "true");
		ccontrols.retrieve("text-field-form").setProp("close", "true");

		ccontrols.retrieve("text-field-form show-text").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				String value = (String) ccontrols.retrieve("text-field-form customer-name").getProp("value");
				__.alert("Text field value: " + value);
				__.println("Text field value: " + value);
				return null;
			}
		});
	}
}
