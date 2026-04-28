//-----------------------------------------------------------------------------------------
// file: CVar.java
// desc: Wrapper around a driver-backed memory variable with typed access helpers.
//-----------------------------------------------------------------------------------------
package c3dclasses;

//----------------------------------------------------------------
// file: CVar
// desc: Represents one named value stored in CMemory.
//----------------------------------------------------------------
public class CVar extends CCast {	
	protected CMemory m_cmemory;
	protected String m_strtype;
	protected String m_strname;	
	protected Object m_value;
	
	public CVar(){ this.clear(); }
	
	public CReturn create(String strmemoryid, String strname, Object value){	
		CMemory cmemory = null;
		if((cmemory = CMemory.use(strmemoryid)) == null)
			return null;
		CReturn creturn = null;
		if((creturn = cmemory.create(strname, value, __.typeOf(value), null)) == null)
			return null;
		this.init(cmemory,(CHash)creturn.data());
		return creturn;	
	} // end create()
	
	public CReturn retrieve(String strmemoryid, String strname){	
		CMemory cmemory = null;
		if((cmemory = CMemory.use(strmemoryid)) == null)
			return null;
		CReturn creturn = null;
		if((creturn = cmemory.retrieve(strname)) == null)
			return null;
		this.init(cmemory,(CHash)creturn.data());
		return creturn;	
	} // end retrieve()
	
	public CReturn destroy(){
		if(this.m_cmemory == null) 
			return null; 
		CReturn creturn = this.m_cmemory.delete(this.m_strname); 
		this.clear(); 
		return creturn; 
	} // end destroy() 
	
	public Object get(){ 
		return (this.m_cmemory == null || this.m_strname.equals("") || 
			this.init(this.m_cmemory, this.m_cmemory.get(this.m_strname)) == false) ? null : this.m_value;
	} // end get()
	
	public CCast set(Object value){ 
		if(this.m_cmemory != null || !this.m_strname.equals(""))
			this.init(this.m_cmemory, (CHash)this.m_cmemory.update(this.m_strname, value, __.typeOf(value), null).data());
		return this;
	} // end set()
	
	public CReturn die() { return this.destroy(); }
	public void sync() { this.m_cmemory.sync(); }
	
	// Internal state helpers
	public boolean init(CMemory cmemory, CHash cvar){ 
		if(cvar == null)
			return false;
		this.m_cmemory = cmemory;
		this.m_strtype = cvar._string("m_strtype"); 
		this.m_strname = cvar._string("m_strname"); 
		this.m_value = cvar._("m_value"); 
		return true;
	} // end init()
	
	public void clear(){ 
		this.m_cmemory = null; 
		this.m_strtype = ""; 
		this.m_strname = "";
		this.m_value = null;
	} // end clear()
	
	public String toString() { return this.m_cmemory.get(this.m_strname).toString(); }
	
	public CMemory getCMemory() { return this.m_cmemory; }
	
	///////////////////
	// Static constructors/helpers
	public static CVar _use(String strmemoryid, String strname, Object value){
		CVar cvar = null;
		return ((cvar = CVar._new(strmemoryid, strname, value)) != null || (cvar = CVar._get(strmemoryid, strname)) != null) ? cvar : null;
	} // end _use()

	public static CVar _new(String strmemoryid, String strname, Object value){ 
		CVar cvar = null;
		return ((cvar = new CVar()) == null || cvar.create(strmemoryid, strname, value) == null) ? null : cvar; 
	} // end _new() 
	
	public static CVar _get(String strmemoryid, String strname){ 
		CVar cvar = null;
		return ((cvar = new CVar()) == null || cvar.retrieve(strmemoryid, strname) == null) ? null : cvar; 
	} // end _get()
	
	public static void _delete(CVar cvar){ 
		if(cvar == null) 
			return; 
		cvar.destroy(); 
		return; 
	} // end _delete()
} // end CVar