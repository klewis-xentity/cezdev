//-------------------------------------------------------------------------------------------------------
// file: CVarUnitTest.java
// desc: Integration-style unit test coverage for CVar operations over CMemory.
//-------------------------------------------------------------------------------------------------------
package c3dclasses;
import org.junit.Test;

//-----------------------------------------------------------
// name: CVarUnitTest
// desc: Verifies create/read/update/delete and helper constructor behavior.
//-----------------------------------------------------------
public class CVarUnitTest extends CUnitTest {
	@Test
	public void test() {	
		// Set up multiple memory stores used by the test scenarios.
		String strpath = __.dir_path(this) + "/cmemory-test1.json";
		this.assertTrue(CMemory.include("cmemory-test1", strpath, "c3dclasses.CJSONMemoryDriver", null) != null);
		CMemory cmemory1 = CMemory.use("cmemory-test1");
		strpath = __.dir_path(this) + "/cmemory-test2.json";
		this.assertTrue(CMemory.include("cmemory-test2", strpath, "c3dclasses.CJSONMemoryDriver", null) != null);
		CMemory cmemory2 = CMemory.use("cmemory-test2");
		strpath = __.dir_path(this) + "/cmemory-test3.json";
		this.assertTrue(CMemory.include("cmemory-test3", strpath, "c3dclasses.CJSONMemoryDriver", null) != null);
		CMemory cmemory3 = CMemory.use("cmemory-test3");
		
		CVar cvar1 = CVar._use("cmemory-test1", "var1", "var1 value");
		CVar cvar2 = CVar._use("cmemory-test1", "var2", "var2 value");
		this.assertTrue(cvar1 != null);
		this.assertTrue(cvar1._string().equals("var1 value"));
		this.assertTrue(cvar1.toString().contains("var1 value"));
		cvar1.set(1000);
		this.assertTrue(cvar1._int() == 1000);
		this.assertTrue(cvar1.toString().contains("1000"));
		CVar._delete(cvar1);
		this.assertTrue(cmemory1.toString().contains("1000") == false);
		cvar2.set(100000);
		this.assertTrue(cmemory1.toString().contains("100000"));
		cvar2.set(__.carray(1,2,3,4,5,6,7,8,9,10));
		CVar cvar3 = CVar._use("cmemory-test1", "var1", "var1 value");

		CVar cvar4 = CVar._new("cmemory-test1", "var4", __.random(5,10));
		CVar cvar5 = CVar._get("cmemory-test1", "var4");
		this.assertTrue(cvar4._int() == cvar5._int());
		
		//CVar._delete(cvar3);
		
		cmemory1.close();
		cmemory2.close();
		cmemory3.close();
	} // end test()
} // end CVarUnitTest