//-----------------------------------------------------------------------------------------------
// file: _forUnitTest.java
// desc: test the asynchonous while statement
//-----------------------------------------------------------------------------------------------
package c3dclasses;
import org.junit.Test;

//--------------------------------------------------------------------------------
// name: _forUnitTest
// desc: 
//--------------------------------------------------------------------------------
public class _forUnitTest extends CUnitTest {
	@Test
	public void test() {
		final CUnitTest __this = this;
		new _for(){ int i=0; public boolean cond(){ return i<10; } public void inc(){ i++; } public void body(){
			__this.assertTrue(i>=0&&i<10);
		}}; // end _while()
			
		new _for(){ int i=0; public boolean cond(){ return i<100; } public void inc(){ i++; } public void body(){
			__this.assertTrue(i>=0&&i<100);
		}}; // end _while()	
	} // end test()
} // end _forUnitTest()