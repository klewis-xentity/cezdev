//---------------------------------------------------------------------------------
// file: CCHash2CCSV.java
// desc: converts a CHash object into a CCSV object
//---------------------------------------------------------------------------------
package c3dclasses;

//---------------------------------------------------------------------------------
// name: CCHash2CCSV
// desc: converts a CHash object into a CCSV object
//---------------------------------------------------------------------------------
class CCHash2CCSV {
	protected String m_metaprefix = "_m_";	// default meta prefix
	protected String m_jointoken = "##";	// default array join token
	protected CHash m_chashmap;
	protected CHash m_chash;
	
	public CCHash2CCSV() {
		this.m_chashmap = null;
		this.m_chash = null;
	} // end CCHash2CCSV()
	
	public boolean open(String injsonmapperfile, String injsonfile) {
		return (((this.m_chashmap = __.json_file_2_chash(injsonmapperfile)) != null) && 
				(this.m_chash = __.json_file_2_chash(injsonfile)) != null);
	} // end open()
	
	public CHash getCHashMap() { return this.m_chashmap; }
	public CHash getHeader() { return this.m_chashmap._chash(this.m_metaprefix + "csv_header"); }
	public CHash getCHash() { return this.m_chash; }
	public CArray getCHashItems() { return this.m_chash._carray("items"); }
	
	public String toString() { 
		return this.m_chashmap.toString() + "\n\n" + this.m_chash.toString(); 
	} // end toString()
		
	public CArray map() {
		CArray chashitems = this.getCHashItems();
		int len = chashitems.length();
		CArray csvitems = __.carray();
		for(int i=0; i<len; i++) {
			__.println("begin: cchash2ccsv.map(): hashitem - (" + (i+1) + "/" + len + ")");
			csvitems.push(this.mapCHash(chashitems._chash(i), this.m_chashmap));
			__.println("end: cchash2ccsv.map(): hashitem - (" + (i+1) + "/" + len + ")");
		} // end for
		return this.convertToCCSV(csvitems);		
	} // end map()
	
	public CArray mapCHash(CHash chashitem, CHash chashmap) {
		CArray ccsvitems_related = __.carray();
		CArray ccsvitems_unrelated = __.carray();
		CArray fields = chashitem.keys();
		int len = fields.length();
		for(int i=0; i<len; i++) {
			String field = fields._string(i); 
			Object obj = chashitem._(field);
			CHash  map = chashmap._chash(field);
			if(map == null)	// no mapping for this field
				continue;
			String csvfield = map._string(this.m_metaprefix + "csv_field");
			Object type = map._(this.m_metaprefix + "type");
			if(type instanceof CArray) {
				CArray tmp = this.mapCArray((CArray)obj, (CArray)type);
				if(tmp != null)
					ccsvitems_unrelated.push(tmp);
			} // end if
			else if(type instanceof CHash) {
				CArray tmp = this.mapCHash((CHash)obj, (CHash)type);
				if(tmp != null)
					ccsvitems_unrelated.push(tmp);
			} // end if
			else if(type.toString().equals("num") || type.toString().equals("str"))
				ccsvitems_related.push(__.chash(csvfield, obj.toString()));
			else if(type.toString().equals("array-join"))
				ccsvitems_related.push(__.chash(csvfield, ((CArray)obj).join(this.m_jointoken)));
		} // end for
		return this.combineFields(ccsvitems_related, ccsvitems_unrelated);	
	} // end mapCHash()		
	
	public CArray mapCArray(CArray carrayitem, CArray chashmap) {
		CArray ccsvitems_related = __.carray();
		CArray ccsvitems_unrelated = __.carray();
		int len = carrayitem.length();
		for(int i=0; i<len; i++) {
			Object obj = carrayitem._(i);
			CHash map = chashmap._chash(0);
			String csvfield = map._string(this.m_metaprefix + "csv_field");
			Object type = map._(this.m_metaprefix + "type");
			if(type instanceof CArray) {
				CArray tmp = this.mapCArray((CArray)obj, (CArray)type);
				if(tmp != null)
					ccsvitems_unrelated.push(tmp);
			} // end if
			else if(type instanceof CHash) {
				CArray tmp = this.mapCHash((CHash)obj, (CHash)type);
				if(tmp != null)
					ccsvitems_unrelated.push(tmp);
			} // end if
			else if(type.toString().equals("num") || type.toString().equals("str"))
				ccsvitems_related.push(__.chash(csvfield, obj.toString()));
			else if(type.toString().equals("array-join"))
				ccsvitems_related.push(__.chash(csvfield, ((CArray)obj).join(this.m_jointoken)));		
		} // end for
		return this.combineFields(ccsvitems_related, ccsvitems_unrelated);	
	} // end mapCArray()

	public CArray combineFields(CArray ccsvitems_related, CArray ccsvitems_unrelated) {
		int len = ccsvitems_unrelated.length();
		CArray tmp = null;
		if(len > 0) {
			tmp = ccsvitems_unrelated._carray(0);
			for(int i=1; i<len; i++)
				tmp = this.combineUnRelatedFields(tmp, ccsvitems_unrelated._carray(i));
		} // end if
		len = ccsvitems_related.length();
		for(int i=0; i<len; i++) 
			tmp = this.combineRelatedFields(tmp, ccsvitems_related._chash(i));
		return tmp;
	} // end combineFields()
	
	public CArray combineUnRelatedFields(CArray ccsvitems1, CArray ccsvitems2) {
		if(ccsvitems1 == null && ccsvitems2 == null)
			return null;
		if(ccsvitems1 == null)
			return ccsvitems2;
		if(ccsvitems2 == null)
			return ccsvitems1;
		int len = ccsvitems2.length();
		for(int i=0; i<len; i++)
			ccsvitems1.push(ccsvitems2._carray(i));	
		return ccsvitems1;
	} // end combineRelatedFields()

	public CArray combineRelatedFields(CArray ccsvitems, CHash ccsvitem) {
		if(ccsvitems == null && ccsvitem == null)
			return null;
		if(ccsvitems == null) {
			ccsvitems = __.carray();
			ccsvitems.push(__.carray_1(ccsvitem));
			return ccsvitems;
		} // endif		
		if(ccsvitem == null)
			return ccsvitems;
		int len = ccsvitems.length();
		for(int i=0; i<len; i++)
			ccsvitems._carray(i).push(ccsvitem);
		return ccsvitems;	
	} // end combineRelatedFields()
	
	protected CArray convertToCCSV(CArray ccsvitems) {
		CArray csvtable = __.carray();
		CHash header = this.getHeader();
		CArray fields = header.keys(); 
		CArray csvheader = __.carray_c(fields.length());
		int len = fields.length();	
		for(int i=0; i<len; i++) {
			String field = fields._string(i);
			int index = (header._(field) instanceof CHash) ? header._chash(field)._int(this.m_metaprefix + "index") : header._int(field);
			csvheader._(index, field);
		} // end for
		csvtable.push(csvheader);
		int nitems = ccsvitems.length();
		for(int i=0; i<nitems; i++)
			this.convertToCCSV(csvtable, ccsvitems._carray(i));
		this.updateCCSVFields(csvtable);
		return csvtable;
	} // end convertToCCSV()
	
	protected void convertToCCSV(CArray csvtable, CArray ccsvitems) {
		int ilen = ccsvitems.length();
		CHash header = this.getHeader();
		CArray fields = header.keys(); 
		int jlen = fields.length();
		int index = -1;
		for(int i=0; i<ilen; i++) {
			CArray ccsvitem = ccsvitems._carray(i);
			CArray row = __.carray_c(jlen);
			int klen = ccsvitem.length();
			for(int k=0; k<klen; k++) {
				CHash chash = ccsvitem._chash(k);
				String field = chash.keys()._string(0);
				String value = chash._string(field);
				if(header._(field) instanceof CHash)
					index = header._chash(field)._int(this.m_metaprefix + "index");
				else index = header._int(field);
				row._(index, value.toString()); 
			} // end for
			csvtable.push(row);
		} // end for
	} // end convertToCCSV()
	
	protected void updateCCSVFields(CArray csvtable) {
		CHash header = this.getHeader();
		CArray colnames = header.keys();
		int len = colnames.length();
		for(int i=0; i<len; i++) {
			String colname = colnames._string(i);
			Object coldata = header._(colname);
			if(!(coldata instanceof CHash))
				continue;
			this.updateCCSVField(csvtable, (CHash) coldata);		
		} // end for
		return;
	} // end updateCCSVFields()
	
	protected void updateCCSVField(CArray csvtable, CHash coldata) {
		String strexpression = coldata._string(this.m_metaprefix + "csv_field");
		if(strexpression == null || strexpression == "")
			return;
		int dstindex = coldata._int(this.m_metaprefix + "index");
		CHash header = this.getHeader();
		CArray csvfields = header.keys();
		int nfields = csvfields.length();
		int nrows = csvtable.length();
		for(int i=1; i<nrows; i++) {
			CArray row = csvtable._carray(i);
			String strtmpexpression = strexpression;
			for(int j=0; j<nfields; j++) { 
				String srccsvfield = csvfields._string(j);
				if(strtmpexpression.indexOf("["+srccsvfield+"]") != -1) {
					Object srccoldata = this.getHeader()._(srccsvfield);
					int srcindex = (!(srccoldata instanceof CHash)) ? this.getHeader()._int(srccsvfield) : this.getHeader()._chash(srccsvfield)._int(this.m_metaprefix + "index");
					String value = row._string(srcindex);
					strtmpexpression = strtmpexpression.replace("["+srccsvfield+"]","" + value);
				} // end if
			} // end for
			String result = __.eval(strtmpexpression);
			if(result.indexOf("Exception") == -1)
				strtmpexpression = result;
			row._(dstindex, strtmpexpression);
		} // end for
		return;
	} // end updateCCSVField()
} // end CCHash2CCSV