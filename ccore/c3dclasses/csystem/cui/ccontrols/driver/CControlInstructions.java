//--------------------------------------------------------------
// name: CControlInstructions
// desc: implements control instruction set
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
// name: CControlInstructions
// desc: implements control instruction set
//--------------------------------------------------------
public class CControlInstructions extends CInstructions {	
	public CControlInstructions(CProcessor cprocessor) {	
		super(cprocessor);
		// instruction function
		
		CFunction fnSetVisible = new CFunction() { public CReturn call(CObject obj) { 
			CControl ccontrol = (CControl) obj;
			String value = (String) ccontrol._("m_propvalue");
			Component jcontrol = (Component) ccontrol._("m_jcontrol");
			jcontrol.setVisible(Boolean.parseBoolean(value));
			return null;	
		}}; // end fnSetVisible()
			
		CFunction fnGetVisible = new CFunction() { public CReturn call(CObject obj) { 
			CControl ccontrol = (CControl) obj;
			Component jcontrol = (Component) ccontrol._("m_jcontrol");
			String value = Boolean.toString(jcontrol.isVisible());
			ccontrol._("m_propvalue", value);
			return null;	
		}}; // end fnGetVisible()
		
		CFunction fnSetFont = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.setFont();
			return null;	
		}}; // end fnSetVisible()
			
		CFunction fnGetFont = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.getFont();
			return null;	
		}}; // end fnGetVisible()
		
		CFunction fnSetEnable = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.setEnable(boolean b);
			return null;	
		}}; // end fnSetVisible()
			
		CFunction fnGetEnable = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.getFont();
			return null;	
		}}; // end fnGetVisible()
		
		CFunction fnSetIcon = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.setIcon(boolean b);
			return null;	
		}}; // end fnSetVisible()
			
		CFunction fnGetIcon = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.getIcon();
			return null;	
		}}; // end fnGetVisible()
		
		CFunction fnSetText = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.setEnable(boolean b);
			return null;	
		}}; // end fnSetVisible()
			
		CFunction fnGetText = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.getFont();
			return null;	
		}}; // end fnGetVisible()
		
		CFunction fnSetHorizontalAlignment = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.setEnable(boolean b);
			return null;	
		}}; // end fnSetVisible()
			
		CFunction fnGetHorizontalAlignment = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.getFont();
			return null;	
		}}; // end fnGetVisible()
		
		
		CFunction fnSetVerticalAlignment = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.setEnable(boolean b);
			return null;	
		}}; // end fnSetVisible()
			
		CFunction fnGetVerticalAlignment = new CFunction() { public CReturn call(CObject obj) { 
			//jcontrol.getFont();
			return null;	
		}}; // end fnGetVisible()

		
        CFunction fnOnInit = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl ccontrol = (CControl) obj;
                ccontrol._("m_oninit_command", ccontrol._("m_propvalue"));
                return null;
            } // end call()
        }; // end fnOnInit()

        CFunction fnOnDeInit = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl ccontrol = (CControl) obj;
                ccontrol._("m_ondeinit_command", ccontrol._("m_propvalue"));
                return null;
            } // end call()
        }; // end fnOnDeInit()

        CFunction fnDoOnInit = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl ccontrol = (CControl) obj;
                String strcommand = (String) ccontrol._("m_oninit_command");
                if(strcommand != null)
                    __.exec_command(strcommand + " " + (String) ccontrol._("m_strid"));
                return null;
            } // end call() 
        }; // end fnDoOnInit()

        CFunction fnDoOnDeInit = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl ccontrol = (CControl) obj;
                String strcommand = (String) ccontrol._("m_ondeinit_command");
                if(strcommand != null)
                    __.exec_command(strcommand + " " + (String) ccontrol._("m_strid"));
                return null;
            } // end call()
        }; // end fnDoOnInit()

		// add instruction id to instrunction function mapping to the processor
		cprocessor._("ccontrol->set->visible", fnSetVisible);
		cprocessor._("ccontrol->get->visible", fnGetVisible);	
		cprocessor._("ccontrol->set->font", fnSetFont);
		cprocessor._("ccontrol->get->font", fnGetFont);
		cprocessor._("ccontrol->set->oninit", fnOnInit);
        cprocessor._("ccontrol->set->ondeinit", fnOnDeInit);
   		cprocessor._("ccontrol->get->oninit", fnDoOnInit);
        cprocessor._("ccontrol->get->ondeinit", fnDoOnDeInit);
   } // end CControlDriverImplementor()
	
	// instruction set helper functions
	public static Object createJControl(CControl ccontrol, Component jcontrol) {
		if(jcontrol == null)
			return null;
		ccontrol._("m_jcontrol", jcontrol);	
		Container parent = (Container) CControlInstructions.getParentContainer(ccontrol);
		if(parent != null)
			parent.add(jcontrol);
		return ccontrol;
	} // end createJControl()
	
	public static Object getParentContainer(CControl ccontrol) {
		if(ccontrol == null)
			return null;
		CControl container = (CControl) ccontrol._("m_container");
		if(container == null)
			return null;
		return (Object) container._("m_jcontrol");	
	} // end getParentContainer()
} // end CControlInstructions


//------------------------------------------------------------------------
// name: CActionEventHandlers
// desc: 
//------------------------------------------------------------------------
class CActionEventHandlers implements ActionListener {
    public void actionPerformed(ActionEvent e) {
		System.out.println("CActionEventHandlers");
    } // end menuSelected()
} // end CActionEventHandlers