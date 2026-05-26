//-----------------------------------------------------------------------------------------------
// file: _while.java
// desc: defines an object that behaves like a while loop in an asynchronous environment
//-----------------------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------
// name: _while
// desc: defines an asynchonous while loop
//---------------------------------------------------
public class _while extends CThreadFunction {
	public _while() { this(null); }
	public _while(CHash params) { super(); this._start(); }
	public boolean cond(){ return false; }
	public void body() {}
	public CReturn call(Object object) {
		while(this.cond())
			this.body();
		return null;
	} // end call()
} // end _while()