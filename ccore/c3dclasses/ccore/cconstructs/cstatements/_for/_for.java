//-----------------------------------------------------------------------------------------------
// file: _for.java
// desc: defines an object that behaves like a for loop in an asynchronous environment
//-----------------------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------
// name: _for
// desc: defines an asynchonous while loop
//---------------------------------------------------
public class _for extends CFunction {
	public _for(){ this(null); }
	public _for(CHash params){
		this.append(params);
		CThread cthread = new CThread();
		cthread.create(this);
		cthread.start();
	} // end _while()
	public boolean cond(){return true;}
	public void body(){}
	public void inc(){}
	public CReturn call(Object object){
		while(this.cond()){
			this.body();
			this.inc();
		} // end while
		return null;
	} // end call()
} // end _for()