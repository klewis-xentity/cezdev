//-------------------------------------------------------
// name: CControlDateProgram.java
// desc: Demonstrates date picker usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlDateProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("date-form", "Date Picker Demo", null);
			ccontrols.label("date-label", "Choose a date and click Show Date.", null);
			ccontrols.create("datepicker", "selected-date", "2026-10-23", null);
			ccontrols.button("show-date", "Show Date", null);
		ccontrols.endform();

		ccontrols.retrieve("date-form").setProp("grid", "true");
		ccontrols.retrieve("date-form").setProp("visible", "true");
		ccontrols.retrieve("date-form").setProp("pack", "true");
		ccontrols.retrieve("date-form").setProp("close", "true");

		ccontrols.retrieve("date-form selected-date").setProp("format", "yyyy-MM-dd");

		ccontrols.retrieve("date-form show-date").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				CControl date = ccontrols.retrieve("date-form selected-date");
				String selected = (String) date.getProp("value");
				__.alert("Selected date: " + selected);
				__.println("Selected date: " + selected);
				return null;
			}
		});
	}
}
