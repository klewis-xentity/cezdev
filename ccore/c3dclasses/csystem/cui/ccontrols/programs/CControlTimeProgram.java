//-------------------------------------------------------
// name: CControlTimeProgram.java
// desc: Demonstrates time picker usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlTimeProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("time-form", "Time Picker Demo", null);
			ccontrols.label("time-label", "Choose a time and click Show Time.", null);
			ccontrols.timepicker("selected-time", "09:30", null);
			ccontrols.button("show-time", "Show Time", null);
		ccontrols.endform();

		ccontrols.retrieve("time-form").setProp("grid", "true");
		ccontrols.retrieve("time-form").setProp("visible", "true");
		ccontrols.retrieve("time-form").setProp("pack", "true");
		ccontrols.retrieve("time-form").setProp("close", "true");

		ccontrols.retrieve("time-form selected-time").setProp("format", "HH:mm");

		ccontrols.retrieve("time-form show-time").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				Object value = ccontrols.retrieve("time-form selected-time").getProp("value");
				String selected = value == null ? "" : String.valueOf(value);
				__.alert("Selected time: " + selected);
				__.println("Selected time: " + selected);
				return null;
			}
		});
	}
}
