//-------------------------------------------------------
// name: CControlToggleProgram.java
// desc: Demonstrates toggle usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlToggleProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("toggle-form", "Toggle Demo", null);
			ccontrols.label("toggle-label", "Switch the toggle and click Show State.", null);
			ccontrols.create("toggle", "feature-toggle", "Feature Enabled", __.chash(
				"m_selected", Boolean.TRUE,
				"m_onlabel", "Enabled",
				"m_offlabel", "Disabled"
			));
			ccontrols.button("show-state", "Show State", null);
		ccontrols.endform();

		ccontrols.retrieve("toggle-form").setProp("grid", "true");
		ccontrols.retrieve("toggle-form").setProp("visible", "true");
		ccontrols.retrieve("toggle-form").setProp("pack", "true");
		ccontrols.retrieve("toggle-form").setProp("close", "true");

		ccontrols.retrieve("toggle-form show-state").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				boolean selected = ((Boolean) ccontrols.retrieve("toggle-form feature-toggle").getProp("selected")).booleanValue();
				__.alert("Toggle selected: " + selected);
				__.println("Toggle selected: " + selected);
				return null;
			}
		});
	}
}
