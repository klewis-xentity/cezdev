//-----------------------------------------------------------------------------------------------
// file: _switch_c.java
// desc: defines an object that behaves like a switch statement in an asynchronous environment
//-----------------------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------
// name: _switch_c
// desc: defines an asynchonous switch statement
//---------------------------------------------------
public class _switch_c extends _switch {
	public _switch_c(int value){ super(value, null); }
	public _switch_c(int value, CHash params){ super(value, params); }
	public CReturn call(Object object) {
		boolean cont = true;
		while(cont) {
			int len = this.m_cases.length();
			for(int i=0; i<len; i++) {
				_case c = (_case) this.m_cases._(i);
				if(c.getValue() == this.m_value) {
					c.body();
					continue;
				} // end else
			} // end for
			if(this.m_default != null) { // execute the default case statement
				this.m_default.body();
				continue;
			} // end if
		} // end while()
		return null;
	} // end call()
} // end _switch_c()