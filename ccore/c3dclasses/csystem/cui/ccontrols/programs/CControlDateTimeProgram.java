//-------------------------------------------------------
// name: CControlDateTimeProgram.java
// desc: Demonstrates date-time picker usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlDateTimeProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("datetime-form", "DateTime Picker Demo", null);
			ccontrols.label("datetime-label", "Select a date and time and click Show DateTime.", null);
			ccontrols.datetimepicker("selected-datetime", "2026-10-23 09:30", null);
			ccontrols.button("show-datetime", "Show DateTime", null);
		ccontrols.endform();

		ccontrols.retrieve("datetime-form").setProp("grid", "true");
		ccontrols.retrieve("datetime-form").setProp("visible", "true");
		ccontrols.retrieve("datetime-form").setProp("pack", "true");
		ccontrols.retrieve("datetime-form").setProp("close", "true");

		ccontrols.retrieve("datetime-form selected-datetime").setProp("format", "yyyy-MM-dd HH:mm");

		ccontrols.retrieve("datetime-form show-datetime").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				Object value = ccontrols.retrieve("datetime-form selected-datetime").getProp("value");
				String selected = value == null ? "" : String.valueOf(value);
				__.alert("Selected date and time: " + selected);
				__.println("Selected date and time: " + selected);
				return null;
			}
		});
	}
}
