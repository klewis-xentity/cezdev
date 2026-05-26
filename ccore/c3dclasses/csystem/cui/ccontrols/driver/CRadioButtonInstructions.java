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
            (CControlInstructions) cprocessor.getCInstructions("CControlInstrunctions");

        CFunction fnCreateJRadioButton = new CFunction() {
            public CReturn call(CObject obj) {
                CControl control = (CControl) obj;
                createJRadioButton(control, new JRadioButton((String) control._("m_value")));
                return CReturn._done(control);
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
        cprocessor._("radio->set->onclick", fnOnClick);
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
