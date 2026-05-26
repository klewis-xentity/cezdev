//--------------------------------------------------------------
// name: CToggleInstructions
// desc: Two-state Toggle control using JToggleButton
// cmds:
//   toggle->create
//   toggle->set->text          / toggle->get->text
//   toggle->set->selected      / toggle->get->selected
//   toggle->set->onchange      (fires when state changes)
//   toggle->set->onclick       (fires on click action)
//   toggle->set->onlabel       (text when selected=true)
//   toggle->set->offlabel      (text when selected=false)
//   toggle->set->visible       / toggle->get->visible (via ccontrol generic)
// params on create (optional):
//   m_text (String), m_selected (Boolean), m_onlabel (String), m_offlabel (String)
//--------------------------------------------------------------
package c3dclasses;

import javax.swing.JToggleButton;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

class CToggleInstructions extends CInstructions {
    CControlInstructions m_ccontrolinstructions;

    public CToggleInstructions(CProcessor cprocessor) {
        super(cprocessor);
        this.m_ccontrolinstructions = (CControlInstructions) cprocessor.getCInstructions("CControlInstrunctions");
        final CToggleInstructions _this = this;

        // CREATE
        CFunction fnCreate = new CFunction() {
            public CReturn call(CObject obj) {
                CControl control = (CControl) obj;
                CHash params = (CHash) control._("m_params");

                String text = control._("m_value") instanceof String ? (String) control._("m_value") : null;
                if (params != null && params._("m_text") instanceof String) {
                    text = (String) params._("m_text");
                }
                if (text == null) text = "";

                JToggleButton toggle = new JToggleButton(text);

                if (params != null) {
                    Object sel = params._("m_selected");
                    if (sel instanceof Boolean) toggle.setSelected((Boolean) sel);

                    Object onlabel = params._("m_onlabel");
                    if (onlabel instanceof String) control._("m_onlabel", (String) onlabel);
                    Object offlabel = params._("m_offlabel");
                    if (offlabel instanceof String) control._("m_offlabel", (String) offlabel);
                }

                // Save and apply labels if provided
                _this.updateLabelForState(control, toggle);

                m_ccontrolinstructions.createJControl(control, toggle);
                return CReturn._done(control);
            }
        };

        // TEXT
        CFunction fnSetText = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JToggleButton t = (JToggleButton) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                t.setText(v == null ? "" : String.valueOf(v));
                return null;
            }
        };
        CFunction fnGetText = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JToggleButton t = (JToggleButton) c._("m_jcontrol");
                c._("m_propvalue", t.getText());
                return null;
            }
        };

        // SELECTED
        CFunction fnSetSelected = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JToggleButton t = (JToggleButton) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                boolean sel = false;
                if (v instanceof Boolean) sel = (Boolean) v;
                else if (v != null) sel = Boolean.parseBoolean(String.valueOf(v));
                t.setSelected(sel);
                _this.updateLabelForState(c, t);
                return null;
            }
        };
        CFunction fnGetSelected = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JToggleButton t = (JToggleButton) c._("m_jcontrol");
                c._("m_propvalue", t.isSelected());
                return null;
            }
        };

        // ONCHANGE (fires when selection state changes)
        CFunction fnOnChange = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl c = (CControl) obj;
                final JToggleButton t = (JToggleButton) c._("m_jcontrol");
                final String command = (String) c._("m_propvalue");

                // remove previous
                Object prev = c._("m_onchange_listener");
                if (prev instanceof ItemListener) {
                    t.removeItemListener((ItemListener) prev);
                }

                if (command != null && !command.trim().isEmpty()) {
                    ItemListener il = new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            _this.updateLabelForState(c, t);
                            __.exec_command(command + " " + (String) c._("m_strid"));
                        }
                    };
                    t.addItemListener(il);
                    c._("m_onchange_listener", il);
                }
                return null;
            }
        };

        // ONCLICK (fires on action performed)
        CFunction fnOnClick = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl c = (CControl) obj;
                final JToggleButton t = (JToggleButton) c._("m_jcontrol");
                final String command = (String) c._("m_propvalue");

                // remove previous
                Object prev = c._("m_onclick_listener");
                if (prev instanceof ActionListener) {
                    t.removeActionListener((ActionListener) prev);
                }

                if (command != null && !command.trim().isEmpty()) {
                    ActionListener al = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            _this.updateLabelForState(c, t);
                            __.exec_command(command + " " + (String) c._("m_strid"));
                        }
                    };
                    t.addActionListener(al);
                    c._("m_onclick_listener", al);
                }
                return null;
            }
        };

        // ONLABEL / OFFLABEL
        CFunction fnSetOnLabel = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JToggleButton t = (JToggleButton) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                c._("m_onlabel", v == null ? null : String.valueOf(v));
                _this.updateLabelForState(c, t);
                return null;
            }
        };
        CFunction fnSetOffLabel = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JToggleButton t = (JToggleButton) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                c._("m_offlabel", v == null ? null : String.valueOf(v));
                _this.updateLabelForState(c, t);
                return null;
            }
        };

        // REGISTER COMMANDS
        cprocessor._("toggle->create", fnCreate);
        cprocessor._("toggle->set->text", fnSetText);
        cprocessor._("toggle->get->text", fnGetText);
        cprocessor._("toggle->set->selected", fnSetSelected);
        cprocessor._("toggle->get->selected", fnGetSelected);
        cprocessor._("toggle->set->onchange", fnOnChange);
        cprocessor._("toggle->set->onclick", fnOnClick);
        cprocessor._("toggle->set->onlabel", fnSetOnLabel);
        cprocessor._("toggle->set->offlabel", fnSetOffLabel);

        // visibility passthrough
        cprocessor._("toggle->set->visible", cprocessor._("ccontrol->set->visible"));
        cprocessor._("toggle->get->visible", cprocessor._("ccontrol->get->visible"));
    }

    // Helper to update button text based on selected state and optional on/off labels
    private void updateLabelForState(CControl c, JToggleButton t) {
        Object on = c._("m_onlabel");
        Object off = c._("m_offlabel");
        if (on instanceof String || off instanceof String) {
            boolean sel = t.isSelected();
            String label = sel ? (on instanceof String ? (String) on : t.getText())
                               : (off instanceof String ? (String) off : t.getText());
            t.setText(label);
        }
    }
}
