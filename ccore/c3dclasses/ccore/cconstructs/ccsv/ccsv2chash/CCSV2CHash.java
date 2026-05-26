//---------------------------------------------------------------------------------
// file: CCSV2CHash.java
// desc: converts a CArray of CSV data to a CHash object
//---------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------------------------------------
// name: CCSV2CHash
// desc: converts a CArray of CSV data to a CHash object
//---------------------------------------------------------------------------------
class CCSV2CHash {
	protected final String m_metaprefix = "_m_";
	protected CHash m_chashmap;
	protected CCSV 	m_csv;
	
	public CCSV2CHash() {
		this.m_chashmap = null;
		this.m_csv = null;
	} // end CCSV2CHash()
	
	public boolean open(String injsonmapperfile, String incsvfile) {
		return (((this.m_chashmap = __.json_file_2_chash(injsonmapperfile)) != null) && 
				((this.m_csv = new CCSV()) != null) &&
				(this.m_csv.open(incsvfile)));			
	} // end open()
	
	public String toString() { 
		return this.m_chashmap.toString() + "\n\n" + this.m_csv.toString(); 
	} // end toString()
		
	public CHash map() {
		CArray items = new CUniqueArray();
		int nrows = m_csv.getRowLength();
		for(int row=0; row<nrows; row++) {
			__.println("begin ccsv2chash.map(): csvrow - (" + (row+1) + "/" + nrows + ")"); 
			CHash csvrow = this.m_csv.getRowAsCHash(row);
			CUniqueArray uitems = (CUniqueArray) items;
			CHash item = uitems.pushCHash(this.getKeyField(this.m_chashmap, csvrow));
			this.mapCHash(this.m_chashmap, csvrow, item);
			__.println("end ccsv2chash.map(): csvrow - (" + (row+1) + "/" + nrows + ")"); 
		} // end for
		return __.chash("items", items);
	} // end map()
		
	public CHash mapCHash(CHash chashmap, CHash csvrow, CHash item) {
		if(item == null)
			return null;
		CArray fields = chashmap.keys();
		int len = fields.length(); 
		for(int i=0; i<len; i++) {
			String field = fields._string(i);
			if (field.indexOf(this.m_metaprefix) != -1) // skip meta fields
				continue;
			this.mapField(field, chashmap._chash(field), csvrow, item);
		} // end for
		return item;
	} // end mapCHash()

	public CArray mapCArray(CArray carraymapper, CHash csvrow, CArray items) {
		CHash chashmap = carraymapper._chash(0);
		String csvfield = chashmap._string(this.m_metaprefix + "csv_field");
		Object type = chashmap._(this.m_metaprefix + "type");
		CUniqueArray uitems = (CUniqueArray) items;
		boolean bdistinct = chashmap._boolean(this.m_metaprefix + "distinct");
		if(type instanceof CArray) { // map an array
			items.push(this.mapCArray((CArray)type, csvrow, new CUniqueArray()));
		} // end if
		else if(type instanceof CHash) {	// map an object
			CHash item = uitems.pushCHash(this.getKeyField((CHash)type, csvrow));
			this.mapCHash((CHash)type, csvrow, item);
		} // end else
		else {	// map the type/value in the csvrow to the hash object
			String strtype = (String) type;
			if(strtype.indexOf("num") != -1)
				//uitems.pushNumber(csvrow._double(csvfield), bdistinct);
				uitems.pushNumber(evalNumericExpression(csvrow,csvfield), bdistinct);
			else if(strtype.indexOf("str") != -1) 
				uitems.pushString(evalExpression(csvrow,csvfield), bdistinct);
			else uitems.push(null);	
		} // end else
		return items;
	} // end mapCArray()

	public CHash mapField(String field, CHash chashmap, CHash csvrow, CHash item) {
		String csvfield = chashmap._string(this.m_metaprefix + "csv_field");
		Object type = chashmap._(this.m_metaprefix + "type");
		if(type instanceof CArray) { // map an array
			if (item._(field) == null)
				item._(field, new CUniqueArray());
			item._(field, this.mapCArray((CArray)type, csvrow, item._carray(field)));
		} // end if
		else if(type instanceof CHash) {	// map an object
			 if (item._(field) == null)
				item._(field, __.chash());
			item._(field, this.mapCHash((CHash)type, csvrow, item._chash(field)));
		} // end else
		else {	// map the type/value in the csvrow to the hash object
			String strtype = (String) type;
			if(strtype.indexOf("num") != -1)
				//item._(field, csvrow._double(csvfield));
				item._(field, evalNumericExpression(csvrow, csvfield));
			else if(strtype.indexOf("str") != -1) 
				item._(field, evalExpression(csvrow, csvfield));
			else item._(field, null);	
		} // end else
		return item;
	} // end mapField()
	
	public String getKeyField(CHash chashmap, CHash csvrow) {
		String keyfield = chashmap._string(this.m_metaprefix + "key_field");
		if(keyfield == "" || chashmap._chash(keyfield) == null)
			return "";
		String csvfield = chashmap._chash(keyfield)._string(this.m_metaprefix + "csv_field");
		String csvvalue = evalExpression(csvrow, csvfield);//csvrow._string(csvfield);	
		return csvvalue;
	} // end getKeyField()
	
	public String evalExpression(CHash csvrow, String csvexpression) {
		CArray csvfields = csvrow.keys();
		int len = csvfields.length();
		for(int i=0; i<len; i++) {
			String csvfield = csvfields._string(i);
			if(csvexpression.indexOf("["+csvfield+"]") != -1)
				csvexpression = csvexpression.replace("["+csvfield+"]", csvrow._string(csvfield));
			else if(csvexpression.indexOf(csvfield) != -1)
				csvexpression = csvexpression.replace(csvfield, csvrow._string(csvfield));
		} // end for
		String value = __.eval(csvexpression);
		return (value.indexOf("Exception") != -1) ? csvexpression : value;
	} // end evalExpression()		
	
	public Double evalNumericExpression(CHash csvrow, String csvexpression) { return Double.parseDouble(evalExpression(csvrow, csvexpression)); }
} // end CCSV2CHash

//----------------------------------------------------------------------------
// name: CUniqueArray
// desc: builds an array to store unique chashes, arrays, nums or strings
//----------------------------------------------------------------------------
class CUniqueArray extends CArray {
	protected CHash m_chash;
	
	public CUniqueArray() {
		super();
		this.m_chash = __.chash();
	} // end CUniqueArray()
	
	public void pushNumber(double number, boolean bunique) {
		if(bunique == false) {
			this.push(number);
			return;
		} // end if
		
		String key = "" + number;
		if(!key.isEmpty() && this.m_chash._(key) == null) {
			this.push(number);
			this.m_chash._(key, number);
		} // end if
		return;
	} // end pushNumber()
	
	public void pushString(String string, boolean bunique) {
		if(bunique == false) {
			this.push(string);
			return;
		} // end if
		
		String key = "" + string;
		if(!key.isEmpty() && this.m_chash._(key) == null) {
			this.push(string);
			this.m_chash._(key, string);
		} // end if
		return;
	} // end pushString()
	
	public CHash pushCHash(String key) {
		if(key == null || key == "") {
			this.push(__.chash());
			return (CHash) this.last();
		} // end if
		
		if(this.m_chash._(key) == null) {
			this.m_chash._(key, __.chash());
			this.push(this.m_chash._(key));
		} // end if
		return this.m_chash._chash(key);
	} // end pushCHashItem()
} // end CUniqueArray