//---------------------------------------------------------------------------
// name: CUnitTest
// desc: the unit test object
//---------------------------------------------------------------------------
package c3dclasses;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
//import static org.mockito.Mockito.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


//--------------------------------------------------------
// name: CUnitTest
// desc: the unit test object
//--------------------------------------------------------
public class CUnitTest {
	public void assertTrue(boolean condition) {
		assertEquals(true, condition); 
	} // end assertTrue()
} // end CUnitTest