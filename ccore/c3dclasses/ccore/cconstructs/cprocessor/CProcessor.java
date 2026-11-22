//------------------------------------------------------------------------------------------------
// name: CProcessor
// desc: defines the driver interface and implementor to do crud operation on control objects
//------------------------------------------------------------------------------------------------
package c3dclasses;

//-----------------------------------------------------------------------------
// name: CProcessor
// desc: defines the interface object to do crud operation on control objects
//-----------------------------------------------------------------------------
public class CProcessor extends CHash {
	protected CHash m_cinstructions; // stores the instuction set object
	protected CHash m_cinterrupts; // stores the interrupt set object
	public CProcessor() { 
		this.m_cinstructions = new CHash(); 
		this.m_cinterrupts = new CHash();
	} 
	public CReturn execute(CObject operand) {
		if(operand == null) 
			return null;
		CFunction cinstructionhandlerfunction = (CFunction) this._(this.toInstruction(operand));
		if(cinstructionhandlerfunction == null)
			return null;
		CReturn creturn = cinstructionhandlerfunction.call(operand);			
		if(creturn == null)
			return null;
		return creturn;
	} // end execute() 
	public boolean addCInstructions(String strinstructionname, Object cinstructions) { 
		__.alert("Adding instruction: " + strinstructionname);
		this.m_cinstructions._(strinstructionname, cinstructions); return true; 
	}
	public Object getCInstructions(String strinstructionname) { return this.m_cinstructions._(strinstructionname); }
	public String toInstruction(CObject operand) { return ""; }

	public boolean addCInterrupt(String strinterruptname, Object cinterrupt) { this.m_cinterrupts._(strinterruptname, cinterrupt); return true; }
	public Object getCInterrupt(String strinterruptname) { return this.m_cinterrupts._(strinterruptname); }
	public String toInterrupt(CObject operand) { return ""; }
} // end CProcessor
