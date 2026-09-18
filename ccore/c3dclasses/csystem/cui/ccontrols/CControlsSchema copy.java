//-------------------------------------------------------
// name: CControlsSchema.java
// desc: Convenience wrapper around the schema renderer for CControls
//-------------------------------------------------------
package c3dclasses;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class CControlsSchema extends CControls {
    private final CControlsSchemaRender m_renderer;

    public CControlsSchema() {
        this.m_renderer = new CControlsSchemaRender();
    }

    public CControlsSchema(CControlsSchemaRender renderer) {
        this.m_renderer = (renderer != null) ? renderer : new CControlsSchemaRender();
    }

    public String getFormId() {
        return this.m_renderer.getFormId();
    }

    public String getFieldPath(String strFieldName) {
        return this.m_renderer.getFieldPath(strFieldName);
    }

    public CControl getField(CControls ccontrols, String strFieldName) {
        return this.m_renderer.getField(ccontrols, strFieldName);
    }

    public CControls renderFromFile(String strUiSchemaPath) {
        return this.m_renderer.renderFromFile(strUiSchemaPath);
    }

    public CControls renderFromJson(String strUiSchemaJson) {
        return this.m_renderer.renderFromJson(strUiSchemaJson);
    }

    public CControls render(CHash uiSchema, CControls ccontrols) {
        return this.m_renderer.render(uiSchema, ccontrols);
    }

    public CControls renderFieldsIntoContainer(String strUiSchemaJson, CControls ccontrols, String strParentId) {
        return this.m_renderer.renderFieldsIntoContainer(strUiSchemaJson, ccontrols, strParentId);
    }

    public CControls renderFieldIntoContainer(String fieldJson, CControls ccontrols, String strParentId) {
        return this.m_renderer.renderFieldIntoContainer(fieldJson, ccontrols, strParentId);
    }

    /////////////////////////
    // file loading helpers
    /////////////////////////
    private static final Pattern CCONTROL_SUBEXT_JSON_PATTERN = Pattern.compile("(?i)^.+\\.[a-z0-9_-]+\\.json$");

    public boolean loadCControlsFromPath(String path) {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("[ERROR] loadCControlsFromPath: invalid path: " + path);
            return false;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return true;
        }

        boolean loadedAny = false;
        for (File file : files) {
            if (file.isFile() && isCControlConfigFile(file.getName())) {
                boolean loaded = loadCControlFromFile(file);
                loadedAny = loadedAny || loaded;
            }
        }

        return loadedAny;
    }

    public boolean loadCControlFromFile(String filename) {
        return loadCControlFromFile(new File(filename));
    }

    public boolean loadCControlFromFile(File file) {
        try {
            CControl parentContainer = (CControl) this.getContainers().top();
            if (parentContainer == null) {
                System.out.println("[ERROR] No active parent container for: " + file.getAbsolutePath());
                return false;
            }

            String parentPathId = (String) parentContainer.get("m_strpathid");
            String schemaJson = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            CControlsSchemaRender renderer = new CControlsSchemaRender();
            CControls result = renderer.renderFieldsIntoContainer(schemaJson, this, parentPathId);

            if (result != null) {
                System.out.println("[LOADED] " + file.getAbsolutePath());
                return true;
            }

            boolean loadedMeta = loadControlFromMetadata(parentPathId, file, schemaJson);
            if (!loadedMeta) {
                System.out.println("[ERROR] Failed to render controls from: " + file.getAbsolutePath());
            }
            return loadedMeta;
        } catch (Exception ex) {
            System.out.println("[ERROR] Failed loading control file: " + file.getAbsolutePath());
            System.out.println("[ERROR] " + ex.getMessage());
            return false;
        }
    }

    private boolean isCControlConfigFile(String fileName) {
        return CCONTROL_SUBEXT_JSON_PATTERN.matcher(fileName).matches();
    }

    private boolean loadControlFromMetadata(String parentPathId, File file, String jsonContents) {
        CHash config = CJSON.decode(jsonContents);
        if (config == null) {
            return false;
        }

        String fileName = file.getName();
        String controlType = controlTypeFromFileName(fileName);
        String controlId = toControlId(fileName);
        controlId = ensureUniqueControlId(parentPathId, controlId, controlType);
        String label = stringValue(config.get("label"), controlId);
        String handler = stringValue(config.get("handler"), null);
        String optionsFromFolder = stringValue(config.get("optionsFromFolder"), null);
        if (optionsFromFolder == null || optionsFromFolder.trim().isEmpty()) {
            optionsFromFolder = stringValue(config.get("optionsFromFolders"), null);
        }
        Object optionsObject = config.get("options");

        boolean created = false;
        String selectLoaderPanelId = null;
        if ("button".equals(controlType)) {
            created = this.button(controlId, label, null);
        } else if ("select-loader".equals(controlType)) {
            CHash options = resolveSelectOptions(file, optionsObject, optionsFromFolder);
            String defaultValue = firstOptionValue(options);
            String loaderPanelId = controlId + "-panel";
            String loaderContentPanelId = loaderPanelId + "-content";
            selectLoaderPanelId = loaderPanelId;
            String loaderPanelPathId = parentPathId + " " + loaderPanelId;
            String loaderContentPanelPathId = loaderPanelPathId + " " + loaderContentPanelId;

            this.panel(loaderPanelId, label, null);
            this.label(controlId + "-label", label, null);
            created = this.select(controlId, defaultValue, options, null);
            this.panel(loaderContentPanelId, label + "-selected", null);
            this.endpanel();
            this.endpanel();

            CControl loaderPanelControl = this.retrieve(loaderPanelPathId);
            if (loaderPanelControl != null) {
                loaderPanelControl.setProp("grid", "true");
                forceVerticalPanelLayout(loaderPanelControl);
            }
            CControl loaderContentPanelControl = this.retrieve(loaderContentPanelPathId);
            if (loaderContentPanelControl != null) {
                loaderContentPanelControl.setProp("grid", "true");
                forceVerticalPanelLayout(loaderContentPanelControl);
            }

            if (created) {
                attachLoaderBehavior(parentPathId, controlId, loaderPanelId, loaderContentPanelId);
            }
        } else if ("select".equals(controlType)) {
            CHash options = resolveSelectOptions(file, optionsObject, optionsFromFolder);
            String defaultValue = firstOptionValue(options);
            this.label(controlId + "-label", label, null);
            created = this.select(controlId, defaultValue, options, null);
        } else if ("text".equals(controlType)) {
            created = this.text(controlId, label, null);
        } else {
            created = this.label(controlId, label, null);
        }

        if (!created) {
            return false;
        }

        String controlPathForProps = parentPathId + " " + controlId;
        if ("select-loader".equals(controlType) && selectLoaderPanelId != null) {
            controlPathForProps = parentPathId + " " + selectLoaderPanelId + " " + controlId;
        }

        CControl control = this.retrieve(controlPathForProps);
        if (control != null) {
            if (handler != null && !handler.trim().isEmpty()) {
                String handlerPath = resolveRelativePath(file, handler);
                if ("select".equals(controlType) || "select-loader".equals(controlType)) {
                    control.setProp("onchange", handlerPath);
                } else {
                    control.setProp("onclick", handlerPath);
                }
            }
            if (optionsFromFolder != null && !optionsFromFolder.trim().isEmpty()) {
                control.setProp("optionsFromFolder", resolveRelativePath(file, optionsFromFolder));
            }
        }

        System.out.println("[LOADED-META] " + file.getAbsolutePath() + " -> " + controlType);
        return true;
    }

    private String controlTypeFromFileName(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".btn.json")) {
            return "button";
        }
        if (lower.endsWith(".cbx.loader.json")) {
            return "select-loader";
        }
        if (lower.endsWith(".cbx.json")) {
            return "select";
        }
        if (lower.endsWith(".tf.json")) {
            return "text";
        }
        return "label";
    }

    private String toControlId(String fileName) {
        String lower = fileName.toLowerCase();
        String name = fileName;
        if (lower.endsWith(".cbx.loader.json")) {
            name = fileName.substring(0, fileName.length() - ".cbx.loader.json".length());
        } else if (lower.endsWith(".btn.json")) {
            name = fileName.substring(0, fileName.length() - ".btn.json".length());
        } else if (lower.endsWith(".cbx.json")) {
            name = fileName.substring(0, fileName.length() - ".cbx.json".length());
        } else if (lower.endsWith(".tf.json")) {
            name = fileName.substring(0, fileName.length() - ".tf.json".length());
        } else if (lower.endsWith(".json")) {
            name = fileName.substring(0, fileName.length() - 5);
        }
        return name.replaceAll("[^a-zA-Z0-9_-]", "-");
    }

    private String ensureUniqueControlId(String parentPathId, String baseId, String controlType) {
        if (baseId == null || baseId.trim().isEmpty()) {
            return baseId;
        }

        String pathId = parentPathId + " " + baseId;
        if (this.retrieve(pathId) == null) {
            return baseId;
        }

        String suffix = "-" + controlType.replaceAll("[^a-zA-Z0-9_-]", "-");
        String candidate = baseId + suffix;
        int index = 2;
        while (this.retrieve(parentPathId + " " + candidate) != null) {
            candidate = baseId + suffix + "-" + index;
            index++;
        }

        System.out.println("[WARN] Duplicate control id detected: " + baseId + ", remapped to: " + candidate);
        return candidate;
    }

    private String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String str = value.toString().trim();
        return str.isEmpty() ? fallback : str;
    }

    private CHash resolveSelectOptions(File configFile, Object optionsObject, String optionsFromFolders) {
        if (optionsObject instanceof CHash) {
            return (CHash) optionsObject;
        }
        return buildSelectOptions(configFile, optionsFromFolders);
    }

    private CHash buildSelectOptions(File configFile, String optionsFromFolders) {
        CHash options = new CHash();
        if (optionsFromFolders == null || optionsFromFolders.trim().isEmpty()) {
            return options;
        }

        File folder = new File(resolveRelativePath(configFile, optionsFromFolders));
        File[] children = folder.listFiles();
        if (children == null) {
            return options;
        }

        for (File child : children) {
            if (child.isDirectory()) {
                String name = child.getName();
                options.set(name, child.getAbsolutePath());
            }
        }

        return options;
    }

    private String firstOptionValue(CHash options) {
        if (options == null || options.keys() == null || options.keys().length() == 0) {
            return "";
        }
        Object key = options.keys().get(0);
        Object value = options.get(key);
        return value == null ? "" : value.toString();
    }

    private String resolveRelativePath(File configFile, String value) {
        File candidate = new File(value);
        if (candidate.isAbsolute()) {
            return candidate.getAbsolutePath();
        }
        return new File(configFile.getParentFile(), value).getAbsolutePath();
    }

    private void attachLoaderBehavior(String parentPathId, String controlId, String loaderPanelId, String loaderContentPanelId) {
        try {
            final String loaderPanelPathId = parentPathId + " " + loaderPanelId;
            final String loaderContentPanelPathId = loaderPanelPathId + " " + loaderContentPanelId;
            final String controlPathId = loaderPanelPathId + " " + controlId;
            final CControl loaderControl = this.retrieve(controlPathId);
            if (loaderControl == null) {
                return;
            }

            Object jcontrol = loaderControl.get("m_jcontrol");
            if (!(jcontrol instanceof javax.swing.JComboBox)) {
                return;
            }

            @SuppressWarnings("unchecked")
            final javax.swing.JComboBox<String> combo = (javax.swing.JComboBox<String>) jcontrol;

            java.awt.event.ActionListener loadControls = new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Object selectedObj = combo.getSelectedItem();
                    if (selectedObj == null) {
                        return;
                    }

                    String selectedLabel = selectedObj.toString();
                    CHash options = (CHash) loaderControl.get("m_options");
                    String selectedPath = selectedLabel;
                    if (options != null && options.get(selectedLabel) != null) {
                        selectedPath = options.get(selectedLabel).toString();
                    }

                    CControl panelControl = CControlsSchema.this.retrieve(loaderPanelPathId);
                    if (panelControl == null) {
                        System.out.println("[ERROR] Panel control not found: " + loaderPanelPathId);
                        return;
                    }

                    CControl contentPanelControl = CControlsSchema.this.retrieve(loaderContentPanelPathId);
                    if (contentPanelControl == null) {
                        System.out.println("[ERROR] Content panel not found: " + loaderContentPanelPathId);
                        return;
                    }

                    forceVerticalPanelLayout(contentPanelControl);

                    System.out.println("[DEBUG] Loading controls from: " + selectedPath);
                    clearPanelControls(CControlsSchema.this, loaderContentPanelPathId);
                    setPanelTitle(contentPanelControl, selectedLabel);

                    CControlsSchema.this.getContainers().push(contentPanelControl);
                    boolean loadedAny = CControlsSchema.this.loadCControlsFromPath(selectedPath);
                    CControlsSchema.this.getContainers().pop();

                    System.out.println("[DEBUG] Controls loaded: " + loadedAny + " into panel: " + loaderPanelPathId);

                    panelControl.setProp("visible", "true");
                    panelControl.setProp("pack", "true");

                    CControl formControl = CControlsSchema.this.retrieve("main-form");
                    if (formControl != null) {
                        formControl.setProp("pack", "true");
                    }
                }
            };

            combo.addActionListener(loadControls);
            loadControls.actionPerformed(null);
        } catch (Exception ex) {
            System.out.println("[ERROR] Failed to attach loader behavior: " + ex.getMessage());
        }
    }

    private void clearPanelControls(CControls ccontrols, String panelPathId) {
        CArray keys = ccontrols.getCControls().keys();
        CArray keysToDelete = new CArray();

        for (int i = 0; i < keys.length(); i++) {
            Object keyObj = keys.get(i);
            if (keyObj == null) {
                continue;
            }
            String key = keyObj.toString();
            if (key.startsWith(panelPathId + " ")) {
                keysToDelete.push(key);
            }
        }

        for (int i = 0; i < keysToDelete.length(); i++) {
            for (int j = i + 1; j < keysToDelete.length(); j++) {
                String a = String.valueOf(keysToDelete.get(i));
                String b = String.valueOf(keysToDelete.get(j));
                if (b.length() > a.length()) {
                    keysToDelete.set(i, b);
                    keysToDelete.set(j, a);
                }
            }
        }

        for (int i = 0; i < keysToDelete.length(); i++) {
            String key = String.valueOf(keysToDelete.get(i));
            CControl child = ccontrols.retrieve(key);
            if (child != null) {
                child.delete();
            }
            ccontrols.getCControls().remove(key);
        }

        CControl panelControl = ccontrols.retrieve(panelPathId);
        if (panelControl != null) {
            Object jcontrol = panelControl.get("m_jcontrol");
            if (jcontrol instanceof java.awt.Container) {
                java.awt.Container container = (java.awt.Container) jcontrol;
                container.removeAll();
                container.revalidate();
                container.repaint();
            }
        }
    }

    private void forceVerticalPanelLayout(CControl panelControl) {
        if (panelControl == null) {
            return;
        }
        Object jcontrol = panelControl.get("m_jcontrol");
        if (jcontrol instanceof java.awt.Container) {
            java.awt.Container container = (java.awt.Container) jcontrol;
            container.setLayout(new java.awt.GridLayout(0, 1));
            container.revalidate();
            container.repaint();
        }
    }

    private void setPanelTitle(CControl panelControl, String title) {
        if (panelControl == null) {
            return;
        }
        Object jcontrol = panelControl.get("m_jcontrol");
        if (jcontrol instanceof javax.swing.JPanel) {
            javax.swing.JPanel panel = (javax.swing.JPanel) jcontrol;
            String text = (title == null) ? "" : title;
            panel.setBorder(javax.swing.BorderFactory.createTitledBorder(text));
            panel.revalidate();
            panel.repaint();
        }
    }

    
	///////////////////////////////////////////////////////////////////////////////////
	// Schema/Dynamic Field Addition
	///////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Add fields from a UI schema JSON dynamically to this control container.
	 * This allows adding form fields programmatically to an existing form/panel/container
	 * that is already running/visible.
	 * 
	 * Example usage:
	 *   String newFieldsJson = "{\"fields\":[{\"name\":\"newField\",\"label\":\"New Field\",\"component\":\"text\"}]}";
	 *   CControls updatedControls = myControl.addUISchemaJson(newFieldsJson);
	 * 
	 * @param strUiSchemaJson JSON string containing a "fields" array to add to this control
	 * @return The CControls manager object, allowing further manipulation or null on error
	 */
	public CControls addUISchemaJson(String strUiSchemaJson) {
		CControls ccontrols = (CControls) this._("m_ccontrols");
		String strpathid = (String) this._("m_strpathid");
		
		if (ccontrols == null || strpathid == null) {
			return null;
		}
		
		CControlsSchemaRender renderer = new CControlsSchemaRender();
		return renderer.renderFieldsIntoContainer(strUiSchemaJson, ccontrols, strpathid);
	} // end addUISchemaJson()
	
	/**
	 * Add a single field from a field definition JSON to this control container.
	 * 
	 * Example usage:
	 *   String fieldJson = "{\"name\":\"newField\",\"label\":\"New Field\",\"component\":\"text\"}";
	 *   CControls updatedControls = myControl.addUISchemaField(fieldJson);
	 * 
	 * @param strFieldJson JSON string containing a single field definition
	 * @return The CControls manager object, allowing further manipulation or null on error
	 */
	public CControls addUISchemaField(String strFieldJson) {
		CControls ccontrols = (CControls) this._("m_ccontrols");
		String strpathid = (String) this._("m_strpathid");
		
		if (ccontrols == null || strpathid == null) {
			return null;
		}
		
		CControlsSchemaRender renderer = new CControlsSchemaRender();
		return renderer.renderFieldIntoContainer(strFieldJson, ccontrols, strpathid);
	} // end addUISchemaField()

	/**
	 * Create a new panel as a child of this control and populate it with fields from a schema.
	 * Useful for dynamically adding grouped fields to a running form.
	 * 
	 * Example usage:
	 *   String fieldsJson = "{\"fields\":[{\"name\":\"field1\",\"label\":\"Field 1\",\"component\":\"text\"}]}";
	 *   CControls updated = formControl.addNewPanel(fieldsJson, "panel1", "Panel Label");
	 * 
	 * @param strFieldsJson JSON string containing a "fields" array to populate the new panel
	 * @param strPanelId The ID for the new panel
	 * @param strPanelLabel The label for the new panel
	 * @return The CControls manager object, allowing further manipulation or null on error
	 */
	public CControls addNewPanel(String strFieldsJson, String strPanelId, String strPanelLabel) {
		CControls ccontrols = (CControls) this._("m_ccontrols");
		String strpathid = (String) this._("m_strpathid");
		
		if (ccontrols == null || strpathid == null) {
			return null;
		}
		
		// Create the panel under this control
		ccontrols.getContainers().push(this);
		ccontrols.panel(strPanelId, strPanelLabel, null);
		ccontrols.getContainers().pop();
		
		// Build the path ID for the new panel
		String panelPathId = strpathid + " " + strPanelId;
		CControl panelControl = ccontrols.retrieve(panelPathId);
		
		// Add fields to the new panel if provided
		if (panelControl != null && strFieldsJson != null && !strFieldsJson.trim().equals("")) {
			CControlsSchemaRender renderer = new CControlsSchemaRender();
			renderer.renderFieldsIntoContainer(strFieldsJson, ccontrols, panelPathId);
		}
		
		return ccontrols;
	} // end addNewPanel()


}
