//-------------------------------------------------------
// name: CControlsProgram3.java
// desc: 
//-------------------------------------------------------
import c3dclasses.*;
import java.io.File;


public class CControlsProgram4 {		
	 public static void main(String[] args) {		
	    String strUiSchemaJson = "{"
                + "\"type\":\"form\"," 
                + "\"layout\":\"vertical\"," 
                + "\"title\":\"Article Form\"," 
                + "\"fields\":["
                    + "{\"name\":\"title\",\"label\":\"Title\",\"component\":\"text\"},"
                    + "{\"name\":\"description\",\"label\":\"Description\",\"component\":\"textarea\"},"
                    + "{\"name\":\"status\",\"label\":\"Status\",\"component\":\"select\",\"options\":[\"Draft\",\"Published\",\"Archived\"]},"
                    + "{\"name\":\"environment\",\"label\":\"Environment\",\"component\":\"select\",\"optionFromDirectoryPath\":\"C:\\\\Users\\\\kevle\\\\Desktop\\\\cezdev\\\\cplatform\\\\cenvironments\"},"
                    + "{\"name\":\"active\",\"label\":\"Active\",\"component\":\"checkbox\"},"
                    + "{\"name\":\"save\",\"label\":\"Save Article\",\"component\":\"button\",\"onclick\":\"test.bat\"}"
                + "]"
                + "}";

        CControlsSchemaRender renderer = new CControlsSchemaRender();
        CControls ccontrols = renderer.renderFromJson(strUiSchemaJson);

        if (ccontrols == null) {
            __.alert("Failed to render schema form");
            return;
        }

        // Use renderer.getField() so paths are always correct regardless of form id.
        CControl titleControl = renderer.getField(ccontrols, "title");
        if (titleControl != null) {
            Object text = titleControl.getProp("text");
            if (text == null || String.valueOf(text).trim().equals("")) {
                titleControl.setProp("text", "My Title");
            }
        }

        CControl descControl = renderer.getField(ccontrols, "description");
        if (descControl != null) {
            Object text = descControl.getProp("text");
            if (text == null || String.valueOf(text).trim().equals("")) {
                descControl.setProp("text", "Initial description");
            }
        }

        CControl statusControl = renderer.getField(ccontrols, "status");
        if (statusControl != null) {
            Object selected = statusControl.getProp("selected");
            if (selected == null || String.valueOf(selected).trim().equals("")) {
                statusControl.setProp("selected", "Draft");
            }
        }

        CControl environmentControl = renderer.getField(ccontrols, "environment");
        if (environmentControl != null) {
            Object selected = environmentControl.getProp("selected");
            if (selected == null || String.valueOf(selected).trim().equals("")) {
                environmentControl.setProp("selected", "cjava");
            }
        }

        CControl activeControl = renderer.getField(ccontrols, "active");
        if (activeControl != null) {
            Object selected = activeControl.getProp("selected");
            if (selected == null) {
                activeControl.setProp("selected", Boolean.TRUE);
            }
        }

        // Fallback for environments still running an older renderer from the prebuilt jar.
        CControl saveControl = renderer.getField(ccontrols, "save");
        if (saveControl == null) {
            String strFormId = renderer.getFormId();
            if (ccontrols.retrieve(strFormId + " save_btn") == null) {
                ccontrols.form(strFormId, null, null);
                ccontrols.button("save_btn", "Save Article", null);
                ccontrols.endform();
            }
            saveControl = ccontrols.retrieve(strFormId + " save_btn");
        }
        if (saveControl != null) saveControl.setProp("onclick", "test.bat");

        // Show the form using the form id from the renderer.
        String strFormId = renderer.getFormId();
        ccontrols.retrieve(strFormId).setProp("visible", "true");
        ccontrols.retrieve(strFormId).setProp("pack", "true");
        ccontrols.retrieve(strFormId).setProp("close", "true");

        // Read values back.
        __.println("title="       + (titleControl  != null ? titleControl.getProp("text")     : "n/a"));
        __.println("description=" + (descControl   != null ? descControl.getProp("text")      : "n/a"));
        __.println("status="      + (statusControl != null ? statusControl.getProp("selected"): "n/a"));
        __.println("environment=" + (environmentControl != null ? environmentControl.getProp("selected"): "n/a"));
        __.println("active="      + (activeControl != null ? activeControl.getProp("selected"): "n/a"));
    }

} // end CControlsProgram3
