//--------------------------------------------------------------
// name: CTextInputInstructions
// desc: Single-line text input using JTextField
// cmds:
//   text->create
//   text->set->text / get->text
//   text->set->value / get->value        (aliases of text)
//   text->set->columns / get->columns    (int)
//   text->set->editable                  (boolean)
//   text->set->onchange                  (command string; fires on text change)
//   text->set->onenter                   (command string; fires on ENTER)
//   text->set->visible / get->visible    (via ccontrol generic)
// params on create (optional): m_columns (Integer), m_text (String)
//--------------------------------------------------------------
package c3dclasses;

import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

class CTextInputInstructions extends CInstructions {
    CControlInstructions m_ccontrolinstructions;

    public CTextInputInstructions(CProcessor cprocessor) {
        super(cprocessor);
        this.m_ccontrolinstructions = (CControlInstructions) cprocessor.getCInstructions("CControlInstrunctions");
        final CTextInputInstructions _this = this;
CFunction fnCreate = new CFunction() {
    public CReturn call(CObject obj) {
        final CControl control = (CControl) obj;

        final JTextField tf = new JTextField();
        tf.setText("foo-boo"); // initialize with value

        // Expand to take available width
        tf.setPreferredSize(new Dimension(400, tf.getPreferredSize().height));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, tf.getPreferredSize().height));

        Object restored = CControlStateMemory.load(control);
        if (restored != null) {
            tf.setText(String.valueOf(restored));
        }

        // Always persist text state, even when onchange command is not configured.
        tf.getDocument().addDocumentListener(new DocumentListener() {
            private void persist() {
                CControlStateMemory.save(control, tf.getText());
            }
            public void insertUpdate(DocumentEvent e) { persist(); }
            public void removeUpdate(DocumentEvent e) { persist(); }
            public void changedUpdate(DocumentEvent e) { persist(); }
        });

        m_ccontrolinstructions.createJControl(control, tf);
        return CReturn._done(control);
    }
};

        // TEXT (set/get)
        CFunction fnSetText = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextField tf = (JTextField) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                tf.setText(v == null ? "" : String.valueOf(v));
                CControlStateMemory.save(c, tf.getText());
                return null;
            }
        };
        CFunction fnGetText = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextField tf = (JTextField) c._("m_jcontrol");
                c._("m_propvalue", tf.getText());
                return null;
            }
        };

        // COLUMNS (set/get)
        CFunction fnSetColumns = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextField tf = (JTextField) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                if (v instanceof Number) tf.setColumns(((Number) v).intValue());
                else {
                    try { tf.setColumns(Integer.parseInt(String.valueOf(v))); } catch (Exception ignored) {}
                }
                return null;
            }
        };
        CFunction fnGetColumns = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextField tf = (JTextField) c._("m_jcontrol");
                c._("m_propvalue", tf.getColumns());
                return null;
            }
        };

        // EDITABLE
        CFunction fnSetEditable = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextField tf = (JTextField) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                boolean editable = false;
                if (v instanceof Boolean) editable = (Boolean) v;
                else if (v != null) editable = Boolean.parseBoolean(String.valueOf(v));
                tf.setEditable(editable);
                return null;
            }
        };

        // ONCHANGE (DocumentListener)
        CFunction fnOnChange = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl c = (CControl) obj;
                final JTextField tf = (JTextField) c._("m_jcontrol");
                final String command = (String) c._("m_propvalue");

                // remove previous
                Object prev = c._("m_onchange_listener");
                if (prev instanceof DocumentListener) {
                    tf.getDocument().removeDocumentListener((DocumentListener) prev);
                }
                DocumentListener dl = new DocumentListener() {
                    private void fire() {
                        CControlStateMemory.save(c, tf.getText());
                        if (command != null && !command.trim().isEmpty()) {
                            __.exec_command(command + " " + (String) c._("m_strid"));
                        }
                    }
                    public void insertUpdate(DocumentEvent e) { fire(); }
                    public void removeUpdate(DocumentEvent e) { fire(); }
                    public void changedUpdate(DocumentEvent e) { fire(); }
                };
                tf.getDocument().addDocumentListener(dl);
                c._("m_onchange_listener", dl);
                return null;
            }
        };

        // ONENTER (ActionListener)
        CFunction fnOnEnter = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl c = (CControl) obj;
                final JTextField tf = (JTextField) c._("m_jcontrol");
                final String command = (String) c._("m_propvalue");

                // remove previous
                Object prev = c._("m_onenter_listener");
                if (prev instanceof ActionListener) tf.removeActionListener((ActionListener) prev);

                if (command != null && !command.trim().isEmpty()) {
                    ActionListener al = new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            __.exec_command(command + " " + (String) c._("m_strid"));
                        }
                    };
                    tf.addActionListener(al);
                    c._("m_onenter_listener", al);
                }
                return null;
            }
        };

        // REGISTER
        cprocessor._("text->create", fnCreate);
        cprocessor._("text->set->text", fnSetText);
        cprocessor._("text->get->text", fnGetText);
        cprocessor._("text->set->value", fnSetText);
        cprocessor._("text->get->value", fnGetText);
        cprocessor._("text->set->columns", fnSetColumns);
        cprocessor._("text->get->columns", fnGetColumns);
        cprocessor._("text->set->editable", fnSetEditable);
        cprocessor._("text->set->onchange", fnOnChange);
        cprocessor._("text->set->onenter", fnOnEnter);

        // visibility passthrough
        cprocessor._("text->set->visible", cprocessor._("ccontrol->set->visible"));
        cprocessor._("text->get->visible", cprocessor._("ccontrol->get->visible"));
    }
}
