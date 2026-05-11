//---------------------------------------------------------
// file: CControl
// desc: defines a control object
//---------------------------------------------------------
package c3dclasses;

//-----------------------------------------------------------------
// name: CControl
// desc: defines a control object
//-----------------------------------------------------------------
public class CControl extends CObject implements AutoCloseable  {		
	///////////////////////////////
	// contructor
	public CControl() {
	} // end CControl()

	//////////////////////////////////////////
	// destructor
	@Override
    public void close() {
		//__.alert("destructing the object");
		//this._doOnDeInit();
	} // end close()

	@Override
    protected void finalize() throws Throwable {
		//__.alert("destructing the object");
		//this._doOnDeInit();
        super.finalize();
    } // end finalize()

	///////////////////
	// get/set prop
	public CControl setProp(String strpropname, Object propvalue) {	
		this._("m_straction", "set");
		this._("m_strpropname", strpropname);
		this._("m_propvalue", propvalue);
		CControlsDriver.call(this);
		return this;
	} // end setProp()
	
	public Object getProp(String strpropname) {	
		this._("m_straction", "get");
		this._("m_strpropname", strpropname);
		CControlsDriver.call(this);
		return this._("m_propvalue");
	} // end getProp()
	
	///////////////////
	// CRUD
	public boolean create(CControls ccontrols, String strtype, String strid, String strpathid, String value, CHash params) {
		this._("m_straction", "create");
		this._("m_ccontrols", ccontrols);
		this._("m_strtype", strtype);
		this._("m_strid", strid);
		this._("m_strpathid", strpathid);
		this._("m_value", value);
		this._("m_params", params);
		this._("m_attributes", ccontrols);
		this._("m_container", ccontrols.getContainers().top());
		this._("m_address", this);
	    this._doOnInit();
		return (CControlsDriver.call(this) != null);
	} // end create()
	
	public boolean delete() {
		this._doOnDeInit();
		this._("m_straction", "delete");
		CControls ccontrols = (CControls) this._("m_ccontrols");
		String strpathid = (String) this._("m_strpathid");
		if(CControlsDriver.call(this) == null) {
			return false;
		}
		ccontrols.getCControls().remove(strpathid);	
		return true;		
	} // end delete()
	
	/////////////
	// other
	public String toStringContents(int maxLength) {		
    	CArray keys = this.keys();
    	if (keys == null)
        	return "";

    	int len = keys.length();
    	StringBuilder str = new StringBuilder();

		for (int i = 0; i < len; i++) {
			String strpathid = (String) keys._(i);
			String value = String.valueOf(this._(strpathid));
			// Truncate if needed
			if (maxLength != -1 && value.length() > maxLength) {
				value = value.substring(0, maxLength) + "...";
			}

			str.append(strpathid).append(": ").append(value).append("\n");
		}
		return str.toString();
	} // end toStringContents()

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

	/////////////////////////
	// event handlers
	public void _doOnInit() {
		final String strcommand = (String) this._("m_oninit_command") + " " + (String) this._("m_strid");
		if(strcommand != null || strcommand.trim() != "")
			__.exec_command(strcommand); // Executes the string as a command
		return;
	} // end doOnInit()

	/////////////////////////
	// event handlers
	public void _doOnDeInit() {
		final String strcommand = (String) this._("m_ondeinit_command") + " " + (String) this._("m_strid");
		if(strcommand != null || strcommand.trim() != "")
			__.exec_command(strcommand); // Executes the string as a command
		return;
	} // end doOnDeInit()
} // end class CControl