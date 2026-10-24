//-------------------------------------------------------
// name: CControlRadioButtonProgram.java
// desc: Demonstrates radio button usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlRadioButtonProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("radio-form", "Radio Button Demo", null);
			ccontrols.label("radio-label", "Choose your favorite color.", null);
			ccontrols.radio("favorite-color", "radio-blue", "Blue", null);
			ccontrols.radio("favorite-color", "radio-green", "Green", null);
			ccontrols.radio("favorite-color", "radio-red", "Red", null);
			ccontrols.button("show-selection", "Show Selection", null);
		ccontrols.endform();

		boolean blue = Boolean.parseBoolean(String.valueOf(CControlStateMemory.load(ccontrols.retrieve("radio-form radio-blue"))));
		boolean green = Boolean.parseBoolean(String.valueOf(CControlStateMemory.load(ccontrols.retrieve("radio-form radio-green"))));
		boolean red = Boolean.parseBoolean(String.valueOf(CControlStateMemory.load(ccontrols.retrieve("radio-form radio-red"))));

		ccontrols.retrieve("radio-form").setProp("grid", "true");
		ccontrols.retrieve("radio-form").setProp("visible", "true");
		ccontrols.retrieve("radio-form").setProp("pack", "true");
		ccontrols.retrieve("radio-form").setProp("close", "true");

		ccontrols.retrieve("radio-form radio-blue").setProp("selected", blue);
		ccontrols.retrieve("radio-form radio-green").setProp("selected", green);
		ccontrols.retrieve("radio-form radio-red").setProp("selected", red);

		if (!blue && !green && !red) {
			ccontrols.retrieve("radio-form radio-blue").setProp("selected", Boolean.TRUE);
		}

		ccontrols.retrieve("radio-form show-selection").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				boolean blue = Boolean.TRUE.equals(ccontrols.retrieve("radio-form radio-blue").getProp("selected"));
				boolean green = Boolean.TRUE.equals(ccontrols.retrieve("radio-form radio-green").getProp("selected"));
				boolean red = Boolean.TRUE.equals(ccontrols.retrieve("radio-form radio-red").getProp("selected"));
				String selection = blue ? "Blue" : (green ? "Green" : (red ? "Red" : "None"));
				__.alert("Selected color: " + selection);
				__.println("Selected color: " + selection);
				return null;
			}
		});
	}
}
