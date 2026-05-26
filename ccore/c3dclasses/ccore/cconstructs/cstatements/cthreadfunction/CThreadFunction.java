//---------------------------------------------------------------------------------
// file: CThreadFunction
// desc: 
//---------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------------------------------------
// name: CThreadFunction
// desc: 
//---------------------------------------------------------------------------------
public class CThreadFunction extends CFunction {
	protected CThread m_cthread;
	public CThreadFunction() {
		this.m_cthread = new CThread();
		this.m_cthread.create(this);
	} // end CThreadFunction()
	
	// functions to override
	public boolean cond() { return false; }
	public void body() {}
	
	// methods to call inside of the thread function
	public void _return(int iexitcode) { this._exit(iexitcode); }
	public void _return() { this._exit(1); }
	public void _exit(int iexitcode) { this.m_cthread.exit(iexitcode); }
	public void _break() { this._return(); }
	public void _sleep(int iintervalms) { this.m_cthread.sleep(iintervalms); }
	
	// methods to call when setting up thread function
	//public void _wait() { this.m_cthread.join(); }
	public void _wait() { while(this._running()){} }
	public void _sync() { this._wait(); }
	public void _start() { this.m_cthread.start(); }
	public boolean _running() { return this.m_cthread.alive(); }
} // end CThreadFunction