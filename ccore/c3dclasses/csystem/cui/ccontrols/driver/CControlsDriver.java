//------------------------------------------------------------------------------------------------
// name: CControlsDriver
// desc: defines the driver interface and implementor to do crud operation on control objects
//------------------------------------------------------------------------------------------------
package c3dclasses;

//-----------------------------------------------------------------------------
// name: CControlsDriver
// desc: defines the interface object to do crud operation on control objects
//-----------------------------------------------------------------------------
public class CControlsDriver extends CProcessor {	
	public CControlsDriver() {
		// setup ccontrols processor instruction set for each type of control
		super();
		this.addCInstructions("CControlInstructions", new CControlInstructions(this));
		this.addCInstructions("CFormInstructions", new CFormInstructions(this)); 
		this.addCInstructions("CButtonInstructions", new CButtonInstructions(this)); 
		//this.addCInstructions("CLabelInstructions", new CLabelInstructions(this)); 
		this.addCInstructions("CMenuInstructions", new CMenuInstructions(this)); 
		this.addCInstructions("CRadioButtonInstructions", new CRadioButtonInstructions(this)); 
		this.addCInstructions("CCheckboxInstructions", new CCheckboxInstructions(this));
		this.addCInstructions("CComboBoxInstructions", new CComboBoxInstructions(this));
		this.addCInstructions("CTextInputInstructions", new CTextInputInstructions(this));
		this.addCInstructions("CTextAreaInstructions", new CTextAreaInstructions(this));
	} // end CControlsDriver()
	 
	public String toInstruction(CObject operand) {
		CControl _this = (CControl) operand;
		if(_this == null)
			return "";
		String strtype = (String) _this._("m_strtype");
		String strdefaulttype = (String) _this._("m_strdefaulttype");
		String straction = (String) _this._("m_straction");
		String strpropname = (String) _this._("m_strpropname");
		String strinstruction = "";
		if(strdefaulttype != null)	// control
			strinstruction += strdefaulttype;	
		else if(strtype != null)
			strinstruction += strtype;
		if(straction != null)
			strinstruction += "->" + straction;
		if(strpropname != null)
			strinstruction += "->" + strpropname;		
		return strinstruction.toLowerCase();
	} // end toInstruction()
	
	// setup processor specific to this object (CControls)  
	static public CProcessor m_processor = new CControlsDriver();
	public static CReturn call(CControl ccontrol) { return CControlsDriver.m_processor.execute(ccontrol); }
} // end CControlsDriver
