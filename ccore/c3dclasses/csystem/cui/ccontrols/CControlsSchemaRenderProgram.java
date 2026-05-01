//-------------------------------------------------------
// name: CControlsSchemaRenderProgram.java
// desc: Driver code for CControlsSchemaRender usage
//-------------------------------------------------------
package c3dclasses;

public class CControlsSchemaRenderProgram {
    public static void main(String[] args) {
        String strUiSchemaJson = "{"
                + "\"type\":\"form\"," 
                + "\"layout\":\"vertical\"," 
                + "\"title\":\"Article Form\"," 
                + "\"fields\":["
                    + "{\"name\":\"title\",\"label\":\"Title\",\"component\":\"text\"},"
                    + "{\"name\":\"description\",\"label\":\"Description\",\"component\":\"textarea\"},"
                    + "{\"name\":\"status\",\"label\":\"Status\",\"component\":\"select\",\"options\":[\"Draft\",\"Published\",\"Archived\"]},"
                    + "{\"name\":\"active\",\"label\":\"Active\",\"component\":\"checkbox\"}"
                + "]"
                + "}";

        CControlsSchemaRender renderer = new CControlsSchemaRender();
        CControls ccontrols = renderer.renderFromJson(strUiSchemaJson);

        if (ccontrols == null) {
            __.alert("Failed to render schema form");
            return;
        }

        // Optional field defaults
        ccontrols.retrieve("schema-form title").setProp("text", "My Title");
        ccontrols.retrieve("schema-form description").setProp("text", "Initial description");
        ccontrols.retrieve("schema-form status").setProp("selected", "Draft");
        ccontrols.retrieve("schema-form active").setProp("selected", Boolean.TRUE);

        // Show form
        ccontrols.retrieve("schema-form").setProp("visible", "true");
        ccontrols.retrieve("schema-form").setProp("pack", "true");
        ccontrols.retrieve("schema-form").setProp("close", "true");

        // Example: read values back
        __.println("title=" + ccontrols.retrieve("schema-form title").getProp("text"));
        __.println("description=" + ccontrols.retrieve("schema-form description").getProp("text"));
        __.println("status=" + ccontrols.retrieve("schema-form status").getProp("selected"));
        __.println("active=" + ccontrols.retrieve("schema-form active").getProp("selected"));
    }
}
