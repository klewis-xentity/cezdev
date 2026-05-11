//-------------------------------------------------------
// name: CControlsSchemaRender.java
// desc: Renders a simple JSON UI schema into CControls
//-------------------------------------------------------
package c3dclasses;

import java.io.File;

public class CControlsSchemaRender {
    // Populated during render so callers can look up paths without guessing.
    private String m_formId = null;
    private CHash m_fieldPaths = new CHash(); // fieldName -> pathId

    public CControlsSchemaRender() {
    }

    // Returns the form path id (e.g. "schema-form") after render has run.
    public String getFormId() {
        return this.m_formId;
    }

    // Returns the full pathId used to retrieve a field by name
    // (e.g. "schema-form title"), or null when the field was not rendered.
    public String getFieldPath(String strFieldName) {
        if (strFieldName == null) return null;
        return (String) this.m_fieldPaths._string(strFieldName);
    }

    // Convenience: retrieve the CControl for a field by name.
    public CControl getField(CControls ccontrols, String strFieldName) {
        String pathId = this.getFieldPath(strFieldName);
        return (pathId != null) ? ccontrols.retrieve(pathId) : null;
    }

    public CControls renderFromFile(String strUiSchemaPath) {
        String strJson = __.get_file_contents(strUiSchemaPath);
        if (strJson == null || strJson.trim().equals("")) {
            return null;
        }
        return this.renderFromJson(strJson);
    }

    public CControls renderFromJson(String strUiSchemaJson) {
        if (strUiSchemaJson == null || strUiSchemaJson.trim().equals("")) {
            return null;
        }

        CHash uiSchema = CJSON.decode(strUiSchemaJson);
        if (uiSchema == null) {
            return null;
        }

        return this.render(uiSchema, new CControls());
    }

    public CControls render(CHash uiSchema, CControls ccontrols) {
        if (uiSchema == null) {
            return null;
        }

        if (ccontrols == null) {
            ccontrols = new CControls();
        }

        String strType = uiSchema._string("type");
        if (strType == null || !strType.equalsIgnoreCase("form")) {
            return null;
        }

        String strLayout = uiSchema._string("layout");
        String strFormId = this.getOrDefault(uiSchema._string("id"), "schema-form");
        String strFormTitle = this.getOrDefault(uiSchema._string("title"), "Schema Form");

        // Reset tracking state for this render pass.
        this.m_formId = strFormId;
        this.m_fieldPaths = new CHash();

        ccontrols.form(strFormId, strFormTitle, null);

        Object fieldsObj = uiSchema._("fields");
        CArray fields = (fieldsObj instanceof CArray) ? (CArray) fieldsObj : null;
        if (fields != null) {
            this.renderFields(ccontrols, strFormId, fields);
        }

        ccontrols.endform();

        if (strLayout == null || strLayout.equalsIgnoreCase("vertical")) {
            ccontrols.retrieve(strFormId).setProp("grid", "true");
        }

        return ccontrols;
    }

    private void renderFields(CControls ccontrols, String strParentId, CArray fields) {
        if (fields == null) {
            return;
        }
        for (int i = 0; i < fields.length(); i++) {
            Object fieldObj = fields._(i);
            if (!(fieldObj instanceof CHash)) {
                continue;
            }
            this.renderField(ccontrols, strParentId, (CHash) fieldObj, i);
        }
    }

    private void renderField(CControls ccontrols, String strFormId, CHash field, int index) {
        String strName = this.getOrDefault(field._string("name"), "field" + index);
        String strLabel = this.getOrDefault(field._string("label"), strName);
        String strType = this.getOrDefault(field._string("type"), "").toLowerCase();
        String strComponent = this.getOrDefault(field._string("component"), "text").toLowerCase();

        // Handle nested containers (panel, section)
        if (strType.equals("panel") || strType.equals("section")) {
            ccontrols.panel(strName, strLabel, null);
            
            // Recursively render nested fields
            Object nestedFieldsObj = field._("fields");
            CArray nestedFields = (nestedFieldsObj instanceof CArray) ? (CArray) nestedFieldsObj : null;
            if (nestedFields != null) {
                this.renderFields(ccontrols, strFormId, nestedFields);
            }
            
            ccontrols.endpanel();
            return;
        }

        String strLabelId = strName + "-label";
        String strInputId = strName;

        if (strComponent.equals("checkbox")) {
            ccontrols.checkbox(strInputId, strLabel, null);
            String pathId = strFormId + " " + strInputId;
            this.m_fieldPaths._(strName, pathId);
            CControl ccontrol = ccontrols.retrieve(pathId);
            if (ccontrol != null) {
                ccontrol._("m_field_name", strName);
                ccontrol._("m_component", strComponent);
            }
            return;
        }

        if (strComponent.equals("button")) {
            ccontrols.button(strInputId, strLabel, null);
            String pathId = strFormId + " " + strInputId;
            this.m_fieldPaths._(strName, pathId);
            CControl ccontrol = ccontrols.retrieve(pathId);
            if (ccontrol != null) {
                ccontrol._("m_field_name", strName);
                ccontrol._("m_component", strComponent);
            }
            return;
        }

        ccontrols.label(strLabelId, strLabel, null);

        if (strComponent.equals("textarea")) {
            ccontrols.textarea(strInputId, "", null);
        } else if (strComponent.equals("select")) {
            ccontrols.select(strInputId, null, this.optionsToHash(field), null);
        } else {
            ccontrols.text(strInputId, "", null);
        }

        String pathId = strFormId + " " + strInputId;
        this.m_fieldPaths._(strName, pathId);
        CControl ccontrol = ccontrols.retrieve(pathId);
        if (ccontrol != null) {
            ccontrol._("m_field_name", strName);
            ccontrol._("m_component", strComponent);
        }
    }

    private CHash optionsToHash(CHash field) {
        String strDirectoryPath = field._string("optionFromDirectoryPath");
        if (strDirectoryPath == null || strDirectoryPath.trim().equals("")) {
            strDirectoryPath = field._string("optionsFromDirectoryPath");
        }
        if (strDirectoryPath != null && !strDirectoryPath.trim().equals("")) {
            return this.optionsFromDirectory(strDirectoryPath);
        }

        CHash optionsHash = __.chash();
        Object optionsObj = field._("options");
        if (!(optionsObj instanceof CArray)) {
            return optionsHash;
        }

        CArray options = (CArray) optionsObj;
        for (int i = 0; i < options.length(); i++) {
            Object option = options._(i);
            if (option == null) {
                continue;
            }
            String strOption = String.valueOf(option);
            optionsHash.set(strOption, strOption);
        }

        return optionsHash;
    }

    private CHash optionsFromDirectory(String strDirectoryPath) {
        CHash optionsHash = __.chash();
        String resolvedPath = this.resolveDirectoryPath(strDirectoryPath);
        if (resolvedPath == null || resolvedPath.trim().equals("")) {
            return optionsHash;
        }

        File directory = new File(resolvedPath);
        File[] entries = directory.listFiles(File::isDirectory);
        if (entries == null) {
            return optionsHash;
        }

        for (File entry : entries) {
            optionsHash.set(entry.getName(), entry.getAbsolutePath());
        }

        return optionsHash;
    }

    private String resolveDirectoryPath(String strDirectoryPath) {
        if (strDirectoryPath == null) {
            return null;
        }

        String path = strDirectoryPath.trim();
        if (path.equals("/cenvironments") || path.startsWith("/cenvironments/")) {
            String strEnvironments = System.getenv("CENVIRONMENTS");
            if (strEnvironments != null && !strEnvironments.trim().equals("")) {
                String suffix = path.substring("/cenvironments".length());
                if (suffix.startsWith("/")) suffix = suffix.substring(1);
                return suffix.equals("")
                        ? strEnvironments
                        : strEnvironments + File.separator + suffix.replace("/", File.separator);
            }
        }

        return path.replace("/", File.separator);
    }

    private String getOrDefault(String value, String fallback) {
        if (value == null || value.trim().equals("")) {
            return fallback;
        }
        return value;
    }

    /**
     * Renders fields from a schema JSON string into an existing container control.
     * Used for dynamically adding fields to a running form/panel/container.
     * 
     * @param strUiSchemaJson JSON schema containing fields array
     * @param ccontrols The CControls manager object
     * @param strParentId The path ID of the parent control to add fields to
     * @return The CControls object for chaining, or null if parsing failed
     */
    public CControls renderFieldsIntoContainer(String strUiSchemaJson, CControls ccontrols, String strParentId) {
        if (strUiSchemaJson == null || strUiSchemaJson.trim().equals("")) {
            return null;
        }

        CHash uiSchema = CJSON.decode(strUiSchemaJson);
        if (uiSchema == null) {
            return null;
        }

        // If the schema itself is an object with a "fields" array, use that
        // Otherwise, return null as fields are required
        Object fieldsObj = uiSchema._("fields");
        CArray fields = (fieldsObj instanceof CArray) ? (CArray) fieldsObj : null;

        if (fields == null) {
            return null;
        }

        // Push the parent control onto the container stack so nested controls are added as children
        CControl parentControl = ccontrols.retrieve(strParentId);
        if (parentControl != null) {
            ccontrols.getContainers().push(parentControl);
        }

        // Render the fields into this container
        this.renderFields(ccontrols, strParentId, fields);

        // Pop the parent control from the stack
        if (parentControl != null) {
            ccontrols.getContainers().pop();
        }

        return ccontrols;
    }

    /**
     * Renders a single field (from a JSON schema) into an existing container control.
     * Used for adding individual fields dynamically.
     * 
     * @param fieldJson JSON object representing a single field
     * @param ccontrols The CControls manager object
     * @param strParentId The path ID of the parent control to add field to
     * @return The CControls object for chaining, or null if parsing failed
     */
    public CControls renderFieldIntoContainer(String fieldJson, CControls ccontrols, String strParentId) {
        if (fieldJson == null || fieldJson.trim().equals("")) {
            return null;
        }

        CHash field = CJSON.decode(fieldJson);
        if (field == null) {
            return null;
        }

        // Push the parent control onto the container stack so nested controls are added as children
        CControl parentControl = ccontrols.retrieve(strParentId);
        if (parentControl != null) {
            ccontrols.getContainers().push(parentControl);
        }

        // Render the single field into this container
        this.renderField(ccontrols, strParentId, field, 0);

        // Pop the parent control from the stack
        if (parentControl != null) {
            ccontrols.getContainers().pop();
        }

        return ccontrols;
    }
}
