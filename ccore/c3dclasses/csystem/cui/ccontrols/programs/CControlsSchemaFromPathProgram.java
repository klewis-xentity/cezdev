//-------------------------------------------------------
// name: CControlsSchemaFromPathProgram.java
// desc: tests CControlsSchema.createCControlsFromPath() for directory paths
//-------------------------------------------------------
package c3dclasses;

import java.io.File;

public class CControlsSchemaFromPathProgram {
    public static void main(String[] args) {
        String inputPath = __.dir_path("CControlsSchemaFromPathProgram.java") + "/test";
       
        File path = new File(inputPath.trim());
        if (!path.exists() || !path.isDirectory()) {
            System.out.println("Not a valid directory: " + inputPath);
            return;
        }

        CControlsSchema schema = new CControlsSchema();
        CControls ccontrols = schema.createCControlsFromPath(path.getAbsolutePath());

        if (ccontrols == null) {
            System.out.println("createCControlsFromPath returned null for directory: " + path.getAbsolutePath());
            return;
        }

        System.out.println("Directory: " + path.getAbsolutePath());
        System.out.println("Form id: " + schema.getFormId());

        CArray keys = ccontrols.getCControls().keys();
        if (keys == null) {
            System.out.println("No controls were generated.");
            return;
        }

        for (int i = 0; i < keys.length(); i++) {
            String controlPath = String.valueOf(keys._(i));
            CControl control = ccontrols.retrieve(controlPath);
            if (control != null) {
                control.setProp("visible", "true");
                control.setProp("pack", "true");
                control.setProp("close", "true");
                if ("form".equals(control._("m_strtype")) || "panel".equals(control._("m_strtype"))) {
                    control.setProp("grid", "true");
                }
            }
            System.out.println((i + 1) + ") " + controlPath + " -> " + (control == null ? "null" : control._("m_strtype")));
        }

        System.out.println("Generated controls: " + keys.length());
    }
}
