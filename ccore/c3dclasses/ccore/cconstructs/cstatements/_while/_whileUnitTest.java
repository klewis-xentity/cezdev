//-----------------------------------------------------------------------------------------------
// file: _whileUnitTest.java
// desc: test the asynchonous while statement
//-----------------------------------------------------------------------------------------------
package c3dclasses;
import org.junit.Test;

//--------------------------------------------------------------------------------
// name: _whileUnitTest
// desc: 
//--------------------------------------------------------------------------------
public class _whileUnitTest extends CUnitTest {
	@Test
	public void test() {
		final CUnitTest __this = this;
		new _while(){ int i=0; public boolean cond(){ return i<10; } public void body(){
			__this.assertTrue(i>=0&&i<10);
			i++;
		}}; // end _while()
			
		new _while(){ int i=0; public boolean cond(){ return i<100; } public void body(){
			__this.assertTrue(i>=0&&i<100);
			i++;
		}}; // end _while()	
	} // end test()
} // end CStatementsUnitTest()