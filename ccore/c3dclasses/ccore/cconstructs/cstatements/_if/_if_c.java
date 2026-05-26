//-----------------------------------------------------------------------------------------------
// file: _if_c.java
// desc: defines an object that behaves like a if statement in an asynchronous environment
//-----------------------------------------------------------------------------------------------
package c3dclasses;

//-------------------------------------------------------
// name: _if_c
// desc: defines an continuous asynchonous if statement
//-------------------------------------------------------
public class _if_c extends _if {
	public CReturn call(Object object) {
		boolean cont = true;
		while(cont) {
			if(this.cond()) {
				this.body();
			} // end if	
			int len = this.m_ifelse.length();
			for(int i=0; i<len; i++) {
				_if_else ifelse = (_if_else) this.m_ifelse._(i);
				if(ifelse.cond()) {
					ifelse.body();
				} // end else
			} // end for
			if(this.m_else != null) { // execute the else statement
				this.m_else.body();
			} // end if
		} // end while()
		return null;
	} // end call()
} // end _if_c()