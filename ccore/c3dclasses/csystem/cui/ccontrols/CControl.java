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
	// contructor
	public CControl() {
	} // end CControl()

	// destructor
	@Override
    public void close() {
	} // end close()

	@Override
    protected void finalize() throws Throwable {
        super.finalize();
    } // end finalize()

	// CRUD
	public CControl updateProp(String strpropname, Object propvalue) {	
		this._("m_straction", "set");
		this._("m_strpropname", strpropname);
		this._("m_propvalue", propvalue);
		CControlsDriver.call(this);
		return this;
	} // end updateProp()

	public CControl setProp(String strpropname, Object propvalue) {
		return this.updateProp(strpropname, propvalue);
	} // end setProp()
	
	public Object retrieveProp(String strpropname) {	
		this._("m_straction", "get");
		this._("m_strpropname", strpropname);
		CControlsDriver.call(this);
		return this._("m_propvalue");
	} // end retrieveProp()

	public Object getProp(String strpropname) {
		return this.retrieveProp(strpropname);
	} // end getProp()

	public CControls addNewPanel(String strFieldsJson, String strid, String strlabel) {
		CControls ccontrols = (CControls) this._("m_ccontrols");
		if(ccontrols == null || this._("m_strpathid") == null)
			return null;

		ccontrols.getContainers().push(this);
		CControl panel = ccontrols.create("panel", strid, strlabel, null);
		if(panel == null) {
			ccontrols.getContainers().pop();
			return null;
		}
		ccontrols.getContainers().pop();
		CControlsSchema schema = new CControlsSchema();
		CControls result = schema.renderFieldsIntoContainer(strFieldsJson, ccontrols,
				(String) panel._("m_strpathid"));
		return result;
	} // end addNewPanel()
	
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
		return (CControlsDriver.call(this) != null);
	} // end create()
	
	public boolean delete() {
		this._("m_straction", "delete");
		CControls ccontrols = (CControls) this._("m_ccontrols");
		String strpathid = (String) this._("m_strpathid");
		if(CControlsDriver.call(this) == null) {
			return false;
		}
		ccontrols.getCControls().remove(strpathid);	
		return true;		
	} // end delete()
	
	// info functions
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

} // end class CControl