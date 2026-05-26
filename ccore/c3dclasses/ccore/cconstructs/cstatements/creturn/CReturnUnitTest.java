//-----------------------------------------------------------------------------------------------
// file: CReturn.java
// desc: defines constructs to use to simulate feature found in multithread programming lang 
//-----------------------------------------------------------------------------------------------
package c3dclasses;
import org.junit.Test;

//--------------------------------------------------------------------------------
// name: CReturnUnitTest
// desc: 
//--------------------------------------------------------------------------------
public class CReturnUnitTest extends CUnitTest {
	@Test
	public void test() {
		CReturn creturn = CReturnUnitTest.asyncfunc_done(80);
		this.assertTrue(creturn.isnull() == false);
		this.assertTrue(creturn.iserror() == false);
		this.assertTrue(creturn.isbusy() == false);	
		this.assertTrue(creturn.isdone() == true);	
		this.assertTrue(creturn._int() == 80);	
		
		creturn = CReturnUnitTest.asyncfunc_done("hello, world");
		this.assertTrue(creturn.isnull() == false);
		this.assertTrue(creturn.iserror() == false);
		this.assertTrue(creturn.isbusy() == false);	
		this.assertTrue(creturn.isdone() == true);	
		this.assertTrue(creturn._string() == "hello, world");	
		
		creturn = CReturnUnitTest.asyncfunc_done(8.90);
		this.assertTrue(creturn.isnull() == false);
		this.assertTrue(creturn.iserror() == false);
		this.assertTrue(creturn.isbusy() == false);	
		this.assertTrue(creturn.isdone() == true);	
		this.assertTrue(creturn._float() > 8.00);	
		
		creturn = CReturnUnitTest.asyncfunc_done(true);
		this.assertTrue(creturn.isnull() == false);
		this.assertTrue(creturn.iserror() == false);
		this.assertTrue(creturn.isbusy() == false);	
		this.assertTrue(creturn.isdone() == true);	
		this.assertTrue(creturn._boolean() == true);	
	
		creturn = CReturnUnitTest.asyncfunc_error(true);
		this.assertTrue(creturn.isnull() == false);
		this.assertTrue(creturn.iserror() == true);
		this.assertTrue(creturn.isbusy() == false);	
		this.assertTrue(creturn.isdone() == false);	
		this.assertTrue(creturn._boolean() == true);	
	
		creturn = CReturnUnitTest.asyncfunc_busy(true);
		this.assertTrue(creturn.isnull() == false);
		this.assertTrue(creturn.iserror() == false);
		this.assertTrue(creturn.isbusy() == true);	
		this.assertTrue(creturn.isdone() == false);	
		this.assertTrue(creturn._boolean() == true);	
	} // end test()
	
	public static CReturn asyncfunc_done(Object data) {
		return CReturn._done(data);
	} // end asyncfunc()
	
	public static CReturn asyncfunc_busy(Object data) {
		return CReturn._busy(data);
	} // end asyncfunc_busy()
	
	public static CReturn asyncfunc_error(Object data) {
		return CReturn._error(data);
	} // end asyncfunc_error()
} // end CReturnUnitTest()