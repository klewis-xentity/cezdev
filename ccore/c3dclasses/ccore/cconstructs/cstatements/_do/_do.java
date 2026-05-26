//-----------------------------------------------------------------------------------------------
// file: _do.java
// desc: defines an object that behaves like a do/while loop in an asynchronous environment
//-----------------------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------
// name: _do
// desc: defines an asynchonous do/while loop
//---------------------------------------------------
public class _do extends CFunction {
	public _do(){ this(null); }
	public _do(CHash params){}
	public void body(){}
	public boolean _while_cond(){ return false; }
	public CReturn call(Object object){
		this.body();
		while(this._while_cond())
			this.body();
		return null;
	} // end call()
} // end _while()