//-----------------------------------------------------------------------------------------------
// file: _if.java
// desc: defines an object that behaves like a if statement in an asynchronous environment
//-----------------------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------
// name: _if
// desc: defines an asynchonous if loop
//---------------------------------------------------
public class _if extends CThreadFunction {
	protected CArray m_ifelse;
	protected _else m_else;
	public _if() { this(null); }
	public _if(CHash params) {
		super();
		this.append(params);
		this.m_ifelse = __.carray();
		this.m_else = null;
	} // end _if
	public boolean cond(){return true;}
	public void body(){}
	public _if __(_if_else ifelse) {
		ifelse.create(this.getHashMap());
		this.m_ifelse.push(ifelse);
		return this;
	} // _else_if
	public _if __(_else __else) {
		__else.create(this.getHashMap());
		this.m_else = __else;
		return this;
	} // end _else
	public void _end_if() {
		//CThread cthread = new CThread();
		//cthread.create(this);
		//cthread.start();
		this._start();
		return;
	} // endif
	public CReturn call(Object object) {
		boolean cont = true;
		while(cont) {
			if(this.cond()) {
				this.body();
				cont = false;
				continue;
			} // end if	
			int len = this.m_ifelse.length();
			for(int i=0; i<len; i++) {
				_if_else ifelse = (_if_else) this.m_ifelse._(i);
				if(ifelse.cond()) {
					ifelse.body();
					cont = false;
					continue;
				} // end else
			} // end for
			if(this.m_else != null) { // execute the else statement
				this.m_else.body();
				cont = false;
				continue;
			} // end if
		} // end while()
		return null;
	} // end call()
} // end _if()