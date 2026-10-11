//-------------------------------------------------------
// name: CControlsSchema.java
// desc: Renders JSON UI schemas into CControls
//-------------------------------------------------------
package c3dclasses;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CControlsSchema {
    private String m_formId;
    private CHash m_fieldPaths = new CHash();

    public CControlsSchema() {
    }

    public CControls createCControls(String strJSONSchema) {
        return this.renderFromJson(strJSONSchema);
    }

    public CControls createCControlsFromPath(String strJSONSchemaPath) {
        System.out.println("[CControlsSchema] createCControlsFromPath called with: " + strJSONSchemaPath);
        if (strJSONSchemaPath == null || strJSONSchemaPath.trim().equals("")) {
            System.out.println("[CControlsSchema] null or empty path; returning null");
            return null;
        }

        File schemaFile = new File(strJSONSchemaPath.trim());
        if (schemaFile.isFile() || !schemaFile.exists() || !schemaFile.isDirectory()) {
            System.out.println("[CControlsSchema] path is not a directory: " + schemaFile.getAbsolutePath());
            return null;
        }

        String dirName = schemaFile.getName();
        if (dirName == null || dirName.trim().equals("")) {
            System.out.println("[CControlsSchema] directory name is empty");
            return null;
        }

        String dirId = this.text2id(dirName, 12);
        CControls ccontrols = new CControls();
        String formId = "form-" + dirId;
        String panelId = "panel-" + dirId;
        this.m_formId = formId;

        System.out.println("[CControlsSchema] dirName=" + dirName + ", formId=" + formId + ", panelId=" + panelId);
        ccontrols.form(formId, dirName, null);
        ccontrols.panel(panelId, dirName, null);
        this.renderDirectoryControlsIntoPath(schemaFile, ccontrols, formId + " " + panelId);

        ccontrols.endpanel();
        ccontrols.endform();
        System.out.println("[CControlsSchema] returning form controls: " + ccontrols.getCControls().keys().length());
        return ccontrols;
    }

    public CControls createCControlsFromPathIntoContainer(String strJSONSchemaPath, CControls ccontrols,
            String strParentPathId, String strPanelId, String strPanelLabel) {
        if (strJSONSchemaPath == null || strJSONSchemaPath.trim().equals("") || ccontrols == null
                || strParentPathId == null || strParentPathId.trim().equals("")) {
            return null;
        }

        File schemaDirectory = new File(strJSONSchemaPath.trim());
        if (!schemaDirectory.exists() || !schemaDirectory.isDirectory()) {
            return null;
        }

        CControl parentControl = ccontrols.retrieve(strParentPathId);
        if (parentControl == null) {
            return null;
        }

        String dirName = schemaDirectory.getName();
        String panelId = this.getOrDefault(strPanelId, "panel-" + this.text2id(strJSONSchemaPath, 10));
        String panelLabel = this.getOrDefault(strPanelLabel, dirName);
        String panelPath = strParentPathId + " " + panelId;

        ccontrols.getContainers().push(parentControl);
        ccontrols.panel(panelId, panelLabel, null);
        this.renderDirectoryControlsIntoPath(schemaDirectory, ccontrols, panelPath);
        ccontrols.endpanel();
        ccontrols.getContainers().pop();

        CControl panelControl = ccontrols.retrieve(panelPath);
        if (panelControl != null) {
            panelControl._("m_directory_path", schemaDirectory.getAbsolutePath());
            panelControl.setProp("visible", "true");
            panelControl.setProp("grid", "true");
        }
        return ccontrols;
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
        return this.optionsToHash(field, null);
    }

    private void renderDirectoryControlsIntoPath(File schemaDirectory, CControls ccontrols, String parentPath) {
        if (schemaDirectory == null || ccontrols == null || parentPath == null || parentPath.trim().equals("")) {
            return;
        }

        File[] files = schemaDirectory.listFiles(File::isFile);
        if (files == null) {
            System.out.println("[CControlsSchema] listFiles returned null for directory");
            return;
        }

        System.out.println("[CControlsSchema] scanning " + files.length + " file(s)");
        for (File file : files) {
            System.out.println("[CControlsSchema] file in directory: " + file.getAbsolutePath());
            String fileName = file.getName();
            String lower = fileName.toLowerCase();
            boolean isButton = lower.endsWith(".btn.json");
            boolean isDirectorySelect = lower.endsWith(".cbx.dir.json");
            boolean isSelect = lower.endsWith(".cbx.json") || isDirectorySelect;
            if (!isButton && !isSelect) {
                System.out.println("[CControlsSchema] skipping unsupported file type: " + fileName);
                continue;
            }

            String suffix = isButton ? ".btn.json" : ".cbx.json";
            if (isDirectorySelect) {
                suffix = ".cbx.dir.json";
            }

            String controlId = (isButton ? "btn-" : "select-") + this.text2id(fileName, 8);
            String json = __.get_file_contents(file.getAbsolutePath());
            CHash parsed = json == null || json.trim().equals("") ? null : CJSON.decode(json);
            String label = fileName;
            String handler = null;
            String selected = null;
            CHash options = __.chash();

            if (parsed != null) {
                String customId = parsed._string("id");
                if (customId != null && !customId.trim().equals("")) {
                    controlId = customId.trim();
                }
                label = this.getOrDefault(parsed._string("label"), fileName.substring(0, fileName.length() - suffix.length()));
                handler = this.getOrDefault(parsed._string("handler"), parsed._string("onclick"));
                selected = this.getOrDefault(parsed._string("selected"), parsed._string("value"));
                options = this.optionsToHash(parsed, file.getParentFile());
            } else {
                label = fileName.substring(0, fileName.length() - suffix.length());
            }

            handler = this.resolveHandlerPath(handler, file.getParentFile());

            if (isButton) {
                System.out.println("[CControlsSchema] creating button: file=" + fileName + ", label=" + label + ", handler=" + handler);
                ccontrols.button(controlId, label, null);
                String buttonPath = parentPath + " " + controlId;
                CControl buttonControl = ccontrols.retrieve(buttonPath);
                System.out.println("[CControlsSchema] buttonPath=" + buttonPath + ", found=" + (buttonControl != null));
                if (buttonControl != null) {
                    buttonControl._("m_schema", parsed != null ? parsed : __.chash());
                    buttonControl.setProp("visible", "true");
                    buttonControl.setProp("text", label);
                    if (handler != null && !handler.trim().equals("")) {
                        buttonControl.setProp("onclick", handler);
                    }
                }
            } else {
                System.out.println("[CControlsSchema] creating select: file=" + fileName + ", label=" + label + ", handler=" + handler + ", options=" + options.keys().length());
                ccontrols.select(controlId, null, options, null);
                String selectPath = parentPath + " " + controlId;
                CControl selectControl = ccontrols.retrieve(selectPath);
                System.out.println("[CControlsSchema] selectPath=" + selectPath + ", found=" + (selectControl != null));
                if (selectControl != null) {
                    selectControl._("m_schema", parsed != null ? parsed : __.chash());
                    selectControl._("m_component", "select");
                    selectControl._("m_options_from_directory", isDirectorySelect ? "true" : "false");
                    selectControl.setProp("visible", "true");
                    if (selected != null && !selected.trim().equals("")) {
                        selectControl.setProp("selected", selected);
                    }
                    if (handler != null && !handler.trim().equals("")) {
                        selectControl.setProp("onchange", handler);
                    }
                }
            }
        }
    }

    private CHash optionsToHash(CHash field, File baseDirectory) {
        if (field == null) {
            return __.chash();
        }

        String path = field._string("optionFromDirectoryPath");
        if (path == null || path.trim().equals("")) {
            path = field._string("optionsFromDirectoryPath");
        }
        if (path == null || path.trim().equals("")) {
            path = field._string("optionsFromDirectory");
        }
        if (path != null && !path.trim().equals("")) {
            return this.optionsFromDirectory(path, baseDirectory);
        }

        CHash optionsHash = __.chash();
        Object optionsObj = field._("options");
        if (optionsObj instanceof CHash) {
            CHash options = (CHash) optionsObj;
            CArray keys = options.keys();
            for (int i = 0; i < keys.length(); i++) {
                String key = String.valueOf(keys._(i));
                Object value = options._(key);
                optionsHash.set(key, value == null ? key : String.valueOf(value));
            }
            return optionsHash;
        }
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

    private CHash optionsFromDirectory(String strDirectoryPath, File baseDirectory) {
        CHash optionsHash = __.chash();
        String resolvedPath = this.resolveDirectoryPath(strDirectoryPath, baseDirectory);
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

    private String resolveDirectoryPath(String strDirectoryPath, File baseDirectory) {
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

        String normalized = path.replace("/", File.separator);
        File asFile = new File(normalized);
        if (!asFile.isAbsolute() && baseDirectory != null) {
            return new File(baseDirectory, normalized).getAbsolutePath();
        }
        return normalized;
    }

    private String resolveHandlerPath(String strHandler, File baseDirectory) {
        if (strHandler == null) {
            return null;
        }

        String handler = strHandler.trim();
        if (handler.equals("")) {
            return null;
        }

        // Resolve local script references relative to the .btn.json file directory.
        if (handler.startsWith("./") || handler.startsWith(".\\") || handler.startsWith("../") || handler.startsWith("..\\")) {
            if (baseDirectory != null) {
                File resolved = new File(baseDirectory, handler);
                return resolved.getAbsolutePath();
            }
        }

        File asFile = new File(handler);
        if (!asFile.isAbsolute() && baseDirectory != null) {
            File resolved = new File(baseDirectory, handler);
            if (resolved.exists()) {
                return resolved.getAbsolutePath();
            }
        }

        return handler;
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

    private String text2id(String strText, int length) {
        if (strText == null) {
            strText = "";
        }
        String normalized = strText.trim();
        if (normalized.equals("")) {
            normalized = "0";
        }

        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            BigInteger value = new BigInteger(1, hash);
            StringBuilder result = new StringBuilder();

            while (value.signum() > 0) {
                BigInteger[] divide = value.divideAndRemainder(BigInteger.valueOf(alphabet.length()));
                result.append(alphabet.charAt(divide[1].intValue()));
                value = divide[0];
            }

            String encoded = result.reverse().toString();
            if (encoded.length() < length) {
                StringBuilder padded = new StringBuilder();
                for (int i = encoded.length(); i < length; i++) {
                    padded.append('0');
                }
                padded.append(encoded);
                encoded = padded.toString();
            }
            return encoded.substring(0, Math.min(length, encoded.length())).toUpperCase();
        } catch (Exception ex) {
            StringBuilder fallback = new StringBuilder();
            for (int i = 0; i < length; i++) {
                fallback.append('0');
            }
            return fallback.toString();
        }
    }

    private String getOrDefault(String value, String fallback) {
        return value == null || value.trim().equals("") ? fallback : value;
    }
}
