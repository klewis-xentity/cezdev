//--------------------------------------------------------------
// name: CButtonInstructions
// desc: implements button, checkbox, and radio button instruction set
//--------------------------------------------------------------
package c3dclasses;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

class CButtonInstructions extends CInstructions {	
    CControlInstructions m_ccontrolinstructions;
    public CButtonInstructions(CProcessor cprocessor) {
        super(cprocessor);

        final CControlInstructions ccontrolinstructions =
            this.m_ccontrolinstructions = (CControlInstructions) cprocessor.getCInstructions("CControlInstrunctions");
        final CButtonInstructions _this = this;

        CFunction fnCreateJButton = new CFunction() { 
            public CReturn call(CObject obj) { 
                CControl control = (CControl) obj; 
                ccontrolinstructions.createJControl(control, new JButton((String)control._("m_value")));
                return CReturn._done(control);
            }
        };

/*
        CFunction fnCreateJCheckbox = new CFunction() { 
            public CReturn call(CObject obj) { 
                CControl control = (CControl) obj; 
                ccontrolinstructions.createJControl(control, new JCheckBox((String)control._("m_value")));
                return CReturn._done(control);
            }
        };
/*
        CFunction fnCreateJRadioButton = new CFunction() { 
            public CReturn call(CObject obj) { 
                CControl control = (CControl) obj; 
                _this.createJRadioButton(control, new JRadioButton((String)control._("m_value")));
                return CReturn._done(control);
            }
        };
*/
        CFunction fnSetText = new CFunction() { 
            public CReturn call(CObject obj) { 
                CControl ccontrol = (CControl) obj;
                AbstractButton jcontrol = (AbstractButton) ccontrol._("m_jcontrol");
                String value = (String) ccontrol._("m_propvalue");
                jcontrol.setText(value);
                return null;
            }
        };

        CFunction fnGetText = new CFunction() { 
            public CReturn call(CObject obj) { 
                CControl ccontrol = (CControl) obj;
                AbstractButton jcontrol = (AbstractButton) ccontrol._("m_jcontrol");
                String value = jcontrol.getText();
                ccontrol._("m_propvalue", value);
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

        // --- CLICK HANDLER ---
/*
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
*/

        CFunction fnOnClick = new CFunction() { 
    public CReturn call(CObject obj) { 
        CControl ccontrol = (CControl) obj;
        JComponent jcomponent = (JComponent) ccontrol._("m_jcontrol");
        final String command = (String) ccontrol._("m_propvalue") + " " + (String) ccontrol._("m_strid");

        if (jcomponent instanceof JRadioButton && command != null && !command.isEmpty()) {
            final JRadioButton radio = (JRadioButton) jcomponent;

            // Remove any previous listener (optional)
            Object prevListener = ccontrol._("m_onclick_listener");
            if (prevListener instanceof ActionListener) {
                radio.removeActionListener((ActionListener) prevListener);
            }

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // This will fire when the radio button is selected
                    __.exec_command(command);
                }
            };
            radio.addActionListener(listener);
            ccontrol._("m_onclick_listener", listener);

            // Optional: also listen for actual mouse clicks to fire even if it’s already selected
            radio.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    __.exec_command(command);
                }
            });
        } else if (jcomponent instanceof AbstractButton && command != null && !command.isEmpty()) {
            // Buttons / checkboxes
            ((AbstractButton) jcomponent).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    __.alert("fire button....");
                    __.exec_command(command);
                }
            });
        }
        return null;
    }
};

/*
		CFunction fnOnClickCheckbox = new CFunction() { 
    public CReturn call(CObject obj) { 
        CControl ccontrol = (CControl) obj;
        JComponent jcomponent = (JComponent) ccontrol._("m_jcontrol");
		String command = (String) ccontrol._("m_propvalue");


        if (jcomponent instanceof AbstractButton) {
            AbstractButton checkbox = (AbstractButton) jcomponent;

            // Remove any previous listener to avoid duplicates (optional)
            Object prevListener = ccontrol._("m_onclick_listener");
            if (prevListener instanceof ActionListener) {
                checkbox.removeActionListener((ActionListener) prevListener);
            }

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    boolean selected = checkbox.isSelected();
                    String controlName = (String) ccontrol._("m_name");
                    if (controlName == null) controlName = "unknown";

                    // Build a specific command string including checkbox name and state
                    String command = "checkboxChanged " + controlName + " " + selected;

                    __.exec_command(command);  // execute custom command
                }
            };

            checkbox.addActionListener(listener);
            ccontrol._("m_onclick_listener", listener);
        }
        return null;
    }
	
};*/



        // --- REGISTER COMMANDS ---

//        cprocessor._("radio->create", fnCreateJRadioButton);
//        cprocessor._("checkbox->create", fnCreateJCheckbox);
        cprocessor._("button->create", fnCreateJButton);
        cprocessor._("button->set->visible", cprocessor._("ccontrol->set->visible"));
        cprocessor._("button->get->visible", cprocessor._("ccontrol->get->visible"));
        cprocessor._("button->set->title", fnSetText);		
        cprocessor._("button->get->title", fnGetText);		
        cprocessor._("button->set->text", fnSetText);		
        cprocessor._("button->get->text", fnGetText);	
//        cprocessor._("checkbox->set->selected", fnSetSelected);
//        cprocessor._("checkbox->get->selected", fnGetSelected);
//        cprocessor._("checkboxmenuitem->set->selected", fnSetSelected);
//        cprocessor._("checkboxmenuitem->get->selected", fnGetSelected);

        cprocessor._("button->set->onclick", fnOnClick);
//        cprocessor._("radio->set->onclick", fnOnClick);
//        cprocessor._("checkbox->set->onclick", fnOnClick);
    } 
/*
    // --- RADIO BUTTON CREATION ---
    Object createJRadioButton(CControl ccontrol, Component jcontrol) {
        ccontrol = (CControl) this.m_ccontrolinstructions.createJControl(ccontrol, jcontrol); 
        if(ccontrol == null)
            return null;
        CHash params = (CHash) ccontrol._("m_params");
        if(params == null)
            return null;	
        // add the radio button to the button group
        String strgroupid = (String) params._("m_strgroupid");
        if(strgroupid == null)
            return ccontrol;
        CControl container = (CControl) ccontrol._("m_container");
        if(container == null)
            return null;
        ButtonGroup buttongroup = (ButtonGroup) container._("m_btngroup-" + strgroupid);
        if(buttongroup == null) {
            __.alert("creating ButtonGroup: " + strgroupid);
            buttongroup = new ButtonGroup();
            container._("m_btngroup-" + strgroupid, buttongroup);
        }
        __.alert("added the button group");
        buttongroup.add((JRadioButton)jcontrol);
        return ccontrol;
    }
*/
}
