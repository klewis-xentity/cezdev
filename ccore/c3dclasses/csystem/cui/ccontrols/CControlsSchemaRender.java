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
            for (int i = 0; i < fields.length(); i++) {
                Object fieldObj = fields._(i);
                if (!(fieldObj instanceof CHash)) {
                    continue;
                }
                this.renderField(ccontrols, strFormId, (CHash) fieldObj, i);
            }
        }

        ccontrols.endform();

        if (strLayout == null || strLayout.equalsIgnoreCase("vertical")) {
            ccontrols.retrieve(strFormId).setProp("grid", "true");
        }

        return ccontrols;
    }

    private void renderField(CControls ccontrols, String strFormId, CHash field, int index) {
        String strName = this.getOrDefault(field._string("name"), "field" + index);
        String strLabel = this.getOrDefault(field._string("label"), strName);
        String strComponent = this.getOrDefault(field._string("component"), "text").toLowerCase();

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
}
