//---------------------------------------------------------------------------------
// file: CThreadFunctionProgram
// desc: 
//---------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------------------------------------
// name: CThreadFunctionProgram
// desc: 
//---------------------------------------------------------------------------------
public class CThreadFunctionProgram {
	public static void main(String args[]) {
		CThreadFunction cthreadfunction = new CThreadFunction() {
			public CReturn call(Object object) {
				CThread cthread = (CThread) object;
				cthread.destroy();
				__.println("CThreadFunction - call() ended");
				return null;
			} // end call()
		}; // end CThreadFunction()
		cthreadfunction._start();	
		cthreadfunction._sync();
		__.println("CThreadFunction - main() ended");	
	} // end main()
} // end CThreadFunctionProgram