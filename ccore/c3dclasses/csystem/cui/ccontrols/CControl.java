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
		this._doOnDeInit();
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
	
	public Object retrieveProp(String strpropname) {	
		this._("m_straction", "get");
		this._("m_strpropname", strpropname);
		CControlsDriver.call(this);
		return this._("m_propvalue");
	} // end retrieveProp()
	
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

	// event handlers
	public void _doOnInit() {
		final String strcommand = (String) this._("m_oninit_command") + " " + (String) this._("m_strid");
		if(strcommand != null || strcommand.trim() != "")
			__.exec_command(strcommand); // Executes the string as a command
		return;
	} // end doOnInit()
	public void _doOnDeInit() {
		final String strcommand = (String) this._("m_ondeinit_command") + " " + (String) this._("m_strid");
		if(strcommand != null || strcommand.trim() != "")
			__.exec_command(strcommand); // Executes the string as a command
		return;
	} // end doOnDeInit()
} // end class CControl