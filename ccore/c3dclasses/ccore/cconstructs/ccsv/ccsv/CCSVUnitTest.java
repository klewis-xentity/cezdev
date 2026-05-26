//---------------------------------------------------------------------------------
// file: CCSVUnitTest.java
// desc: 
//---------------------------------------------------------------------------------
package c3dclasses;
import org.junit.Test;

//---------------------------------------------------------------------------------
// name: CCSVUnitTest
// desc: 
//---------------------------------------------------------------------------------
public class CCSVUnitTest extends CUnitTest{
	@Test
	public void test() {
		String strpath = __.dir_path(this);
		CCSV ccsv = new CCSV();
		this.assertTrue(ccsv != null);
		this.assertTrue(ccsv.open(strpath + "/csvunittest.csv"));
		this.assertTrue(ccsv.getColumnHeading().toString().equals("[Start Date , Start Time, End Date, End Time, Event Title , All Day Event, No End Time, Event Description, Contact , Contact Email, Contact Phone, Location, Category, Mandatory, Registration, Maximum, Last Date To Register]"));
		this.assertTrue(ccsv.getRow(0).toString().equals("[9/5/2011, 3:00:00 PM, 9/5/2011, , Social Studies Dept. Meeting, N, Y, Department meeting, Chris Gallagher, cgallagher@schoolwires.com, 814-555-5179, High School, 2, N, N, 25, 9/2/2011]"));
		this.assertTrue(ccsv.getRowAsCHash(0).toString().equals("{\"Event Description\":\"Department meeting\",\"Category\":\"2\",\"All Day Event\":\"N\",\"Contact \":\"Chris Gallagher\",\"Contact Phone\":\"814-555-5179\",\"Last Date To Register\":\"9/2/2011\",\"Start Time\":\"3:00:00 PM\",\"Start Date \":\"9/5/2011\",\"Contact Email\":\"cgallagher@schoolwires.com\",\"Maximum\":\"25\",\"No End Time\":\"Y\",\"End Date\":\"9/5/2011\",\"Registration\":\"N\",\"Event Title \":\"Social Studies Dept. Meeting\",\"End Time\":\"\",\"Location\":\"High School\",\"Mandatory\":\"N\"}"));
		this.assertTrue(ccsv.save(strpath + "/csvunittest2.csv"));
		String csv1 = __.get_file_contents(strpath + "/csvunittest.csv").replace("\r\n", "\n").replace("\r", "\n").trim();
		String csv2 = __.get_file_contents(strpath + "/csvunittest2.csv").replace("\r\n", "\n").replace("\r", "\n").trim();
		this.assertTrue(csv1.equals(csv2));
		ccsv.setCellData(0,"Start Date ", "9/10/2020");
		this.assertTrue(ccsv.getRow(0).toString().equals("[9/10/2020, 3:00:00 PM, 9/5/2011, , Social Studies Dept. Meeting, N, Y, Department meeting, Chris Gallagher, cgallagher@schoolwires.com, 814-555-5179, High School, 2, N, N, 25, 9/2/2011]"));
		this.assertTrue(ccsv.save(strpath + "/csvunittest2.csv"));	
	} // end test()	
} // end CFunctionMain