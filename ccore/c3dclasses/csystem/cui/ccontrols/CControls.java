//-------------------------------------------------------------------------------
// file: CControls
// desc: defines an object responsible for creating controls in the application 
//-------------------------------------------------------------------------------
package c3dclasses;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

//-----------------------------------------------------------------
// name: CControls
// desc: defines the form used in the application
//-----------------------------------------------------------------
public class CControls extends CHash {	
	protected CHash m_ccontrols = new CHash(); 		// stores all of the CControl objects
	protected CArray m_containers = new CArray();	// stores the current container ccontrol using push/pop
	
	public CControls() {}	
	public CHash getCControls(){ return this.m_ccontrols; }
	public CArray getContainers(){ return this.m_containers; }
	
	// container controls
	public boolean form(String strid, String value, CHash params) { return this.beginContainer("form", strid, value, params); }
	public boolean endform() { return this.endContainer("endform", null, null, null); }
	public boolean panel(String strid, String strlabel, CHash params) { return this.beginContainer("panel", strid, strlabel, params);}
	public boolean endpanel() { return this.endContainer("endpanel", null, null, null);}
	public boolean section(String strid, String strlabel, CHash params) { return this.panel(strid, strlabel, params);}
	public boolean endsection() { return this.endpanel(); }
	
	// menu controls
	public boolean menubar(String strid, String value, CHash params) { return this.beginContainer("menubar", strid, value, params); }
	public boolean endmenubar() { return this.endContainer("endmenubar", null, null, null); }
	public boolean menu(String strid, String value, CHash params) { return this.beginContainer("menu", strid, value, params); }
	public boolean endmenu() { return this.endContainer("endmenu", null, null, null); }
	public boolean menuitem(String strid, String value, CHash params) { return this.control("menuitem", strid, value, params); }
	public boolean menuitem_radio(String strid, String value, CHash params) { return this.control("menuitem-radio", strid, value, params); }
	public boolean menuitem_checkbox(String strid, String value, CHash params) { return this.control("menuitem-checkbox", strid, value, params); }
	
	public boolean menuitem_seperator() { return this.control("menuitem-seperator", null, null, null); }
	
	// system menu controls
	public boolean sysmenubar(String strid, String stricon, CHash params) { return this.beginContainer("systray-menubar", strid, stricon, params); }
	public boolean endsysmenubar() { return this.endContainer("systray-endmenubar", null, null, null); }
	public boolean sysmenu(String strid, String value, CHash params) { return this.beginContainer("systray-menu", strid, value, params); }
	public boolean endsysmenu() { return this.endContainer("systray-endmenu", null, null, null); }
	public boolean sysmenuitem(String strid, String value, CHash params) { return this.control("systray-menuitem", strid, value, params); }
	public boolean sysmenuitem_checkbox(String strid, String value, CHash params) { return this.control("systray-menuitem-checkbox", strid, value, params); }
	public boolean sysmenuitem_seperator() { return this.control("systray-menuitem-seperator", null, null, null); }
	
	// standard controls
	public boolean label(String strid, String value, CHash params) { return this.control("label", strid, value, params);}
	public boolean hidden(String strid, String value, CHash params) { return this.control("hidden", strid, value, params);}
	public boolean text(String strid, String value, CHash params) { return this.control("text", strid, value, params);}
	public boolean textarea(String strid, String value, CHash params) { return this.control("textarea", strid, value, params);}
	public boolean checkbox(String strid, String value, CHash params) { return this.control("checkbox", strid, value, params);}
	public boolean select(String strid, String value, CHash options, CHash params) { return this.choices("select", strid, value, options, params); }
	public boolean radio(String strgroupid, String strid, String value, CHash params) { 
		if(params == null)
			params = new CHash();
		params.set("m_strgroupid", strgroupid); 	
		return this.control("radio", strid, value, params);
	} // end radio()
	public boolean button(String strid, String value, CHash params) { return this.control("button", strid, value, params);} 
	public boolean submit(String strid, String value, CHash params) { return this.button(strid, value, params);} 
	public boolean dropDownPages(String strid, String value, CHash params) { return this.control("dropdown-pages", strid, value, params); }
	public boolean colorpicker(String strid, String value, CHash params) { return this.control("color", strid, value, params); }
	public boolean image(String strid, String value, CHash params) { return this.control("image", strid, value, params); }
	public boolean fileupload(String strid, String value, CHash params) { return this.control("fileupload", strid, value, params); }
	//public boolean runCommand(String strcommandpath, String strcommandargs) { 
	//	if(strcommandpath == null || strcommandpath == "")
	//		return false;
	//	__.exec_command(strcommandpath + " " + strcommandargs;
	//	return true;
	//}

	/*
	public boolean crud(String strid, String strtype, CHash params) { 
		this.clear();
		this.set("data-name", strid); 
		this.set("data-type", strtype); 
		this.set("data-action", "create");
		this.set("class", "ccontrol-crud");
		this.button("btn-" + strid + "-create", "create", null);
		this.set("data-action", "retrieve"); 
		this.button("btn-" + strid + "-retrieve", "retrieve", null);
		this.set("data-action", "update"); 
		this.button("btn-" + strid + "-update", "update", null); 
		this.set("data-action", "delete"); 
		this.button("btn-" + strid + "-delete", "delete", null); 	 
		this.clear();
		return true;
	} // end crud()	
	*/

	/////////////////////////
	// event handling
	/////////////////////////
	public boolean oninit(String strcommand) {
		this._("m_oninit_command", strcommand);
		return true;
	} // end oninit()

	public boolean ondeinit(String strcommand) {
		this._("m_ondeinit_command", strcommand);
		return true;
	} // end ondeinit()

	////////////////////////
	// helper functions
	////////////////////////
	public boolean choices(String strtype, String strid, String value, CHash options, CHash params) {
		if(params == null)
			params = new CHash();
		params.set("m_options", options); 
		return this.control(strtype, strid, value, params);
	} // end control_choices()
	public boolean beginContainer(String strtype, String strid, String value, CHash params) {
		CControl ccontrol = this.create(strtype, strid, value, params);
		if(ccontrol != null)
			m_containers.push(ccontrol);
		return ccontrol != null;
	} // end beginContainer()
	public boolean endContainer(String strtype, String strid, String value, CHash params) {
		m_containers.pop();
		return true;		
	} // end endContainer()
	public boolean control(String strtype, String strid, String value, CHash params) {
		return this.create(strtype, strid, value, params) != null;
	} // end control()
	
	////////////////////////
	// helper functions
	////////////////////////
	public CControl create(String strtype, String strid, String value, CHash params) {
		CHash container = (CHash) this.getContainers().top();
		String strpathid = (container != null) ? (((String)container._("m_strpathid")) + " " + strid) : strid;	
		CControl ccontrol = this.retrieve(strpathid);
		if(ccontrol != null)
			return ccontrol;
		ccontrol = new CControl();
		if(ccontrol != null) {
			ccontrol._("m_oninit_command",  this._("m_oninit_command"));
			ccontrol._("m_ondeinit_command",  this._("m_ondeinit_command"));
		}
		if(ccontrol == null || !ccontrol.create(this, strtype, strid, strpathid, value, params))
			return null;
		this.m_ccontrols._(strpathid, ccontrol);			
		this.clear();
		return ccontrol;
	} // end create()
	
	// strid = cform.cpanel.cbutton
	public CControl createEx(String strtype, String strid, String value, CHash params) {
		CArray namespace = __.split(".", strid);	
		if(namespace.length() < 2)
			return this.create(strtype, strid, value, params);
		String strparentid = namespace._(0, namespace.length()-2).join(".");	
		String strchildid = (String) namespace.last();
		CControl ccontrol = this.retrieve(strparentid);
		if(ccontrol == null)
			return null;
		this.m_containers.push(ccontrol);	
		return this.create(strtype, strid, value, params);
	} // end createEx()
	
	public CControl retrieve(String strpathid) {
		return (CControl) this.m_ccontrols._(strpathid);
	} // end retrieve()
	
	public String toStringContents() {
		return this.toStringContents(-1);
	} // end toStringContents()

	public String toStringContents(int length) {
		CArray keys = this.m_ccontrols.keys();
		if(keys == null)
			return "";
		int len = keys.length();
		String str = "\nContents of all the controls:\n";
		for(int i=0; i<len; i++) {
			CControl ccontrol = (CControl) this.m_ccontrols._((String)keys._(i));
			str += ccontrol.toStringContents(length);
			if(i != len-1)
				str += "\n\n";
		} // end for
		return str;
	} // end toString()

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

					CControl panelControl = CControls.this.retrieve(loaderPanelPathId);
					if (panelControl == null) {
						System.out.println("[ERROR] Panel control not found: " + loaderPanelPathId);
						return;
					}

					CControl contentPanelControl = CControls.this.retrieve(loaderContentPanelPathId);
					if (contentPanelControl == null) {
						System.out.println("[ERROR] Content panel not found: " + loaderContentPanelPathId);
						return;
					}

					forceVerticalPanelLayout(contentPanelControl);

					System.out.println("[DEBUG] Loading controls from: " + selectedPath);
					clearPanelControls(CControls.this, loaderContentPanelPathId);
					setPanelTitle(contentPanelControl, selectedLabel);

					CControls.this.getContainers().push(contentPanelControl);
					boolean loadedAny = CControls.this.loadCControlsFromPath(selectedPath);
					CControls.this.getContainers().pop();

					System.out.println("[DEBUG] Controls loaded: " + loadedAny + " into panel: " + loaderPanelPathId);

					panelControl.setProp("visible", "true");
					panelControl.setProp("pack", "true");

					CControl formControl = CControls.this.retrieve("main-form");
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
} // end class CControls