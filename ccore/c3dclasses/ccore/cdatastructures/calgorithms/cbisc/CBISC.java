//-----------------------------------------------------------------------------------------
// file: CBISC
// desc: defines Binary Itemset Support Counting (BISC)
//-----------------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------------
// class CBISC
// desc: defines Binary Itemset Support Counting (BISC)
//---------------------------------------------------------
public class CBISC {
	//---------------------------------------------------------------------
	// name: loadDB()
	// desc: loads the transaction database into memory
	//---------------------------------------------------------------------
	static public CArray loadDB(String strfilename) {
		CArray lines = __.get_lines_from_file(strfilename); 
		for(int i=0; i<lines.length(); i++) {
			String line = lines._string(i);
			CArray strnumbers = __.split(" ", line);
			CArray numbers = __.carray();
			for(int j=0; j<strnumbers.length(); j++)
				numbers.push(Integer.parseInt(strnumbers._string(j)));			
				lines._(i,__.carray(i,numbers));
		} // end for
		return lines;
	} // end loadDB()
	
	//------------------------------------------------------------
	// name: getItems(CArray D)
	// desc: returns the items that are contained in the database
	//------------------------------------------------------------
	static public CArray getItems(CArray D) {
		CHash items = __.chash();
		for(int i=0; i<D.length(); i++) {
			CArray itemset = D._carray(i)._carray(1);
			for(int j=0; j<itemset.length(); j++)
				items._(itemset._int(j),true);
		} // end for
		return items.keys();
	} // end getItems()
	
	//---------------------------------------------------------------------
	// name: getBD()
	// desc: returns the Binary Database (BD) of a the given Database (D)
	//---------------------------------------------------------------------
	static public CArray getBD(CArray D, CArray items, int s) {
		CArray BD = __.carray();
		int len = items.length();
		for(int i=0; i<D.length(); i++) {
			CArray T = D._carray(i);
			CArray itemset = T._carray(1);
			CBitArray B = __.cbitarray(s);
			len = itemset.length();
			for(int k=0; k<s&&k<len; k++)
				B.enableBit(itemset._int(k)-1,true);
			BD.push(__.carray(T._(0), B));
		} // end for
		return BD;
	} // end getBD()

	static public CVector getDirectSupportsFromBD(CArray BD, int size) {
		int len = BD.length();
		CVector ds = new CVector((int)Math.pow(2,size));
		for(int i=0; i<len; i++) {
			CBitArray itemset = (CBitArray) BD._carray(i)._(1);
			int item =itemset.getBitArrays()[0]; 	
			//__.alert(item);
			ds.i(item, (int)ds.i(item)+1);
		} // end for
		return ds;
	} // end getDirectSupportsFromBD()
	
	static public CHash it(int bitemset, CArray items) {
		int len = items.length();
		CHash itemset = __.chash();
		for(int i=0; i<len; i++) {
			int item = 1 << i;
			//__.alert(bitemset&item);
			if((bitemset&item) > 0) {
				itemset.set(items._(i)+"", items._(i));	
			}
		} // end for
		//__.alert(itemset);
		return itemset;
	} // end it()

	//---------------------------------------------------------------------
	// name: BISC1()
	// desc: 
	//---------------------------------------------------------------------
	static public CHash BISC1(CVector ds, CVector prefix, CArray items, int s, int minsup) {
		CHash FIS = __.chash();
		for(int i=1; i<=s; i++) {
			int _2_s_i = (int)Math.pow(2,s-i);
			for(int j=0; j<_2_s_i; j++) {
				int _2_i = (int)Math.pow(2,i);
				int _2_i_1 = (int)Math.pow(2,i-1);
				int j_2_i = j*_2_i;
				int len = j_2_i + _2_i_1;
				for(int k=j_2_i; k<len; k++)
					ds.i(k, ds.i(k)+ds.i(k|_2_i_1));
			} // end for
		} // end for
		int len = ds.length(); 
		for(int i=0; i<len; i++) 
			if(ds.i(i) >= minsup)  {
				CHash itemset = it(i,items);
				String key = itemset.keys().join(",");
				if(FIS._(key) == null)
					FIS._(key, ds.i(i));
				//else FIS._(key, FIS._int(key)+1);
			} // end if
		return FIS;
	} // end BISC1()
} // end CBISC