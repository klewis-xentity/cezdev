//-------------------------------------------------------------------------
// file: __.java
// desc: contains global functions exdev and c3dclasses namespace/package
//-------------------------------------------------------------------------
package c3dclasses;
import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.applet.*;
import javax.swing.*;
import javax.imageio.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import org.apache.commons.lang.ArrayUtils;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

//-----------------------------------------------
// class: _
// desc: global methods used throughout the SDK
//-----------------------------------------------
public class __ {	
	////////////////////////////////
	// reading from the console
	public static boolean isEmpty() { return StdIn.isEmpty(); }
	public static int readInt() { return StdIn.readInt(); }
	public static double readDouble() { return StdIn.readDouble(); }
	public static float readFloat() { return StdIn.readFloat(); }
	public static long readLong() { return StdIn.readLong(); }
	public static boolean readBoolean() { return StdIn.readBoolean(); }
	public static char readChar() { return StdIn.readChar(); }
	public static byte readByte() { return StdIn.readByte(); }
	public static String readString() { return StdIn.readString(); }
	public static boolean hasNextLine() { return StdIn.hasNextLine(); }
	public static String readLine() { return StdIn.readLine(); }
	public static String readAll() { return StdIn.readAll(); }
	public static String readln() { return (String) m_scanner.nextLine(); }
	public static Scanner m_scanner = new Scanner(System.in);
		
	/////////////////////////////////////////////////////////////
	// printing to the console
	public static void echo(Object object) { __.print(object); }
	public static void printbr(String str) { __._print(str + "<br />"); }
	public static void printbr() { __.printbr(""); }
	public static void println(Object object) { if(object != null) __._print(object.toString() + "\n"); }
	public static void println(){ __.println(""); }
	public static void printjs(Object strjstoprint) { __._print("<script>" + strjstoprint.toString() + "</script>"); }
	public static void print(Object str) { __._print(str); }
	public static void print(Object[] str) { __.print(str, ","); }
	public static void printf(Object format, Object... args) { System.out.printf(format.toString(), args); }
	public static void print(Object[] str, String delimiter) { 
		for(int i=0; i<str.length; i++) {		
			__.print(str[i]);
			if(i < str.length-1)
				__.print(delimiter);
		} // end for
	} // end print()
	public static String print_r(Object object, boolean bstr) {
		String str="";
   		if(object!=null && object.getClass().isArray()) {
			Object [] objs = (Object[])object;
			int len = objs.length;
			for(int i=0; i<len; i++) {
				str += print_r(objs[i], bstr);
				if(i < len-1)
					str += ",";
			} // end for
		} // end if	
		else str += object.toString(); 
		if(bstr)
			return str;
		__.print(str);
		return ""; 
	} // end print_r()
	public static String print_r(Object object) { return __.print_r(object,false); } 
	public static void _print(Object str) { if(str != null) System.out.print(str); }	
	public static void cont(Object str, String alertmsg) { __.print(str); __.alert(alertmsg); }
		
	//////////////////////////////////////////////
	// alert, prompt, prompt_file, prompt_path
	public static void alert(Object strmessage) { __.alert(strmessage, true); }
	public static void alert(Object strmessage, String strtitle) { alert(strmessage.toString(), strtitle); }
	public static void alert(String strmessage, String strtitle) {
		strmessage = (strmessage == "" || strmessage == null) ? "  " : strmessage;
		strtitle = (strtitle == "" || strtitle == null) ? "  " : strtitle;
		JOptionPane.showMessageDialog(null, strmessage, strtitle, JOptionPane.INFORMATION_MESSAGE);
	} // end alert()
	public static void alert(Object strmessage, boolean bscript) { 
		JOptionPane.showMessageDialog ( null, 
			(strmessage != null) ? strmessage.toString() : "", 
			"Alert", JOptionPane.INFORMATION_MESSAGE
		); // end JOptionPane.showMessageDialog
	} // end alert()
	public static Object prompt(String strtitle, String strmessage, Object [] choices) {
		int itype = JOptionPane.INFORMATION_MESSAGE;
		Object output = "";
		if(choices == null)
			output = JOptionPane.showInputDialog(null, strmessage, strtitle, itype);
		else output = JOptionPane.showInputDialog(null, strmessage, strtitle, itype, null, choices, choices[0]);
		return (output != null) ? output : null;
	} // end prompt()
	public static String prompt_file(String strtype, String strtitle) {
		strtitle = (strtitle != null) ? strtitle : "Open";		
		strtype = (strtype != null) ? strtype : "file";
		JFileChooser fileChooser = new JFileChooser();
		if(strtype.equals("dir")) // select a directory instead of file
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle(strtitle);
        int result = fileChooser.showOpenDialog(null);
        return (result == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile().getPath() : null;
	} // end prompt_file()
	public static String prompt_path(String strtitle) {
    	return __.prompt_file("dir", strtitle);
	} // end prompt_path()		

	///////////////////////////////////////
	// html, console, confirm, javascript
	public static void console(Object strmessage, boolean bscript) { 
		if(bscript) 
			__.print("<script parse=\"false\">"); 
		__.print("console.log('" + strmessage + "');"); 
		if(bscript) 
			__.print("</script>");
	} // end console()
	public static void confirm(String strmessage, String strjsyesbody, String strjsnobody, boolean bscript) { 
		if(bscript) 
			__.print("<script parse=\"false\">"); 
		__.print("if(confirm('"+strmessage+"') == true) {"+strjsyesbody+"} else {"+strjsnobody+"}");
		if(bscript) 
			__.print("</script>"); 
	} // end confirm()
	public static void confirm(String strmessage, String strjsyesbody, String strjsnobody) { 
		__.confirm(strmessage, strjsyesbody, strjsnobody); 
	} // end confirm()
	
	////////////
	// time
	public static long time() { return __.get_time_ms(); }
	public static long get_time() { return __.get_time_ms(); }
	public static float get_time_mms() { return System.nanoTime() / 1000; }
	public static long get_time_ms() { return System.currentTimeMillis(); }

	/////////////
	// sleep
	public static void sleep(int milliseconds) { try { Thread.sleep(milliseconds); } catch(InterruptedException ex) {} }
	
	///////////////
	// hash_code
	public static int hash_code(Object object) { return System.identityHashCode(object); }

	///////////////////////////////
	// directory, file, path
	public static String dirname(String strfilename) { return __.get_path(strfilename); }
	public static String get_home_path() { 
		try{
			String str = new File(".").getCanonicalPath(); 
			return str; 
		} // end try
		catch(IOException ex){ 
			//CLog.error(ex.toString());
			return ""; 
		} // end catch()
	} // end getHomePath()
	public static String get_path(String strfilename) {
		try {
		 	File f = new File(strfilename);
         	return f.getParentFile().getCanonicalPath();
        } // end try
		catch(Exception ex) {
			//CLog.error(ex.toString());
			return "";
		} // end catch()
	} // end getPath()	
	public static boolean is_directory(String strpathname) {
		try{ File f = new File(strpathname); return (f.exists() && f.isDirectory());}
		catch(Exception ex) { /*CLog.error(ex.toString());*/ return false; }
	} // end isDirectory()
	public static boolean is_file(String strpathname) {
		try{ File f = new File(strpathname); return (f.exists());}
		catch(Exception ex) { /*CLog.error(ex.toString());*/ return false; }
	} // end isFile()	
	public static void traverse_path(String strstartpath, CFunction fncallback) {
		traverse_path(new File(strstartpath), fncallback);
	} // end traverse_path()
	public static void traverse_path(File dir, CFunction fncallback) {
		try {
			CReturn creturn = fncallback.call((Object)dir);
			//if(creturn._boolean(false))
			//	return;
			if(dir.isDirectory()) {
				String[] children = dir.list();
				for (int i=0; children != null && i<children.length; i++)
					traverse_path(new File(dir,children[i]), fncallback);	
			} // end if
		} // end try
		catch(Exception ex) {
			return;
		} // end catch
	} // end traverse_files()
	public static String get_file_contents(String strfilename) {
		String strcontents = "";
		try {
			Reader in = new BufferedReader(new FileReader(strfilename));
			StringBuilder sb = new StringBuilder();
			char[] chars = new char[1 << 16];
			int length;
			while ((length = in.read(chars)) > 0) {
				sb.append(chars, 0, length);
			} // end while()
			strcontents = sb.toString();
			in.close();
		} // end try
		catch(Exception ex) {
			//CLog.error(ex.toString());
			//__.alert(ex.getMessage());
			return "";
		} // end catch()
		return strcontents;
	} // end getFileContents()
	public static String str_replace_key_with_contents(String str, CHash chashKey2Content) {
		if(chashKey2Content == null)
			return str;
		CArray keys = chashKey2Content.keys();
		int len = keys.length();
		for(int i=0; i<len; i++) {
			String key = keys._string(i);
			String value = chashKey2Content._string(key);
			str = str.replace(key,value);
		} // end for
		return str;
	} // end str_replace_key_with_contents()
	
	public static boolean file_replace_key_with_contents(String strkeyvaluejsonfilename, String strinfilename, String stroutfilename, CHash keysvalues) {
		String strcontents = __.get_file_contents(strinfilename);
		CHash chashKey2Content = __.json_file_2_chash(strkeyvaluejsonfilename);
		chashKey2Content.append(keysvalues);
		strcontents = __.str_replace_key_with_contents(strcontents, chashKey2Content);
		return __.set_file_contents(stroutfilename, strcontents);
	} // end file_replace_key_with_contents()
	
	public static boolean set_file_contents(String strfilename, String strcontents) {
		if(strcontents == null)
			return false;
		try{
			//__.alert("set file contents here");
			FileWriter out = new FileWriter(strfilename);
			out.write(strcontents);
			out.close();
		} // end try
		catch(Exception ex) {
			//CLog.error(ex.toString());
			return false;
		} // end catch() 	
		return true;
	} // end setFileContents()
	public static boolean append_file_contents(String strfilename, String strcontents) {
		if(strcontents == null || strcontents == "")
			return false;
		try{
			//__.alert("set file contents here");
			FileWriter out = new FileWriter(strfilename, true);
			out.write(strcontents);
			out.close();
		} // end try
		catch(Exception ex) {
			//CLog.error(ex.toString());
			return false;
		} // end catch() 	
		return true;
	} // end append_file_contents()
	public static String file_get_contents(String strfilename) { return __.get_file_contents(strfilename); }
	public static boolean file_set_contents(String strfilename, String strcontents) { return __.set_file_contents(strfilename, strcontents); }
	public static boolean file_exists(String strfilename) { File f = new File(strfilename); return f.exists(); }
	public static CArray get_lines_from_file(String strfilename) { return __.get_lines_from_file(strfilename, false, null, null); }
	public static CArray load_csv_file(String strfilepathname) { return __.get_lines_from_file(strfilepathname, true, ",", "/"); }
	public static boolean save_csv_file(String strfilepathname, CArray carray) { 
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(strfilepathname));
			int l = carray.length();
			String str = "";
			for(int i=0; i<l; i++) {
				if(carray._carray(i) != null) {
					writer.write(carray._carray(i).join(","));
					if(i < l-1)
						writer.write("\n");
				} // end if
			} // end for
			writer.close();
			return true;
		} // end try
		catch(Exception ex) {
			__.println(ex);
			__.alert(ex);
			return false;
		} // end catch
	} // end save_csv_file()
	public static CArray get_lines_from_file(String strfilename, boolean bsplitlines, String strdelimiter, String strreplacement) {
		if(strfilename == null || strfilename == "")
			return null;
		try {
			CArray lines = __.carray();
			String strline = "";
			LineNumberReader in = new LineNumberReader(new FileReader(strfilename));
			while((strline = in.readLine()) != null) { 
				if(strline.trim().isEmpty())
					continue;
				if(strreplacement != null) {
					for(int s = strline.indexOf("\""), e = strline.indexOf("\"", s+1); 
						s != -1 && e != -1; 
						s = strline.indexOf("\""), e = strline.indexOf("\"", s+1)) {
						String strline_start = strline.substring(0,s);
						String strline_replace = strline.substring(s+1, e).replace(strdelimiter, strreplacement);
						String strline_end = strline.substring(e+1);
						strline = strline_start + strline_replace + strline_end;	
					} // end for	
				} // end if
				lines.push((!bsplitlines) ? strline : __.split(strdelimiter, strline)); 
			} // end while()	
			in.close();
			return lines;		
		} // end try
		catch(Exception ex) {
			//CLog.error(ex.toString());
			__.alert(ex.getMessage());
			return null; 
		} // end catch()
	} // end get_lines_from_file()
	public static boolean file_delete(String strfilepath) { return (new File(strfilepath)).delete(); }
	public static boolean file_copy(String strsrcfilepath, String strdstfilepath) {
		try {
			File srcfile = new File(strsrcfilepath); 
			File dstfile = new File(strdstfilepath);
			Files.copy(srcfile.toPath(), dstfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return true;
		} // end try
		catch(Exception ex) {
			return false;
		} // end catch()
	} // end file_copy
									
	
	/*
	public static void file_copy(String strsrcfolder, 
									String strdstfolder, 
									CArray formatstocopy, 
									CArray formatstoskip, 
									CArray filestocopy) {
		try {
			File srcfolder = new File(strsrcfolder);
			File dstfolder = new File(strdstfolder);
			if (srcfolder.isDirectory()) {
				if(!dstfolder.exists()) {
					dstfolder.mkdir();
					println("Directory created :: " + dstfolder);
				} // end if
				String files[] = srcfolder.list(); 
				for (String file : files) {
					File srcfile = new File(srcfolder, file);
					File dstfile = new File(dstfolder, file);
					file_copy(srcfile.getPath(), dstfile.getPath(), formatstocopy, formatstoskip, filestocopy);
				} // end for
			} // end if
			else {
				// Copy the file content from one place to another
				if(file_check_format(srcfolder.getName(), filestocopy) ||
					file_check_format(srcfolder.getName(), formatstocopy) && 
					!file_check_format(srcfolder.getName(), formatstoskip)) {
					Files.copy(srcfolder.toPath(), dstfolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
					println("File copied :: " + dstfolder);
				} // end if
			} // end else
		} // end try
		catch(Exception ex) {
			println(ex.getMessage());
		} // end catch
		return;
	} // end file_copy()
	
	//-------------------------------------------------------
	// name: file_check_format()
	// desc:
	//-------------------------------------------------------
	public static boolean file_check_format(String strfilename, CArray formats) {
		if(formats == null)
			return false;
		int len = formats.length();
		for(int i=0; i<len; i++)
			if(strfilename.indexOf(formats._string(i)) != -1)
				return true;
		return false;
	} // end file_check_format()
	*/
	
	////////////////////////////////////////
	// file_path / dir_path of an object
	public static CHash m_filetodir = null; 
	public static String file_path(String strfilename) {		
		if(strfilename == null || strfilename == "")
			return "";
		if(__.m_filetodir == null) {
			__.m_filetodir = CJSON.toCHash(__.file_get_contents(__.get_home_path() + "/../c3dclassessdk.filenames.json"));
			if(__.m_filetodir == null)
				__.m_filetodir = CJSON.toCHash(__.file_get_contents(__.get_home_path() + "/../../c3dclassessdk.filenames.json"));
		} // end if
		if(__.m_filetodir == null)
			return "";

		String strpath = __.m_filetodir._string(strfilename);
		if(strpath == null || strpath == "")
			return "";
		if(__.file_exists(strpath))
			return strpath;

		String strhome = __.get_home_path().replace("\\", "/");
		int i = strhome.toLowerCase().indexOf("/cezdev");
		if(i != -1) {
			String strroot = strhome.substring(0, i) + "/cezdev";
			String strnormalized = strpath.replace("\\", "/");
			int j = strnormalized.toLowerCase().indexOf("/cezdev/");
			if(j != -1) {
				String strremapped = strroot + strnormalized.substring(j + "/cezdev".length());
				if(__.file_exists(strremapped)) {
					__.m_filetodir._(strfilename, strremapped);
					return strremapped;
				}
			}
		}

		return strpath;	
	} // end file()
	public static String file_path(Object object) { return __.file_path(object.getClass().getSimpleName() + ".java"); }
	public static String check_file_path(String strpath) { String path = __.file_path(strpath); return (strpath == "" || strpath == null || path == "" || path == null) ? strpath : __.file_path(strpath); }
	public static String dir_path(String strfilename) { return __.get_path(__.file_path(strfilename)); }
	public static String dir_path(Object object) { return __.get_path(__.file_path(object)); }
	public static String check_dir_path(String strpath) { String path = __.file_path(strpath); return (strpath == "" || strpath == null || __.file_path(strpath) == "" || path == "" || path == null) ? strpath : __.file_path(strpath); }
	
	///////////
	// out
	public static void out(String strfilename, String strcontents) { __.file_set_contents(strfilename, strcontents);}
	public static void out_append(String strfilename, String strcontents) { __.append_file_contents(strfilename, strcontents);}
	public static void outln(String strfilename, String strcontents) { __.out_append(strfilename, strcontents + "\n");}
	public static void out(Object object, Object contents) { 
		String strpath = __.dir_path(object.getClass().getSimpleName() + ".java");
		String strfilename = object.getClass().getSimpleName() + ".out";
		__.out(strpath + "/" + strfilename, contents.toString());
	} // end out()
	public static void out_append(Object object, Object contents) { 
		String strpath = __.dir_path(object.getClass().getSimpleName() + ".java");
		String strfilename = object.getClass().getSimpleName() + ".out";
		__.out_append(strpath + "/" + strfilename, contents.toString());
	} // end out_append()
	public static void outln(Object object, Object contents) { __.out_append(object, contents + "\n"); }

	/////////////////
	// image files
	public static CMatrix load_img_file(String strfilename) {
		BufferedImage img = __._load_img_file(strfilename);
		if(img == null)
			return null;
		int w = img.getWidth();
		int h = img.getHeight();
		CMatrix cimg = new CMatrix(h, w);
		for(int y=0; y<h; y++) {	
			for(int x=0; x<w; x++) {
				cimg.ij(y,x,img.getRGB(x,y)&0xFF);
			} // end for
		} // end for
		return cimg;
	} // end load_img_file()
	public static boolean save_img_file(String stroutfilename, String strformat, CMatrix img) {
		int w = img.columnLength();
		int h = img.rowLength();
		BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		if(bimg == null)
			return false;
		for(int y=0; y<h; y++) {
			for(int x=0; x<w; x++) {
				int rgb = (int)img.ij(y,x);
				bimg.setRGB(x,y,(rgb<<16)+(rgb<<8)+rgb);
			} // end for
		} // end for
		return __.save_img_file(stroutfilename, strformat, bimg);
	} // end save_img_file()
	// helper
	public static BufferedImage _load_img_file(String strfilename) {
		try {
			return ImageIO.read(new File(strfilename));
		} // end try 
		catch (IOException e) {
			return null;
		} // end catch()
	} // end load_img_file()
	public static boolean save_img_file(String stroutfilename, String strformat, BufferedImage img) {
		try {
			ImageIO.write(img, strformat, new File(stroutfilename));
			return true;
		} // end try 
		catch (IOException e) {
			return false;
		} // end catch()
	} // end load_img_file()
	public static boolean resize_img_file(String inputImagePath, 
							   String outputImagePath, 
							   int scaledWidth, 
							   int scaledHeight) {
        try {
			// reads input image
			File inputFile = new File(inputImagePath);
			BufferedImage inputImage = ImageIO.read(inputFile);

			// creates output image
			BufferedImage outputImage = new BufferedImage(scaledWidth,
					scaledHeight, inputImage.getType());

			// scales the input image to the output image
			Graphics2D g2d = outputImage.createGraphics();
			g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
			g2d.dispose();

			// extracts extension of output file
			String formatName = outputImagePath.substring(outputImagePath
					.lastIndexOf(".") + 1);

			// writes to output file
			ImageIO.write(outputImage, formatName, new File(outputImagePath));
			return true;
		} // end try
		catch(Exception ex) {
			return false;
		} // end catch
    } // end resize_img_file()
 
    public static boolean resize_img_file(String inputImagePath, String outputImagePath, double percent){
        try {
			File inputFile = new File(inputImagePath);
			BufferedImage inputImage = ImageIO.read(inputFile);
			int scaledWidth = (int) (inputImage.getWidth() * percent);
			int scaledHeight = (int) (inputImage.getHeight() * percent);
			return __.resize_img_file(inputImagePath, outputImagePath, scaledWidth, scaledHeight);
		} // end try
		catch(Exception ex) {
			return false;
		}
    } // end resize_img_file()
 
	///////////////////
	// allocation
	public static Object _new(String strclassname) { 
		try { 
			return Class.forName(strclassname).newInstance(); 
		} // end try
		catch(Exception ex) { 
			return null;
		} // end catch()
	} // end _new() 
	
	///////////////
	// parsing	
	public static int parse_int(String str) { return Integer.parseInt(str); }
	public static float parse_float(String str) { return Float.parseFloat(str); }
	public static double parse_double(String str) { return Double.parseDouble(str); }
	
	//////////////////////////////////////////////////////////
	// string imploding / exploding / splitting / combining	
	public static CArray split(String strseperator, String strtoseperate) {
		if(strseperator == "" || strtoseperate == "")
			return null;
		CArray strings = new CArray();
		String [] strs = strtoseperate.split(strseperator);
		for(int i=0; i<strs.length; i++)
			strings.push(strs[i]);
		return strings;	
	} // end split()
	public static CArray explode(String strseperator, String strtoseperate) {
		return __.split(strseperator, strtoseperate);
	} // end explode()
	
	/////////////////
	// driver code
	public static void include_driver(String strdriverclassname) { CDriver.include(strdriverclassname); }	
	
	////////////////
	// commands
	/*
	public static CArray exec_command(String strcommand) {
		try { 
			Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(strcommand);
            InputStream inputstream = proc.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            String line;
			CArray out = __.carray();
            while ((line = bufferedreader.readLine()) != null)
                out.push(line);
			return out;
        } // end try 
		catch (Exception ex) {
            ex.printStackTrace();
       		return null;
	    } // end catch()
	} // end exec_command()
	*/

public static CArray exec_command(String strcommand) {
    try {
        // Detect the operating system
        String os = System.getProperty("os.name").toLowerCase();
        String[] command;

        if (os.contains("win")) {
            // Windows uses cmd.exe
            command = new String[]{"cmd.exe", "/c", strcommand};
        } else {
            // Linux/Mac use sh
            command = new String[]{"/bin/sh", "-c", strcommand};
        }

        // Run the process
        Process proc = Runtime.getRuntime().exec(command);

        // Capture output
        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

        String line;
        CArray out = __.carray();
        while ((line = bufferedreader.readLine()) != null) {
            out.push(line);
        }

        proc.waitFor(); // Wait for process to finish
        return out;

    } catch (Exception ex) {
        ex.printStackTrace();
        return null;
    }
}


	/*
	public static boolean exec_command(String strcommand) {
		try { 
			Process p = Runtime.getRuntime().exec(strcommand); 
			int returnCode = p.waitFor(); 
			return true; 
		} // end try
		catch(Exception ex) { 
			__.alert(ex.getMessage());
			return false; 
		} // end catch()
	} // end exec_command()
	*/
	
	public static boolean exec_async_command(String strcommand) {
		try { 
			Process p = Runtime.getRuntime().exec(strcommand); 
			System.out.println("SUCCESS: __.execCommand(): executed command: " + strcommand); 
			return true; 
		} // end try
		catch(Exception ex) { 
			System.out.println("ERROR: __.execCommand(): couldn't execute command: " + strcommand + " reason: " + ex.getMessage());
			return false; 
		} // end catch()
	} // end exec_async_command()
	
	// returns true if pid is running
	public static boolean is_pid_running(long pid) {
        try {
            Runtime runtime = Runtime.getRuntime();
            String cmds[] = {"cmd", "/c", "tasklist /FI \"PID eq " + pid + "\""};
            Process proc = runtime.exec(cmds);
            InputStream inputstream = proc.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            String line;
            while ((line = bufferedreader.readLine()) != null) {
                if (line.contains(" " + pid + " ")) {
                    return true;
                } // end if
            } // end while
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Cannot query the tasklist for some reason.");
            System.exit(0);
        } // end catch
        return false;
    } // end is_pid_running()
	
	// prints all the running pids
    public static String to_str_pids() {
        try {
            Runtime runtime = Runtime.getRuntime();
            String cmds[] = {"cmd", "/c", "tasklist"};
            Process proc = runtime.exec(cmds);
            InputStream inputstream = proc.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            String line;
			String str = "";
            while ((line = bufferedreader.readLine()) != null) {
                str += line + "\n";
            } // end while
			return str;
        } // end try 
		catch (Exception ex) {
            ex.printStackTrace();
       		return "";
	    } // end catch()
    } // end to_str_pids()
	
	// returns pid of process
	public static long get_pid() { 
		String jvmName = ManagementFactory.getRuntimeMXBean().getName(); 
		return Long.valueOf(jvmName.split("@")[0]); 
	} // end get_pid()
	
	///////////////////////
	// interval / timeout
	public static int setInterval(CFunction cfunction, int imilliseconds) { return CIntervalConcurrentEvent.setInterval(cfunction, imilliseconds); }
	public static void clearInterval(int id) { CIntervalConcurrentEvent.clearInterval(id); }
	public static int setTimeout(CFunction cfunction, int imilliseconds) { return CTimeoutConcurrentEvent.setTimeout(cfunction, imilliseconds); }
	public static void clearTimeout(int id) { CTimeoutConcurrentEvent.clearInterval(id); }	
	
	///////////////
	// carray
	public static CArray carray_csv(String strfilename) { return __.load_csv_file(strfilename); }
	public static CArray carray_c(int... capacity) { return new CArray(capacity); }
	public static CArray carray_1(Object object) { CArray tmp = __.carray(); tmp.push(object); return tmp; }  
	public static CArray carray() { return new CArray(); }
	public static CArray carray(Object... objects) { return new CArray(objects); }
	public static CArray readCArray() { return null; }
	public static CArray carray(CMatrix cmatrix) {
		int nr = cmatrix.rowLength();
		int nc = cmatrix.columnLength();
		CArray rows = new CArray();
		CArray cols = new CArray();
		for(int j=0; j<nc; j++)
			cols.push("Z"+j);
		rows.push(cols);
		for(int i=0; i<nr; i++) {
			cols = new CArray();
			for(int j=0; j<nc; j++)
				cols.push(cmatrix.ij(i,j));
			rows.push(cols);
		} // end for
		return rows;
	} // end carray()
	public static CArray carray(CHash chash) {
		CArray keys = chash.keys();
		CArray carray = __.carray();
		for(int i=0; i<keys.length(); i++)
			carray.push(chash._(keys._(i)));
		return carray;
	} // end carray()
	public static CArray args(Object... args) { return __.carray(args); }
	public static CArray args() { return __.carray(); }
	public static CArray a() { return new CArray(); }
	public static CArray a(Object... objects) { return new CArray(objects); }
	
	//////////////////
	// chash
	public static CHash chash() { return new CHash(); }
	public static CHash chash(CHash chash) { return (chash == null) ? new CHash() : chash; }
	public static CHash chash(CArray keys, CArray values) { return new CHash(keys, values); }
   	
	// New method to initialize CHash from a JSON file (pure Java)
	public static CHash chash(String strfilename) {
		CHash chash = new CHash();

		try (BufferedReader reader = new BufferedReader(new FileReader(strfilename))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				// Skip empty lines and braces
				if (line.isEmpty() || line.equals("{") || line.equals("}")) continue;

				// Remove trailing comma if exists
				if (line.endsWith(",")) line = line.substring(0, line.length() - 1);

				// Split on the first colon
				int colonIndex = line.indexOf(":");
				if (colonIndex == -1) continue; // invalid line, skip

				String key = line.substring(0, colonIndex).trim();
				String value = line.substring(colonIndex + 1).trim();

				// Remove surrounding quotes
				if (key.startsWith("\"") && key.endsWith("\"")) key = key.substring(1, key.length() - 1);
				if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);

				// Add to CHash
				chash.set(key, value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return chash;
	} // end
	public static CHash chash(Object... objects) { return new CHash(objects); }	
	public static CHash readCHash() { return null; }
	public static CHash json_file_2_chash(String strjsonfile) { return CJSON.toCHash(__.file_get_contents(strjsonfile)); }
	public static CHash json_str_2_chash(String strjson) { return CJSON.toCHash(strjson); }
	public static CHash params(Object... params) { return __.chash(params); }
	public static CHash params() { return __.chash(); }	
	public static CHash h(Object... params) { return __.chash(params); }
	public static CHash h() { return __.chash(); }	
	
	//////////////////
	// cobject
	public static CObject cobject() { return new CObject(); }
	public static CObject cobject(Object... objects) { return new CObject(objects); }
	public static CObject obj(Object... namevalues){ return __.cobject(namevalues); }
	public static CObject obj(){ return __.cobject(); }
	public static CObject o(Object... namevalues){ return __.cobject(namevalues); }
	public static CObject o(){ return __.cobject(); }
	
	///////////////////
	// cvectors 
	public static CVector cvector() { return new CVector(); }
	public static CVector cvector(CVector cvector) { return new CVector(cvector); }
	public static CVector cvector(double... v) { return new CVector(v); }
	public static CVector cvector_c(int size) { return new CVector(size); }
	public static CVector cvector_csv(String strfilename) { return null; }
	public static CVector v(double... v) { return new CVector(v); }
	public static CVector v_c(int size) { return new CVector(size); }
	public static CVector readCVector() { return null; }
	
	///////////////////
	// cmatrix
	public static CMatrix cmatrix() { return new CMatrix(); }
	public static CMatrix cmatrix(CMatrix cmatrix) { return new CMatrix(cmatrix); }
	public static CMatrix cmatrix_csv(String strcsvfilename) { return __.cmatrix(__.load_csv_file(strcsvfilename)); }
	public static CMatrix cmatrix(CVector... cvectors) { return new CMatrix(cvectors); }
	public static CMatrix m(CVector... cvectors) { return new CMatrix(cvectors); }
	public static CMatrix cmatrix(CArray carray) { return __.cmatrix(carray, null); }
	public static CMatrix cmatrix(CArray carray, CArray skip) {
		CArray incarray = carray._carray(0);
		if(incarray == null)
			return null;
		int sl = (skip != null) ? skip.length() : 0; 
		int nr = carray.length();
		int nc = incarray.length();
		CMatrix M = new CMatrix(nr,nc - sl);
		for(int i=0; i<nr; i++)
			for(int j=0; j<nc; j++)
				if(skip == null || skip.indexOf(j) == -1)
					if(!carray._carray(i)._nan(j))
						M.ij(i,j, carray._carray(i)._double(j));
		return M;
	} // end cmatrix()
	public static CMatrix readCMatrix() { return null; }
	
	///////////////////////////
	// cbitarray
	public static CBitArray cbitarray(int length) { return new CBitArray(length); }
	
	///////////////////
	// cfunction
	public static CFunction cfunction(String strname) { return CFunction.get(strname); }
	public static CFunction func(String strname) { return __.cfunction(strname); }
	public static CFunction f(String strname) { return __.cfunction(strname); }
	//public static CFunction f(String strname, CFunction cfunction) { return CFunction.set(strname, cfunction); }
	
	///////////////////
	// ccommand
	//public static CCommand ccommand(String strname) { return CCommand.get(strname); }
	//public static CCommand cmd(String strname) { return __.ccommand(strname); }
	//public static CCommand c(String strname) { return __.ccommand(strname); }
	
	/////////////////////////
	// other
	public static String s(String str) { return "\"" + str + "\""; }
	public static boolean is_within(double l, double u, double v) { return (l<=v && v<=u); }
	public static int cmp(int a, int b) { return (a>b) ? 1 : ((a<b) ? -1 : 0); }
	public static int cmp(double a, double b) { return (a>b) ? 1 : ((a<b) ? -1 : 0); }
	public static int cmp(float a, float b) { return (a>b) ? 1 : ((a<b) ? -1 : 0); }
	public static int cmp(String a, String b) { return a.compareTo(b); }
	public static String eval(String strexpression) {
		try {
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");
			return engine.eval(strexpression).toString();
		}
		catch(Exception ex) {
			return ex.toString();
		}
	} // end eval()
	public static String typeOf(Object object) {
		if(object instanceof CArray) return "carray";
		if(object instanceof CHash) return "chash";
		if(object instanceof CFunction) return "cfunction";
		if(object instanceof CVector) return "cvector";
		if(object instanceof CMatrix) return "cmatrix";
		if(object instanceof Float) return "number";
		if(object instanceof Double) return "number";
		if(object instanceof Integer) return "number";
		if(object instanceof String) return "string";
		return "object";
	} // end typeOf()
	
	// random functions
	public static int random(int min, int max) { return (int) (min + (Math.random() * (max - min))); }
			
	/*
		public boolean _nan() { return this._is_nan(this.get()); }
		public boolean _is_string() { return this.get() instanceof String; }
		public boolean _is_carray() { return this.get() instanceof CArray; }
		public boolean _is_chash() { return this.get() instanceof CHash; }
		public boolean _is_cfunction() { return this.get() instanceof CFunction; }
		public boolean _is_cvector() { return this.get() instanceof CVector; }
		public boolean _is_cmatrix() { return this.get() instanceof CMatrix; }
	*/
	//////////////////////////////
	// other constructs
	/*
	//----------------------------------------------------------------
	// name: _while()
	// desc: implements a asynchronous while loop control structure
	//----------------------------------------------------------------
	public static CLoop _while(CHash params, CCondition ccond, CBody cbody){
		CLoop cloop = new CLoop();
		return (cloop == null || cloop.create(params, ccond, cbody) == false) ? null : cloop;
	} // end _while()
*/
/*
	//----------------------------------------------------------------
	// name: _for()
	// desc: implements a asynchronous for loop control structure
	//----------------------------------------------------------------
	public static CLoop _for(CInitialization cinit, CCondition ccond, CIncrement cinc, CBody cbody){
		CLoop cloop = new CLoop();
		return (cloop == null || CLoop.create(cinit, ccond, cinc, cbody) == false) ? null : cloop;
	} // end _for

	//---------------------------------------------------------------
	// name: _if()
	// desc: sets up an async if structure
	//---------------------------------------------------------------
	public static CIf _if(CCondtion ccond, CBody cbody){
		CIf __if = new CIf();
		return (__if==null || __if.create(ccond, cbody) == false) ? null : __if;
	} // end _if()

	//---------------------------------------------------------------
	// name: _do()
	// desc: sets up an async do/while structure
	//---------------------------------------------------------------
	public static CDoWhile _do(CBody cbody){
		CDoWhile __dowhile = new CDoWhile();	
		if(__dowhile==null || __dowhile.create(cbody) == false) ? null : __dowhile;	
	} // end _do()

	//--------------------------------------------------
	// name: _switch()
	// desc: sets up an asynchronous switch construct
	//--------------------------------------------------
	public static CSwitch _switch(Object value){
		CSwitch __switch = new CSwitch();
		return (__switch==null || __switch.create(value)==false) ? null ? __switch;
	} // end _switch()
	*/
} // end _