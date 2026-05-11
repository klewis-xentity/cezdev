//-------------------------------------------------------
// name: CControlsSchemaRenderProgram.java
// desc: Driver code for CControlsSchemaRender usage
//-------------------------------------------------------
package c3dclasses;

public class CControlsSchemaRenderProgram {
    public static void main(String[] args) {
        String strUiSchemaJson = "{"
                + "\"type\":\"form\"," 
                + "\"id\":\"article-form\","
                + "\"layout\":\"vertical\"," 
                + "\"title\":\"Article Form\"," 
                + "\"fields\":["
                    + "{\"name\":\"title\",\"label\":\"Title\",\"component\":\"text\"},"
                    + "{\"name\":\"description\",\"label\":\"Description\",\"component\":\"textarea\"},"
                    + "{"
                        + "\"name\":\"metadata\",\"label\":\"Metadata\",\"type\":\"panel\","
                        + "\"fields\":["
                            + "{\"name\":\"status\",\"label\":\"Status\",\"component\":\"select\",\"options\":[\"Draft\",\"Published\",\"Archived\"]},"
                            + "{\"name\":\"active\",\"label\":\"Active\",\"component\":\"checkbox\"}"
                        + "]"
                    + "}"
                + "]"
                + "}";

        CControlsSchemaRender renderer = new CControlsSchemaRender();
        CControls ccontrols = renderer.renderFromJson(strUiSchemaJson);

        if (ccontrols == null) {
            __.alert("Failed to render schema form");
            return;
        }

        CControl formControl = ccontrols.retrieve("article-form"); // Get form control reference

        // Add a new panel dynamically with fields
        String panelFieldsJson = "{\"fields\":[" +
            "{\"name\":\"author\",\"label\":\"Author\",\"component\":\"text\"}," +
            "{\"name\":\"revision\",\"label\":\"Revision\",\"component\":\"text\"}" +
        "]}";
        CControls updatedControls = formControl.addNewPanel(panelFieldsJson, "metadata2", "Metadata2");

        if (updatedControls == null) {
            __.alert("Failed to add new panel");
            return;
        }

        // Optional field defaults
        ccontrols.retrieve("article-form title").setProp("text", "My Title");
        ccontrols.retrieve("article-form description").setProp("text", "Initial description");
        ccontrols.retrieve("article-form metadata status").setProp("selected", "Draft");
        ccontrols.retrieve("article-form metadata active").setProp("selected", Boolean.TRUE);

        // Show form
        ccontrols.retrieve("article-form").setProp("visible", "true");
        ccontrols.retrieve("article-form").setProp("pack", "true");
        ccontrols.retrieve("article-form").setProp("close", "true");

        // Example: read values back
        __.println("title=" + ccontrols.retrieve("article-form title").getProp("text"));
        __.println("description=" + ccontrols.retrieve("article-form description").getProp("text"));
        __.println("status=" + ccontrols.retrieve("article-form metadata status").getProp("selected"));
        __.println("active=" + ccontrols.retrieve("article-form metadata active").getProp("selected"));
    }
}
