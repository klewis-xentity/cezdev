//---------------------------------------------------------------------------------------------
// name: CCSV.java
// desc: reads/writes CSV formated files
//---------------------------------------------------------------------------------------------
package c3dclasses;
import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;;

//-----------------------------------------------------------------
// name: CCSV
// desc: reads/writes CSV formated files
//-----------------------------------------------------------------
public class CCSV {	
	protected CArray m_csv;
	protected CArray m_id2name;
	protected CHash m_name2id;
	public CCSV() {
		this.m_csv = null;
		this.m_id2name = null;
		this.m_name2id = null;
	} // end CCSV()
	public boolean open(String infilename) {
		CArray csv = __.load_csv_file(infilename);
		if(csv == null)
			return false;
		this.m_csv = csv;
		this.m_id2name = csv._carray(0);
		this.m_name2id = __.chash();
		for(int i=0; i<this.m_id2name.length(); i++)
			this.m_name2id._(this.m_id2name._string(i), i);
		return true;
	} // end open()
	public int 		n2i(String name){ return this.m_name2id._int(name); }
	public String 	i2n(int id){ return this.m_id2name._string(id); }
	public boolean 	save(String outfilename) { return __.save_csv_file(outfilename, this.m_csv); }
	public CArray 	getCSV() { return this.m_csv; }
	public int 		getColumnLength() { return this.m_id2name.length(); }
	public int 		getRowLength() { return this.m_csv.length()-1; }
	public CArray 	getColumnHeading(){ return this.m_id2name; }
	public CHash 	getColumnHeadingAsName(){ return this.m_name2id; }
	public CArray 	getRow(int row) { return this.m_csv._carray(row+1); }
	public CHash  	getRowAsCHash(int row) { return new CHash(this.m_id2name, this.getRow(row)); }
	public CCSV   	setCellData(int row, int col, Object data) { this.getRow(row)._(col, data); return this; }
	public CCSV  	setCellData(int row, String colname, Object data) { this.getRow(row)._(this.m_name2id._int(colname), data); return this; }
	public Object 	getCellData(int row, int col) { return this.getRow(row)._(col); }
	public String 	toString() { return this.m_csv.toString(); }
} // end CCSV