//-------------------------------------------------------------------------------------------------------
// file: CMemoryTest.java
// desc: 
//-------------------------------------------------------------------------------------------------------
package c3dclasses;
import org.junit.Test;

//-----------------------------------------------------------
// name: CMemoryTest
// desc:
//-----------------------------------------------------------
public class CMemoryUnitTest extends CUnitTest {
	@Test
	public void test() {	
		String strpath = __.dir_path(this) + "/cmemory-test.json";
			
		// include / open
		this.assertTrue(CMemory.include("cmemory-test", strpath, "c3dclasses.CJSONMemoryDriver", null) != null);
		CMemory cmemory = CMemory.use("cmemory-test");
		this.assertTrue(CMemory.include("cmemory-test", strpath, "c3dclasses.CJSONMemoryDriver", null) != null);
		CMemory cmemory2 = CMemory.use("cmemory-test");
		this.assertTrue(CMemory.include("cmemory-test3", strpath, "c3dclasses.CJSONMemoryDriver", null) != null);
		CMemory cmemory3 = CMemory.use("cmemory-test3");
		this.assertTrue(cmemory != null);
		this.assertTrue(cmemory == cmemory2);
		this.assertTrue(cmemory != cmemory3);
		
		// create
		CReturn creturn = cmemory.create("kevin", "is here", "string", null);
		this.assertTrue(creturn != null);
		CHash cvar = (CHash) creturn._chash();
		this.assertTrue(cvar != null);
		this.assertTrue(cvar._string("m_strname").equals("kevin"));
		this.assertTrue(cvar._string("m_value").equals("is here"));
		
		creturn = cmemory.create("", "is here", "string", null);
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn.data();
		this.assertTrue(cvar == null);		
		
		creturn = cmemory.create("joe", null, "NA", null);
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn.data();
		this.assertTrue(cvar != null);
		this.assertTrue(cvar._string("m_strname").equals("joe"));
		this.assertTrue(cvar._string("m_value") == null);
		this.assertTrue(cvar._string("m_strtype").equals("NA"));
		
		// retrieve
		creturn = cmemory.retrieve("joe");
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn.data();
		this.assertTrue(cvar != null);
		this.assertTrue(cvar._string("m_strname").equals("joe"));
		this.assertTrue(cvar._string("m_value") == null);
		this.assertTrue(cvar._string("m_strtype").equals("NA"));
		
		creturn = cmemory.retrieve("kevin");
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn.data();
		this.assertTrue(cvar != null);
		this.assertTrue(cvar._string("m_strname").equals("kevin"));
		this.assertTrue(cvar._string("m_value").equals("is here"));
		this.assertTrue(cvar._string("m_strtype").equals("NA") == false);
	
		// update
		creturn = cmemory.update("joe", "is updating his data", "string", null);
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn.data();
		this.assertTrue(cvar != null);
		this.assertTrue(cvar._string("m_strname").equals("joe"));
		this.assertTrue(cvar._string("m_value") != null);
		this.assertTrue(cvar._string("m_value").equals("is updating his data"));
		this.assertTrue(cvar._string("m_strtype").equals("string"));
		
		creturn = cmemory.update("", "is updating his data", "string", null);
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn.data();
		this.assertTrue(cvar == null);
		
		// delete
		creturn = cmemory.delete("");
		this.assertTrue(creturn != null);
		this.assertTrue(creturn._boolean() == false);
			
		creturn = cmemory.delete("joe");
		this.assertTrue(creturn._boolean());
		creturn = cmemory.retrieve("joe");
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn.data();
		this.assertTrue(cvar == null);
		
		// upsert
		creturn = cmemory.upsert("charlie", "charlie and the chocolate factory", "string", null);
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn.data();
		this.assertTrue(cvar != null);
		this.assertTrue(cvar._string("m_strname").equals("charlie"));
		this.assertTrue(cvar._string("m_value").equals("charlie and the chocolate factory"));

		// sync
		creturn = cmemory.sync();
		//__.println(cmemory.toString());
		String strcontents = __.get_file_contents(strpath);
		this.assertTrue(cmemory.cache().toJSON(true).equals(strcontents));
				
		// get		
		cvar = cmemory.get("charlie");
		this.assertTrue(cvar != null);
		this.assertTrue(cvar._string("m_strname").equals("charlie"));
		this.assertTrue(cvar._string("m_value").equals("charlie and the chocolate factory"));
		
		// set		
		cmemory.set("charlie", "charlie and the chocolate man", null);
		cvar = cmemory.get("charlie");
		this.assertTrue(cvar != null);
		this.assertTrue(cvar._("m_value").equals("charlie and the chocolate man"));
		creturn = cmemory.retrieve("charlie");
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn.data();
		this.assertTrue(cvar != null);
		this.assertTrue(cvar._string("m_strname").equals("charlie"));
		this.assertTrue(cvar._string("m_value").equals("charlie and the chocolate man"));
		
		// close
		cmemory.close();
		creturn = cmemory.upsert("charlie", "charlie and the chocolate factory", "string", null);
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn.data();
		this.assertTrue(cvar == null);		
		
		// global driver
		strpath = __.dir_path(this) + "/json_driver.json";
		this.assertTrue(CMemory.include("cmemory-test10", strpath, "json_driver", null) != null);
		cmemory3 = CMemory.use("cmemory-test10");
		// create
		creturn = cmemory3.create("kevin", "is here", "string", null);
		this.assertTrue(creturn != null);
		cvar = (CHash) creturn._chash();
		this.assertTrue(cvar != null);
		this.assertTrue(cvar._string("m_strname").equals("kevin"));
		this.assertTrue(cvar._string("m_value").equals("is here"));
	} // end test()
} // end CHookUnitTest