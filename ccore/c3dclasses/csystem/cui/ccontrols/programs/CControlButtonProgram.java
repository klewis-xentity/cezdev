//-------------------------------------------------------
// name: CControlButtonProgram.java
// desc: Demonstrates basic button usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlButtonProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("button-form", "Button Demo", null);
			ccontrols.label("button-label", "Click the button to trigger the event.", null);
			ccontrols.button("demo-button", "Press Me", null);
		ccontrols.endform();

		ccontrols.retrieve("button-form").setProp("grid", "true");
		ccontrols.retrieve("button-form").setProp("visible", "true");
		ccontrols.retrieve("button-form").setProp("pack", "true");
		ccontrols.retrieve("button-form").setProp("close", "true");

		ccontrols.retrieve("button-form demo-button").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				__.alert("Button was clicked!");
				__.println("Button was clicked!");
				return null;
			}
		});
	}
}
