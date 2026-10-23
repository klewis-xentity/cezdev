//-------------------------------------------------------
// name: CControlCheckboxProgram.java
// desc: Demonstrates checkbox usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlCheckboxProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("checkbox-form", "Checkbox Demo", null);
			ccontrols.label("checkbox-label", "Select the options you want and click Show Selection.", null);
			ccontrols.checkbox("agree-terms", "I agree to the terms", null);
			ccontrols.checkbox("newsletter", "Send me product updates", null);
			ccontrols.checkbox("debug-mode", "Enable debug mode", null);
			ccontrols.button("show-selection", "Show Selection", null);
		ccontrols.endform();

		ccontrols.retrieve("checkbox-form").setProp("grid", "true");
		ccontrols.retrieve("checkbox-form").setProp("visible", "true");
		ccontrols.retrieve("checkbox-form").setProp("pack", "true");
		ccontrols.retrieve("checkbox-form").setProp("close", "true");

		ccontrols.retrieve("checkbox-form agree-terms").setProp("selected", Boolean.TRUE);
		ccontrols.retrieve("checkbox-form newsletter").setProp("selected", Boolean.FALSE);
		ccontrols.retrieve("checkbox-form debug-mode").setProp("selected", Boolean.TRUE);

		ccontrols.retrieve("checkbox-form show-selection").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				boolean agree = ((Boolean) ccontrols.retrieve("checkbox-form agree-terms").getProp("selected")).booleanValue();
				boolean newsletter = ((Boolean) ccontrols.retrieve("checkbox-form newsletter").getProp("selected")).booleanValue();
				boolean debug = ((Boolean) ccontrols.retrieve("checkbox-form debug-mode").getProp("selected")).booleanValue();

				__.alert("Selected: agree=" + agree + ", newsletter=" + newsletter + ", debug=" + debug);
				__.println("Selected: agree=" + agree + ", newsletter=" + newsletter + ", debug=" + debug);
				return null;
			}
		});
	}
}
