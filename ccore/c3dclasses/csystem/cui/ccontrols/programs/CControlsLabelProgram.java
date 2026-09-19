//-------------------------------------------------------
// name: CControlsLabelProgram.java
// desc: Demonstrates label control usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlsLabelProgram {
    public static void main(String[] args) {
        CControls ccontrols = new CControls();

        ccontrols.form("label-form", "Label Control Demo", null);
        ccontrols.label("title", "Label Control", null);
        ccontrols.label("description", "This label demonstrates the common label instructions.", null);
        ccontrols.label("status", "Ready", null);
        ccontrols.endform();

        CControl title = ccontrols.retrieve("label-form title");
        title.setProp("text", "Updated Label Control");
        title.setProp("font", "Dialog-BOLD-18");
        title.setProp("horizontalalignment", "center");
        title.setProp("verticalalignment", "center");

        CControl description = ccontrols.retrieve("label-form description");
        description.setProp("enable", "true");

        CControl form = ccontrols.retrieve("label-form");
        form.setProp("grid", "true");
        form.setProp("visible", "true");
        form.setProp("pack", "true");
        form.setProp("close", "true");

        __.println(ccontrols.toStringContents());
    }
}
