//-------------------------------------------------------
// name: CControlsComboboxProgram.java
// desc: Demonstrates combobox/select usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlsComboboxProgram {
    public static void main(String[] args) {
        CControls ccontrols = new CControls();

        CHash countries = __.chash(
            "us", "United States",
            "uk", "United Kingdom",
            "ca", "Canada",
            "au", "Australia",
            "de", "Germany"
        );

        ccontrols.form("combobox-form", "Combobox Demo", null);
            ccontrols.label("combobox-label", "Pick a country and click Show Selection.", null);
            ccontrols.select("country", "uk", countries, null);
            ccontrols.button("show-country", "Show Selection", null);
        ccontrols.endform();

        ccontrols.retrieve("combobox-form").setProp("grid", "true");
        ccontrols.retrieve("combobox-form").setProp("visible", "true");
        ccontrols.retrieve("combobox-form").setProp("pack", "true");
        ccontrols.retrieve("combobox-form").setProp("close", "true");

        ccontrols.retrieve("combobox-form country").setProp("selected", "ca");

        ccontrols.retrieve("combobox-form show-country").setProp("onclick", new CFunction() {
            public CReturn call(CObject obj) {
                Object selected = ccontrols.retrieve("combobox-form country").getProp("selected");
                __.alert("Selected country: " + selected);
                __.println("Selected country: " + selected);
                return null;
            }
        });
    }
}
