//---------------------------------------------------------------------------------
// file: CBISCUnitTest
// desc: 
//---------------------------------------------------------------------------------
package c3dclasses;
import org.junit.Test;
import Jama.*; 

//---------------------------------------------------------------------------------
// name: CBISCUnitTest
// desc: 
//---------------------------------------------------------------------------------
public class CBISCUnitTest extends CUnitTest{
	@Test
	public void test() {
		String strpath = __.dir_path(this);
		String inputfile1 = strpath + "/input3.dat";
		CArray DB = CBISC.loadDB(inputfile1);		
		CArray items = CBISC.getItems(DB);
		CArray BD = CBISC.getBD(DB, items, items.length());
		CVector ds = CBISC.getDirectSupportsFromBD(BD, items.length());
		
		//CArray ds = __.carray_c((int)Math.pow(2,items.length()));
		//__.alert(items.length());
		//for(int i=0; i<ds.length(); i++)
		//	ds.set(i,new Integer(0));
		//__.alert(ds);
		//__.alert(ds2);

		CHash FIS = CBISC.BISC1(ds,null,items,items.length(),3);
		//__.alert(FIS);
	

		//CHash is = CBISC.it(15,items);
		//__.alert(is);
	} // end test()
} // end CBISCUnitTest