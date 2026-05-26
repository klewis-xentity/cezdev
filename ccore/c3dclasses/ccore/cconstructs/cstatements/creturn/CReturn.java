//-----------------------------------------------------------------------------------------------
// file: CReturn.java
// desc: defines a return object for use with aynchronous code 
//-----------------------------------------------------------------------------------------------
package c3dclasses;

//--------------------------------------------------------------------------------
// name: CReturn
// desc: defines a return object for use with aynchronous code
//--------------------------------------------------------------------------------
public class CReturn extends CCast {
	protected Object m_data = null;
	protected String m_strerror = "";
	protected int m_icode = CReturn.NULL;
	protected String m_strstatus = "";
		
	public void error(String strerror) { this.m_strerror = strerror; } 
	public String error() { return this.m_strerror; }
	
	public void code(int code) { this.m_icode = code; }
	public int code() { return this.m_icode; }
	
	public void status(String strstatus) { this.m_strstatus = strstatus; } 
	public String status() { return this.m_strstatus; }
	
	public void data(Object data) { this.m_data = data; } 
	public Object data() { return this.m_data; }
	
	public CCast set(Object data) { this.m_data = data; return this; } 
	public Object get() { return this.m_data; }
	
	public boolean isdone() { return this.m_icode == CReturn.DONE; }
	public boolean isbusy() { return this.m_icode == CReturn.BUSY; }
	public boolean isnull() { return this.m_icode == CReturn.NULL; }
	public boolean iserror() { return this.m_icode == CReturn.ERROR; } 
	
	public CReturn status(int code, Object data) { this.code(code); this.data(data); return this; }
	public CReturn done(Object data) { return this.status(CReturn.DONE, data); } 
	public CReturn busy() { return this.status(CReturn.BUSY, null); }
	
	// ClassMethods
	public static int NULL = 0;		// the function/method is not called yet
	public static int BUSY = 1;		// the function/method has been called and it's busy
	public static int DONE = 2;		// the function/method has been called and it's done	
	public static int ERROR = 3;	// the function/method has been called and it has an error
	
	public static CReturn _return(int code, Object data) {
		CReturn _return = new CReturn();
		if(_return == null)
			return null;
		_return.code(code);
		_return.data(data);
		return _return;
	} // end _return()

	public static CReturn _done(Object data) {
		return CReturn._return(CReturn.DONE, data);
	} // end _done()

	public static CReturn _busy(Object data) {
		return CReturn._return(CReturn.BUSY, data);
	} // end _busy()
	
	public static CReturn _error(Object data) {
		return CReturn._return(CReturn.ERROR, data);
	} // end _busy()
 	// end ClassMethods
} // end CReturn()