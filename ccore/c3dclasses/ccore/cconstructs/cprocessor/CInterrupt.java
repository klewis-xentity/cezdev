//--------------------------------------------------------------
// name: CInterrupt
// desc: implements the processor's instruction set
//--------------------------------------------------------------
package c3dclasses;

//--------------------------------------------------------
// name: CInterrupt
// desc: implements the processor's interrupt handling
//--------------------------------------------------------
public class CInterrupt {	
	protected CProcessor m_cprocessor; // the processor that handles the interrupt
	public CInterrupt(CProcessor cprocessor) { this.m_cprocessor = cprocessor; }
	public CProcessor getProcessor() { return this.m_cprocessor; }
} // end CInterrupt