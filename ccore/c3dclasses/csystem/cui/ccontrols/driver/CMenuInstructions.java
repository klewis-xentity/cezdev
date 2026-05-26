//--------------------------------------------------------------
// name: CMenuInstructions
// desc: implements the form control
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
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import java.applet.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import javax.swing.border.*;

//--------------------------------------------------------
// name: CMenuInstructions
// desc: implements the form control
//--------------------------------------------------------
class CMenuInstructions extends CInstructions {	
	public CMenuInstructions(CProcessor cprocessor) {
		super(cprocessor);
		CFunction fnCreateJMenuBar = new CFunction() { public CReturn call(CObject obj) { 
			CControl control = (CControl) obj; 
			CMenuInstructions.createJMenuBar(control, new JMenuBar());
			return CReturn._done(control);
		}}; // end fnCreateJMenuBar
	
	 	CFunction fnCreateJMenu = new CFunction() { public CReturn call(CObject obj) { 
			CControl control = (CControl) obj; 
			CMenuInstructions.createJMenu(control, new JMenu((String)control._("m_value")));
			return CReturn._done(control);
		}}; // end fnCreateJMenu
	 
	 	CFunction fnCreateJMenuItem = new CFunction() { public CReturn call(CObject obj) { 
			CControl control = (CControl) obj; 
			CMenuInstructions.createJMenuItem(control, new JMenuItem((String)control._("m_value")));
			return CReturn._done(control);
		}}; // end fnCreateJMenuItem()

		CFunction fnCreateJCheckBoxMenuItem = new CFunction() { public CReturn call(CObject obj) {
			CControl control = (CControl) obj;
			CMenuInstructions.createMenuItemCheckbox(control, new JCheckBoxMenuItem((String)control._("m_value")));
			return CReturn._done(control);
		}};
		
		CFunction fnAddJMenuSeperator = new CFunction() { public CReturn call(CObject obj) { 
			CMenuInstructions.addJMenuSeperator((CControl)obj);
			return null;
		}}; // end fnAddMenuSeperator
		
		CFunction fnDoClick = new CFunction() { public CReturn call(CObject obj) { 
			CControl control = (CControl) obj;
			JMenu jcontrol = (JMenu) control._("m_jcontrol");
			jcontrol.doClick();
			return null;
		}}; // end fnDoClick
		
		CFunction fnGetItemCount = new CFunction() { public CReturn call(CObject obj) { 
			CControl ccontrol = (CControl) obj;
			JMenu jcontrol = (JMenu) ccontrol._("m_jcontrol");
			ccontrol._("m_propvalue", jcontrol.getItemCount());
			return null;
		}}; // end fnGetItemCount
		
		CFunction fnDeleteItem = new CFunction() { public CReturn call(CObject obj) { 
			//menu.remove(index);
			return null;
		}}; // end fnDeleteItem
		
		CFunction fnDeleteAllItems = new CFunction() { public CReturn call(CObject obj) { 
			//menu.removeAll();
			return null;
		}}; // end fnDeleteItem
		
		CFunction fnOnClickMenuItem = new CFunction() { 
			public CReturn call(CObject obj) { 
				CControl ccontrol = (CControl) obj;
				JComponent jcomponent = (JComponent) ccontrol._("m_jcontrol");
				//String command = (String) ccontrol._("m_propvalue");

				// Build the command string dynamically
				String command = (String) ccontrol._("m_propvalue") + " " 
							+ (String) ccontrol._("m_strid");
				

				if (jcomponent instanceof JMenuItem && command != null && !command.isEmpty()) {
					((JMenuItem) jcomponent).addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							__.exec_command(command); // Executes the string as a command
						}
					});
				}
				return null;
			}
		};

		CFunction fnSetSelected = new CFunction() {
            public CReturn call(CObject obj) {
                CControl ccontrol = (CControl) obj;
                AbstractButton jcontrol = (AbstractButton) ccontrol._("m_jcontrol");
                Boolean value = (Boolean) ccontrol._("m_propvalue");
                jcontrol.setSelected(value != null && value);
                return null;
            }
        };

        CFunction fnGetSelected = new CFunction() {
            public CReturn call(CObject obj) {
                CControl ccontrol = (CControl) obj;
                AbstractButton jcontrol = (AbstractButton) ccontrol._("m_jcontrol");
                ccontrol._("m_propvalue", jcontrol.isSelected());
                return null;
            }
        };

		CFunction fnOnClick = new CFunction() { 
            public CReturn call(CObject obj) { 
                CControl ccontrol = (CControl) obj;
                JComponent jcomponent = (JComponent) ccontrol._("m_jcontrol");
                final String command = (String) ccontrol._("m_propvalue") + " " + (String) ccontrol._("m_strid");
                if (jcomponent instanceof AbstractButton && command != null && !command.isEmpty()) {
                    ((AbstractButton) jcomponent).addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            __.exec_command(command); // Executes the string as a command
                        }
                    });
                }
                return null;
            }
        };
		
		// register the instructions
		cprocessor._("menubar->create", fnCreateJMenuBar);	
		cprocessor._("menu->create", fnCreateJMenu);
		cprocessor._("menu->get->itemcount", fnGetItemCount);
		cprocessor._("menu->set->removeItems", fnDeleteAllItems);
		cprocessor._("menuitem->create", fnCreateJMenuItem);
		cprocessor._("menuitem->delete", fnDeleteItem);
		cprocessor._("menuitem-seperator->create", fnAddJMenuSeperator);
		cprocessor._("menuitem->set->click", fnDoClick);
		cprocessor._("menuitem->set->onclick", fnOnClickMenuItem);
		cprocessor._("menuitem-checkbox->create", fnCreateJCheckBoxMenuItem);
		cprocessor._("menuitem-checkbox->set->onclick", fnOnClickMenuItem);
		cprocessor._("menuitem-checkbox->set->selected", fnSetSelected);
        cprocessor._("menuitem-checkbox->get->selected", fnGetSelected); 
	} // end CMenuInstructions()
	
	public static Object createJMenuBar(CControl ccontrol, JMenuBar jcontrol) {
		__.alert("create menu bar");
		if(jcontrol == null)
			return null;
		ccontrol._("m_jcontrol", jcontrol);	
		JFrame parent = (JFrame) CControlInstructions.getParentContainer(ccontrol);
		if(parent != null) {
			parent.setJMenuBar(jcontrol);
			__.alert("add menu bar");
		}
		return ccontrol;
	} // end createJMenuBar()

	public static Object createJMenu(CControl ccontrol, Component jcontrol) {
		if(jcontrol == null)
			return null;
		ccontrol._("m_jcontrol", jcontrol);	
		((JMenu)jcontrol).addMenuListener(new CMenuEventHandlers());
		Container parent = (Container) CControlInstructions.getParentContainer(ccontrol);
		if(parent != null)
			parent.add(jcontrol);
		return ccontrol;
	} // end createJMenu()
	
	public static Object createJMenuItem(CControl ccontrol, JMenuItem jcontrol) {
		if(jcontrol == null)
			return null;
		ccontrol._("m_jcontrol", jcontrol);	
		JMenu parent = (JMenu) CControlInstructions.getParentContainer(ccontrol);
		if(parent != null)
			parent.add(jcontrol);
		return ccontrol;
	} // end createJMenuItem()
		
    public static Object createMenuItemCheckbox(CControl ccontrol, JCheckBoxMenuItem jcontrol) {
        if(jcontrol == null)
            return null;
        ccontrol._("m_jcontrol", jcontrol);	
        JMenu parent = (JMenu) CControlInstructions.getParentContainer(ccontrol);
        if(parent != null)
            parent.add(jcontrol);
        return ccontrol;
    } // end createCheckboxMenuItem()

	public static CControl addJMenuSeperator(CControl ccontrol){
		if(ccontrol == null)
			return null;
		ccontrol._("m_jcontrol",null);
		JMenu parent = (JMenu) CControlInstructions.getParentContainer(ccontrol);
		if(parent != null)
			parent.addSeparator();
		return ccontrol;
	} // end addJMenuItemSeperator()
} // end CMenuInstructions

//------------------------------------------------------------------------
// name: CMenuEventHandlers
// desc: defines the event handlers for menu 
//------------------------------------------------------------------------
class CMenuEventHandlers implements MenuListener {
    public void menuSelected(MenuEvent e) {
		System.out.println("menuSelected");
    } // end menuSelected()
	
	public void menuDeselected(MenuEvent e) {
		System.out.println("menuDeselected");
	} // end menuDeselected()
	
	public void menuCanceled(MenuEvent e) {
		System.out.println("menuCanceled");
	} // end menuCanceled()
} // end CMenuEventHandlers