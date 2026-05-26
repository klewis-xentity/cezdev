//--------------------------------------------------------------
// name: CFormInstructions
// desc: implements form instruction set
//--------------------------------------------------------------
package c3dclasses;
import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener; 
import java.applet.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import javax.swing.border.*;

//--------------------------------------------------------
// name: CFormInstructions
// desc: implements form instruction set
//--------------------------------------------------------
class CFormInstructions extends CInstructions {
	public CFormInstructions(CProcessor cprocessor) {
		super(cprocessor);	
		// instruction function
		final CControlInstructions ccontrolinstructions = (CControlInstructions) cprocessor.getCInstructions("CControlInstructions");
		CFunction fnCreateJFrame = new CFunction() { public CReturn call(CObject obj) {
			CControl control = (CControl) obj; 
			ccontrolinstructions.createJControl(control, new JFrame((String)control._("m_value")));
			return CReturn._done(control);
		}}; // end fnCreateJFrame

		CFunction fnCreateJPanel = new CFunction() { @Override public CReturn call(CObject obj) {
        	CControl control = (CControl) obj;
        	JPanel panel = new JPanel();
        	String title = (String) control._("m_value");
        	if (title != null && !title.isEmpty()) {
            	panel.setBorder(BorderFactory.createTitledBorder(title));
        	} // end if
        	ccontrolinstructions.createJControl(control, panel);
        	return CReturn._done(control);
    	}}; // end fnCreateJPanel
		
		CFunction fnSetFramePacking = new CFunction() { public CReturn call(CObject obj) { 
			CControl ccontrol = (CControl) obj;
			String value = (String) ccontrol._("m_propvalue");
			JFrame jcontrol = (JFrame) ccontrol._("m_jcontrol");
			jcontrol.pack();
			return null;
			//return ccontrol;	
		}}; // end fnSetFramePacking()

		CFunction fnSetLookAndFeel = new CFunction() { public CReturn call(CObject obj) { 
			//CControl ccontrol = (CControl) obj;
			//String value = (String) ccontrol._("m_propvalue");
			try{ UIManager.setLookAndFeel(""); }
			catch( Exception ex ){ CLog.error(ex.toString()); }
			return null;	
		}}; // end fnSetLookAndFeel()
		
		CFunction fnSetLayout = new CFunction() { public CReturn call(CObject obj) { 
			CControl ccontrol = (CControl) obj;
			String value = (String) ccontrol._("m_propvalue");
			JFrame jcontrol = (JFrame) ccontrol._("m_jcontrol");
			Container cp = jcontrol.getContentPane();
			cp.setLayout(new GridLayout(0, 1));
			return null;
		}}; // end fnSetLayout()

		CFunction fnSetFrameClosing = new CFunction() { public CReturn call(CObject obj) { 
			CControl ccontrol = (CControl) obj;
			String value = (String) ccontrol._("m_propvalue");
			JFrame jcontrol = (JFrame) ccontrol._("m_jcontrol");
			jcontrol.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return null;	
		}}; // end fnSetFrameClosing()

		CFunction fnSetTitle = new CFunction() { public CReturn call(CObject obj) { 
			CControl ccontrol = (CControl) obj;
			String value = (String) ccontrol._("m_propvalue");
			JFrame jcontrol = (JFrame) ccontrol._("m_jcontrol");
			jcontrol.setTitle(value);
			return null;	
		}}; // end fnSetTitle()

		CFunction fnGetTitle = new CFunction() { public CReturn call(CObject obj) { 
			CControl ccontrol = (CControl) obj;
			JFrame jcontrol = (JFrame) ccontrol._("m_jcontrol");
			String value = jcontrol.getTitle();
			ccontrol._("m_propvalue", value);
			return null;	
		}}; // end fnGetTitle()

		// add instruction id to instrunction function mapping to the processor
		cprocessor._("form->create", fnCreateJFrame);
		cprocessor._("panel->create", fnCreateJPanel);
		cprocessor._("form->set->grid", fnSetLayout);
		cprocessor._("form->set->pack", fnSetFramePacking);
		cprocessor._("form->set->close", fnSetFrameClosing);
		cprocessor._("form->set->visible", cprocessor._("ccontrol->set->visible"));
		cprocessor._("form->get->visible", cprocessor._("ccontrol->get->visible"));		
		cprocessor._("form->set->title", fnSetTitle);		
		cprocessor._("form->get->title", fnGetTitle);	
	} // end CFormInstructions()
} // end CFormInstructions