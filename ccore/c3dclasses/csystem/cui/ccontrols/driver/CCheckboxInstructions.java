//--------------------------------------------------------------
// name: CCheckboxInstructions
// desc: Instruction set for JCheckBox (checkbox) and checkbox menu item state helpers
//--------------------------------------------------------------
package c3dclasses;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

class CCheckboxInstructions extends CInstructions {
    CControlInstructions m_ccontrolinstructions;

    public CCheckboxInstructions(CProcessor cprocessor) {
        super(cprocessor);

        this.m_ccontrolinstructions =
            (CControlInstructions) cprocessor.getCInstructions("CControlInstrunctions");

        CFunction fnCreateJCheckbox = new CFunction() {
            public CReturn call(CObject obj) {
                CControl control = (CControl) obj;
                JCheckBox checkbox = new JCheckBox((String) control._("m_value"));
                Object restored = CControlStateMemory.load(control);
                if (restored != null) {
                    if (restored instanceof Boolean) checkbox.setSelected((Boolean) restored);
                    else checkbox.setSelected(Boolean.parseBoolean(String.valueOf(restored)));
                }

                // Always persist checkbox state on user toggle, even when onclick/onchange is not configured.
                checkbox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JCheckBox source = (JCheckBox) e.getSource();
                        CControlStateMemory.save(control, source.isSelected());
                    }
                });

                m_ccontrolinstructions.createJControl(control, checkbox);
                return CReturn._done(control);
            }
        };

        CFunction fnSetSelected = new CFunction() {
            public CReturn call(CObject obj) {
                CControl ccontrol = (CControl) obj;
                AbstractButton jcontrol = (AbstractButton) ccontrol._("m_jcontrol");
                Boolean value = (Boolean) ccontrol._("m_propvalue");
                jcontrol.setSelected(value != null && value);
                CControlStateMemory.save(ccontrol, jcontrol.isSelected());
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
                final String baseCommand = (String) ccontrol._("m_propvalue");

                if (jcomponent instanceof AbstractButton) {
                    ((AbstractButton) jcomponent).addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            AbstractButton button = (AbstractButton) e.getSource();
                            boolean selected = button.isSelected();
                            CControlStateMemory.save(ccontrol, selected);

                            if (baseCommand != null && !baseCommand.trim().isEmpty()) {
                                __.exec_command(baseCommand + " " + (String) ccontrol._("m_strid"));
                            }
                        }
                    });
                }
                return null;
            }
        };

        cprocessor._("checkbox->create", fnCreateJCheckbox);
        cprocessor._("checkbox->set->selected", fnSetSelected);
        cprocessor._("checkbox->get->selected", fnGetSelected);
        cprocessor._("checkbox->set->onclick", fnOnClick);
        cprocessor._("checkbox->set->onchange", fnOnClick);
    }
}
