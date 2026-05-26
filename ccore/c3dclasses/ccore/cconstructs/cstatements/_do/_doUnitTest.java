//-----------------------------------------------------------------------------------------------
// file: _doUnitTest.java
// desc: test the asynchonous while statement
//-----------------------------------------------------------------------------------------------
package c3dclasses;
import org.junit.Test;

//--------------------------------------------------------------------------------
// name: _doUnitTest
// desc: 
//--------------------------------------------------------------------------------
public class _doUnitTest extends CUnitTest {
	@Test
	public void test() {
		final CUnitTest __this = this;
		new _do(){ int i=0; public boolean _while_cond(){ return i<10; } public void body(){
			__this.assertTrue(i>=0&&i<10);
			i++;
		}}; // end _do()
		
		new _do(){ int i=0; public boolean _while_cond(){ return i<10; } public void body(){
			__this.assertTrue(i>=0&&i<10);
			i++;
		}}; // end _do()
	} // end test()
} // end _doUnitTest()