//-------------------------------------------------------
// name: CControlsSchema.java
// desc: Renders JSON UI schemas into CControls
//-------------------------------------------------------
package c3dclasses;

import java.io.File;

public class CControlsSchema {
    private String m_formId;
    private CHash m_fieldPaths = new CHash();

    public CControlsSchema() {
    }

    public CControls createCControls(String strJSONSchema) {
        return this.renderFromJson(strJSONSchema);
    }

    public CControl createCControl(String strJSONSchema) {
        CControls ccontrols = this.renderFromJson(strJSONSchema);
        if (ccontrols == null) {
            return null;
        }

        CArray keys = ccontrols.getCControls().keys();
        if (keys == null || keys.length() == 0) {
            return null;
        }
        return ccontrols.retrieve(String.valueOf(keys._(keys.length() - 1)));
    }

    public CControl updateCControl(CControl ccontrol, String strJSONSchema) {
        if (ccontrol == null || strJSONSchema == null || strJSONSchema.trim().equals("")) {
            return null;
        }

        CHash schema = CJSON.decode(strJSONSchema);
        if (schema == null) {
            return null;
        }

        Object value = schema._("value");
        if (value != null) {
            ccontrol.updateProp("text", value);
        }
        ccontrol._("m_schema", schema);
        return ccontrol;
    }

    public CHash retrieveCControlSchema(CControl ccontrol) {
        if (ccontrol == null) {
            return null;
        }
        Object schema = ccontrol._("m_schema");
        return schema instanceof CHash ? (CHash) schema : null;
    }

    public String getFormId() {
        return this.m_formId;
    }

    public String getFieldPath(String strFieldName) {
        if (strFieldName == null) {
            return null;
        }
        return (String) this.m_fieldPaths._string(strFieldName);
    }

    public CControl getField(CControls ccontrols, String strFieldName) {
        String pathId = this.getFieldPath(strFieldName);
        return pathId == null || ccontrols == null ? null : ccontrols.retrieve(pathId);
    }

    public CControls renderFromFile(String strUiSchemaPath) {
        String strJson = __.get_file_contents(strUiSchemaPath);
        return strJson == null ? null : this.renderFromJson(strJson);
    }

    public CControls renderFromJson(String strUiSchemaJson) {
        if (strUiSchemaJson == null || strUiSchemaJson.trim().equals("")) {
            return null;
        }
        CHash uiSchema = CJSON.decode(strUiSchemaJson);
        return uiSchema == null ? null : this.render(uiSchema, new CControls());
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

        String strFormId = this.getOrDefault(uiSchema._string("id"), "schema-form");
        String strFormTitle = this.getOrDefault(uiSchema._string("title"), "Schema Form");
        this.m_formId = strFormId;
        this.m_fieldPaths = new CHash();

        ccontrols.form(strFormId, strFormTitle, null);
        Object fieldsObj = uiSchema._("fields");
        if (fieldsObj instanceof CArray) {
            this.renderFields(ccontrols, strFormId, (CArray) fieldsObj);
        }
        ccontrols.endform();

        String strLayout = uiSchema._string("layout");
        if (strLayout == null || strLayout.equalsIgnoreCase("vertical")) {
            CControl form = ccontrols.retrieve(strFormId);
            if (form != null) {
                form.updateProp("grid", "true");
            }
        }
        return ccontrols;
    }

    private void renderFields(CControls ccontrols, String strParentId, CArray fields) {
        for (int i = 0; i < fields.length(); i++) {
            Object fieldObj = fields._(i);
            if (fieldObj instanceof CHash) {
                this.renderField(ccontrols, strParentId, (CHash) fieldObj, i);
            }
        }
    }

    private void renderField(CControls ccontrols, String strParentId, CHash field, int index) {
        String strName = this.getOrDefault(field._string("name"), "field" + index);
        String strLabel = this.getOrDefault(field._string("label"), strName);
        String strType = this.getOrDefault(field._string("type"), "").toLowerCase();
        String strComponent = this.getOrDefault(field._string("component"), "text").toLowerCase();

        if (strType.equals("panel") || strType.equals("section")) {
            ccontrols.panel(strName, strLabel, null);
            Object nestedFields = field._("fields");
            if (nestedFields instanceof CArray) {
                this.renderFields(ccontrols, strParentId + " " + strName, (CArray) nestedFields);
            }
            ccontrols.endpanel();
            return;
        }

        String strInputId = strName;
        if (strComponent.equals("checkbox")) {
            ccontrols.checkbox(strInputId, strLabel, null);
        } else if (strComponent.equals("button")) {
            ccontrols.button(strInputId, strLabel, null);
        } else {
            ccontrols.label(strName + "-label", strLabel, null);
            if (strComponent.equals("textarea")) {
                ccontrols.textarea(strInputId, "", null);
            } else if (strComponent.equals("select")) {
                ccontrols.select(strInputId, null, this.optionsToHash(field), null);
            } else {
                ccontrols.text(strInputId, "", null);
            }
        }

        String pathId = strParentId + " " + strInputId;
        this.m_fieldPaths._(strName, pathId);
        CControl ccontrol = ccontrols.retrieve(pathId);
        if (ccontrol != null) {
            ccontrol._("m_field_name", strName);
            ccontrol._("m_component", strComponent);
            ccontrol._("m_schema", field);
        }
    }

    private CHash optionsToHash(CHash field) {
        String path = field._string("optionFromDirectoryPath");
        if (path == null || path.trim().equals("")) {
            path = field._string("optionsFromDirectoryPath");
        }
        if (path != null && !path.trim().equals("")) {
            return this.optionsFromDirectory(path);
        }

        CHash optionsHash = __.chash();
        Object optionsObj = field._("options");
        if (optionsObj instanceof CArray) {
            CArray options = (CArray) optionsObj;
            for (int i = 0; i < options.length(); i++) {
                Object option = options._(i);
                if (option != null) {
                    String value = String.valueOf(option);
                    optionsHash.set(value, value);
                }
            }
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
        if (entries != null) {
            for (File entry : entries) {
                optionsHash.set(entry.getName(), entry.getAbsolutePath());
            }
        }
        return optionsHash;
    }

    private String resolveDirectoryPath(String strDirectoryPath) {
        String path = strDirectoryPath == null ? null : strDirectoryPath.trim();
        if (path == null) {
            return null;
        }
        if (path.equals("/cenvironments") || path.startsWith("/cenvironments/")) {
            String environments = System.getenv("CENVIRONMENTS");
            if (environments != null && !environments.trim().equals("")) {
                String suffix = path.substring("/cenvironments".length());
                if (suffix.startsWith("/")) {
                    suffix = suffix.substring(1);
                }
                return suffix.equals("") ? environments : environments + File.separator
                    + suffix.replace("/", File.separator);
            }
        }
        return path.replace("/", File.separator);
    }

    public CControls renderFieldsIntoContainer(String strUiSchemaJson, CControls ccontrols, String strParentId) {
        if (strUiSchemaJson == null || ccontrols == null) {
            return null;
        }
        CHash uiSchema = CJSON.decode(strUiSchemaJson);
        Object fieldsObj = uiSchema == null ? null : uiSchema._("fields");
        if (!(fieldsObj instanceof CArray)) {
            return null;
        }

        CControl parentControl = ccontrols.retrieve(strParentId);
        if (parentControl != null) {
            ccontrols.getContainers().push(parentControl);
        }
        this.renderFields(ccontrols, strParentId, (CArray) fieldsObj);
        if (parentControl != null) {
            ccontrols.getContainers().pop();
        }
        return ccontrols;
    }

    public CControls renderFieldIntoContainer(String fieldJson, CControls ccontrols, String strParentId) {
        if (fieldJson == null || ccontrols == null) {
            return null;
        }
        CHash field = CJSON.decode(fieldJson);
        if (field == null) {
            return null;
        }

        CControl parentControl = ccontrols.retrieve(strParentId);
        if (parentControl != null) {
            ccontrols.getContainers().push(parentControl);
        }
        this.renderField(ccontrols, strParentId, field, 0);
        if (parentControl != null) {
            ccontrols.getContainers().pop();
        }
        return ccontrols;
    }

    private String getOrDefault(String value, String fallback) {
        return value == null || value.trim().equals("") ? fallback : value;
    }
}
