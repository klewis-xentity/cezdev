//--------------------------------------------------------------
// name: CRadioButtonInstructions
// desc: Instruction set for JRadioButton (radio button) only
//--------------------------------------------------------------
package c3dclasses;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

class CRadioButtonInstructions extends CInstructions {
    CControlInstructions m_ccontrolinstructions;

    public CRadioButtonInstructions(CProcessor cprocessor) {
        super(cprocessor);

        this.m_ccontrolinstructions =
            (CControlInstructions) cprocessor.getCInstructions("CControlInstructions");

        CFunction fnCreateJRadioButton = new CFunction() {
            public CReturn call(CObject obj) {
                CControl control = (CControl) obj;
                control._("m_component", "radio");
                final JRadioButton radio = new JRadioButton((String) control._("m_value"));
                Object restored = CControlStateMemory.load(control);
                if (restored != null) {
                    radio.setSelected(Boolean.parseBoolean(String.valueOf(restored)));
                }
                radio.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        CControlStateMemory.save(control, radio.isSelected());
                    }
                });
                createJRadioButton(control, radio);
                return CReturn._done(control);
            }
        };

        CFunction fnSetSelected = new CFunction() {
            public CReturn call(CObject obj) {
                CControl ccontrol = (CControl) obj;
                JRadioButton jcontrol = (JRadioButton) ccontrol._("m_jcontrol");
                Object value = ccontrol._("m_propvalue");
                boolean selected = value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value));
                jcontrol.setSelected(selected);
                CControlStateMemory.save(ccontrol, selected);
                return null;
            }
        };

        CFunction fnGetSelected = new CFunction() {
            public CReturn call(CObject obj) {
                CControl ccontrol = (CControl) obj;
                JRadioButton jcontrol = (JRadioButton) ccontrol._("m_jcontrol");
                ccontrol._("m_propvalue", jcontrol.isSelected());
                return null;
            }
        };

        CFunction fnOnClick = new CFunction() {
            public CReturn call(CObject obj) {
                CControl ccontrol = (CControl) obj;
                JComponent jcomponent = (JComponent) ccontrol._("m_jcontrol");
                final String command = (String) ccontrol._("m_propvalue") + " " + (String) ccontrol._("m_strid");

                if (jcomponent instanceof JRadioButton && command != null && !command.isEmpty()) {
                    final JRadioButton radio = (JRadioButton) jcomponent;

                    // Remove any previous listener (if we stored one)
                    Object prevListener = ccontrol._("m_onclick_listener");
                    if (prevListener instanceof ActionListener) {
                        radio.removeActionListener((ActionListener) prevListener);
                    }

                    ActionListener listener = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            __.exec_command(command);
                        }
                    };
                    radio.addActionListener(listener);
                    ccontrol._("m_onclick_listener", listener);

                    // Optional: fire on mouse click even if already selected
                    radio.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            __.exec_command(command);
                        }
                    });
                }
                return null;
            }
        };

        // --- REGISTER ---
        cprocessor._("radio->create", fnCreateJRadioButton);
        cprocessor._("radio->set->selected", fnSetSelected);
        cprocessor._("radio->get->selected", fnGetSelected);
        cprocessor._("radio->set->onclick", fnOnClick);
        cprocessor._("radio->set->visible", cprocessor._("ccontrol->set->visible"));
        cprocessor._("radio->get->visible", cprocessor._("ccontrol->get->visible"));
    }

    // --- RADIO BUTTON GROUPING HELPER ---
    Object createJRadioButton(CControl ccontrol, Component jcontrol) {
        ccontrol = (CControl) this.m_ccontrolinstructions.createJControl(ccontrol, jcontrol);
        if (ccontrol == null)
            return null;

        CHash params = (CHash) ccontrol._("m_params");
        if (params == null)
            return null;

        // Add the radio button to the designated ButtonGroup (scoped to container)
        String strgroupid = (String) params._("m_strgroupid");
        if (strgroupid == null)
            return ccontrol;

        CControl container = (CControl) ccontrol._("m_container");
        if (container == null)
            return null;

        ButtonGroup buttongroup = (ButtonGroup) container._("m_btngroup-" + strgroupid);
        if (buttongroup == null) {
            __.alert("creating ButtonGroup: " + strgroupid);
            buttongroup = new ButtonGroup();
            container._("m_btngroup-" + strgroupid, buttongroup);
        }
        __.alert("added the button group");
        buttongroup.add((JRadioButton) jcontrol);
        return ccontrol;
    }
}
