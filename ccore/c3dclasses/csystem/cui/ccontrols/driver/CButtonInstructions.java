//--------------------------------------------------------------
// name: CButtonInstructions
// desc: implements button, checkbox, and radio button instruction set
//--------------------------------------------------------------
package c3dclasses;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;

class CButtonInstructions extends CInstructions {	
    CControlInstructions m_ccontrolinstructions;
    public CButtonInstructions(CProcessor cprocessor) {
        super(cprocessor);

        // get processor instructions for controls
        final CControlInstructions ccontrolinstructions =
            this.m_ccontrolinstructions = (CControlInstructions) cprocessor.getCInstructions("CControlInstructions");

        // define button instructions 
        CFunction fnCreateJButton = new CFunction() { 
            public CReturn call(CObject obj) { 
                CControl control = (CControl) obj; 
                ccontrolinstructions.createJControl(control, new JButton((String)control._("m_value")));
                return CReturn._done(control);
            }
        };

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

        CFunction fnOnClick = new CFunction() { 
            public CReturn call(CObject obj) { 
                final CControl ccontrol = (CControl) obj;
                JComponent jcomponent = (JComponent) ccontrol._("m_jcontrol");
                if (!(jcomponent instanceof AbstractButton)) {
                    return null;
                }

                final AbstractButton button = (AbstractButton) jcomponent;
                Object prevListener = ccontrol._("m_onclick_listener");
                if (prevListener instanceof ActionListener) {
                    button.removeActionListener((ActionListener) prevListener);
                }

                Object onclickValue = ccontrol._("m_propvalue");
                ActionListener listener = null;

                if (onclickValue instanceof CFunction) {
                    final CFunction callback = (CFunction) onclickValue;
                    listener = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            callback.call(ccontrol);
                        }
                    };
                } else {
                    String commandBase = (onclickValue == null) ? "" : onclickValue.toString().trim();
                    if (commandBase.isEmpty()) {
                        return null;
                    }
                    final String command = commandBase + " " + (String) ccontrol._("m_strid");
                    listener = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            __.exec_command(command);
                        }
                    };
                }

                button.addActionListener(listener);
                ccontrol._("m_onclick_listener", listener);
                return null;
            }
        };

        // register instructions to functions for button control
        cprocessor._("button->create", fnCreateJButton);
        cprocessor._("button->set->visible", cprocessor._("ccontrol->set->visible"));
        cprocessor._("button->get->visible", cprocessor._("ccontrol->get->visible"));
        cprocessor._("button->set->title", fnSetText);		
        cprocessor._("button->get->title", fnGetText);		
        cprocessor._("button->set->text", fnSetText);		
        cprocessor._("button->get->text", fnGetText);	
        cprocessor._("button->set->onclick", fnOnClick);
    } 
}
