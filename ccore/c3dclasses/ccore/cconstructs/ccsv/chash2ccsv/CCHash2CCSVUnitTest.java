//---------------------------------------------------------------------------------
// file: CCHash2CCSVUnitTest.java
// desc: 
//---------------------------------------------------------------------------------
package c3dclasses;
import org.junit.Test;

//---------------------------------------------------------------------------------
// name: CCHash2CCSVUnitTest
// desc: 
//---------------------------------------------------------------------------------
public class CCHash2CCSVUnitTest extends CUnitTest{
	@Test
	public void test() {
		String strpath = __.dir_path(this);
		CCHash2CCSV chash2ccsv = new CCHash2CCSV();
		this.assertTrue(chash2ccsv != null);
		this.assertTrue(chash2ccsv.open(strpath + "/chash2ccsv-mapper.json", strpath + "/chash2ccsv.json") == true);
		CArray ccsvitems = chash2ccsv.map();
		this.assertTrue(ccsvitems != null);
		this.assertTrue(ccsvitems.length() == 10);
		this.assertTrue(__.save_csv_file(strpath + "/chash2ccsv.csv", ccsvitems));
	} // end test()
} // end CCHash2CCSVUnitTest