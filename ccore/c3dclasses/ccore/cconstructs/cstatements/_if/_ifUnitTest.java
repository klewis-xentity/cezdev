//-----------------------------------------------------------------------------------------------
// file: _ifUnitTest.java
// desc: test the asynchonous while statement
//-----------------------------------------------------------------------------------------------
package c3dclasses;
import org.junit.Test;

//--------------------------------------------------------------------------------
// name: _ifUnitTest
// desc: 
//--------------------------------------------------------------------------------
public class _ifUnitTest extends CUnitTest {
	@Test
	public void test() {
		final CUnitTest __this = this;
		(new _if(__.chash("i", 2)){ public boolean cond(){ int i = this._int("i"); return i==2; } public void body(){
			__.println("_if()");
			int i = this._int("i");
			__this.assertTrue(i==2);
		}}).__(new _if_else(){ public boolean cond(){ int i = this._int("i"); return i==1; } public void body(){
			int i = this._int("i");
			__.println("_if_else() - " + i);
			__this.assertTrue(i==1);
		}}).__(new _else(){ public void body() { 
			__.println("_else()");
			int i = this._int("i");
			__this.assertTrue(i==0);
		}})._end_if(); // end _endif()
	} // end test()
} // end _doUnitTest()