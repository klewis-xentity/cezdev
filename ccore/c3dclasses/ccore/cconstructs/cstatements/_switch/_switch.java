//-----------------------------------------------------------------------------------------------
// file: _switch.java
// desc: defines an object that behaves like a switch statement in an asynchronous environment
//-----------------------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------
// name: _switch
// desc: defines an asynchonous switch statement
//---------------------------------------------------
public class _switch extends CFunction {
	protected CArray m_cases;
	protected _default m_default;
	protected int m_value;
	
	public _switch(int value){ this(value, null); }
	
	public _switch(int value, CHash params){ 
		this.append(params);
		this.m_cases = __.carray();
		this.m_default = null;
		this.m_value = value;
	} // end _switch()
	
	public void _end_switch() {
		CThread cthread = new CThread();
		cthread.create(this);
		cthread.start();
		return;
	} // _end_switch()
	
	public _switch __(_case c) {
		c.create(this.getHashMap());
		this.m_cases.push(c);
		return this;
	} // __()
	
	public _switch __(_default d) {
		d.create(this.getHashMap());
		this.m_default = d;
		return this;
	} // end __()
	
	public CReturn call(Object object) {
		boolean cont = true;
		while(cont) {
			int len = this.m_cases.length();
			for(int i=0; i<len; i++) {
				_case c = (_case) this.m_cases._(i);
				if(c.getValue() == this.m_value) {
					c.body();
					cont = false;
					continue;
				} // end else
			} // end for
			if(this.m_default != null) { // execute the default case statement
				this.m_default.body();
				cont = false;
				continue;
			} // end if
		} // end while()
		return null;
	} // end call()
} // end _switch()