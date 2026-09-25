//-------------------------------------------------------
// name: CControlsSchemaProgram.java
// desc: Driver code for CControlsSchema usage
//-------------------------------------------------------
package c3dclasses;

public class CControlsSchemaProgram {
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

        CControlsSchema schema = new CControlsSchema();
        CControls ccontrols = schema.createCControls(strUiSchemaJson);
        if (ccontrols == null) {
            __.alert("Failed to render schema form");
            return;
        }

        CControl formControl = ccontrols.retrieve(schema.getFormId());
        if (formControl == null) {
            __.alert("Form control was not created");
            return;
        }

        // Add a new panel dynamically with fields through CControlsSchema integration.
        String panelFieldsJson = "{\"fields\":["
            + "{\"name\":\"author\",\"label\":\"Author\",\"component\":\"text\"},"
            + "{\"name\":\"revision\",\"label\":\"Revision\",\"component\":\"text\"}"
            + "]}";
        if (formControl.addNewPanel(panelFieldsJson, "metadata2", "Metadata2") == null) {
            __.alert("Failed to add new panel");
            return;
        }

        CControl titleField = schema.getField(ccontrols, "title");
        CControl descriptionField = schema.getField(ccontrols, "description");
        CControl statusField = schema.getField(ccontrols, "status");
        CControl activeField = schema.getField(ccontrols, "active");

        if (titleField != null) {
            titleField.setProp("text", "My Title");
        }
        if (descriptionField != null) {
            descriptionField.setProp("text", "Initial description");
        }
        if (statusField != null) {
            statusField.setProp("selected", "Draft");
        }
        if (activeField != null) {
            activeField.setProp("selected", Boolean.TRUE);
        }

        formControl.setProp("visible", "true");
        formControl.setProp("pack", "true");
        formControl.setProp("close", "true");

        __.println("title=" + (titleField == null ? null : titleField.getProp("text")));
        __.println("description=" + (descriptionField == null ? null : descriptionField.getProp("text")));
        __.println("status=" + (statusField == null ? null : statusField.getProp("selected")));
        __.println("active=" + (activeField == null ? null : activeField.getProp("selected")));
    }
}
