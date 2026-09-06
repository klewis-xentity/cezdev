//-------------------------------------------------------
// name: CControlsColorProgram.java
// desc: Demonstrates color picker usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlsColorProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("color-form", "Color Picker Demo", null);
			ccontrols.label("color-label", "Pick a color and click Show Selected Color.", null);
			ccontrols.colorpicker("favorite-color", "#2E86C1", null);
			ccontrols.button("show-color", "Show Selected Color", null);
		ccontrols.endform();

		ccontrols.retrieve("color-form").setProp("grid", "true");
		ccontrols.retrieve("color-form").setProp("visible", "true");
		ccontrols.retrieve("color-form").setProp("pack", "true");
		ccontrols.retrieve("color-form").setProp("close", "true");

		ccontrols.retrieve("color-form show-color").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				CControl color = ccontrols.retrieve("color-form favorite-color");
				String selected = (String) color.getProp("value");
				__.alert("Selected color: " + selected);
				__.println("Selected color: " + selected);
				return null;
			}
		});
	}
}
