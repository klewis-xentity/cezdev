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
		super();
		this.addCInstructions("CControlInstructions", new CControlInstructions(this));
		this.addCInstructions("CFormInstructions", new CFormInstructions(this));
		this.addCInstructions("CButtonInstructions", new CButtonInstructions(this));
		this.addCInstructions("CLabelInstructions", new CLabelInstructions(this));
		this.addCInstructions("CToggleInstructions", new CToggleInstructions(this));
		this.addCInstructions("CMenuInstructions", new CMenuInstructions(this));
		this.addCInstructions("CSystemMenuInstructions", new CSystemMenuInstructions(this));
		this.addCInstructions("CRadioButtonInstructions", new CRadioButtonInstructions(this));
		this.addCInstructions("CCheckboxInstructions", new CCheckboxInstructions(this));
		this.addCInstructions("CComboBoxInstructions", new CComboBoxInstructions(this));
		this.addCInstructions("CColorInstructions", new CColorInstructions(this));
		this.addCInstructions("CFileUploadInstructions", new CFileUploadInstructions(this));
		this.addCInstructions("CImageUploadInstructions", new CImageUploadInstructions(this));
		this.addCInstructions("CTextInputInstructions", new CTextInputInstructions(this));
		this.addCInstructions("CTextAreaInstructions", new CTextAreaInstructions(this));
	}
	 
	public String toInstruction(CObject operand) {
		if(operand == null)
			return "";

		CControl control = (CControl) operand;
		String type = (String) control._("m_strdefaulttype");
		if(type == null)
			type = (String) control._("m_strtype");

		StringBuilder instruction = new StringBuilder(type == null ? "" : type);
		String action = (String) control._("m_straction");
		String property = (String) control._("m_strpropname");
		if(action != null)
			instruction.append("->").append(action);
		if(property != null)
			instruction.append("->").append(property);
		return instruction.toString().toLowerCase();
	}
	
	public static CProcessor m_processor = new CControlsDriver();
	public static CReturn call(CControl ccontrol) { return CControlsDriver.m_processor.execute(ccontrol); }
} // end CControlsDriver
