//--------------------------------------------------------------
// name: CTextAreaInstructions
// desc: Multi-line text area using JTextArea
// cmds:
//   textarea->create
//   textarea->set->text / get->text
//   textarea->set->value / get->value        (aliases of text)
//   textarea->set->rows / get->rows          (int)
//   textarea->set->columns / get->columns    (int)
//   textarea->set->linewrap                  (boolean)
//   textarea->set->wrapstyleword             (boolean)
//   textarea->set->editable                  (boolean)
//   textarea->set->onchange                  (command string; fires on any text change)
//   textarea->set->visible / get->visible    (via ccontrol generic)
// params on create (optional): m_rows (Integer), m_columns (Integer), m_text (String), m_linewrap (Boolean), m_wrapstyleword (Boolean)
//--------------------------------------------------------------
package c3dclasses;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class CTextAreaInstructions extends CInstructions {
    CControlInstructions m_ccontrolinstructions;

    public CTextAreaInstructions(CProcessor cprocessor) {
        super(cprocessor);
        this.m_ccontrolinstructions = (CControlInstructions) cprocessor.getCInstructions("CControlInstrunctions");
        final CTextAreaInstructions _this = this;

        // CREATE
        CFunction fnCreate = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl control = (CControl) obj;
                final JTextArea ta = new JTextArea();
                CHash params = (CHash) control._("m_params");
                if (params != null) {
                    Object rows = params._("m_rows");
                    if (rows instanceof Number) ta.setRows(((Number) rows).intValue());
                    Object cols = params._("m_columns");
                    if (cols instanceof Number) ta.setColumns(((Number) cols).intValue());
                    Object text = params._("m_text");
                    if (text instanceof String) ta.setText((String) text);
                    Object lw = params._("m_linewrap");
                    if (lw instanceof Boolean) ta.setLineWrap((Boolean) lw);
                    Object wsw = params._("m_wrapstyleword");
                    if (wsw instanceof Boolean) ta.setWrapStyleWord((Boolean) wsw);
                }

                Object restored = CControlStateMemory.load(control);
                if (restored != null) {
                    ta.setText(String.valueOf(restored));
                }

                // Always persist textarea state, even when onchange command is not configured.
                ta.getDocument().addDocumentListener(new DocumentListener() {
                    private void persist() {
                        CControlStateMemory.save(control, ta.getText());
                    }
                    public void insertUpdate(DocumentEvent e) { persist(); }
                    public void removeUpdate(DocumentEvent e) { persist(); }
                    public void changedUpdate(DocumentEvent e) { persist(); }
                });

                m_ccontrolinstructions.createJControl(control, ta);
                return CReturn._done(control);
            }
        };

        // TEXT (set/get)
        CFunction fnSetText = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextArea ta = (JTextArea) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                ta.setText(v == null ? "" : String.valueOf(v));
                CControlStateMemory.save(c, ta.getText());
                return null;
            }
        };
        CFunction fnGetText = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextArea ta = (JTextArea) c._("m_jcontrol");
                c._("m_propvalue", ta.getText());
                return null;
            }
        };

        // ROWS/COLUMNS (set/get)
        CFunction fnSetRows = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextArea ta = (JTextArea) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                if (v instanceof Number) ta.setRows(((Number) v).intValue());
                else {
                    try { ta.setRows(Integer.parseInt(String.valueOf(v))); } catch (Exception ignored) {}
                }
                return null;
            }
        };
        CFunction fnGetRows = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextArea ta = (JTextArea) c._("m_jcontrol");
                c._("m_propvalue", ta.getRows());
                return null;
            }
        };
        CFunction fnSetColumns = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextArea ta = (JTextArea) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                if (v instanceof Number) ta.setColumns(((Number) v).intValue());
                else {
                    try { ta.setColumns(Integer.parseInt(String.valueOf(v))); } catch (Exception ignored) {}
                }
                return null;
            }
        };
        CFunction fnGetColumns = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextArea ta = (JTextArea) c._("m_jcontrol");
                c._("m_propvalue", ta.getColumns());
                return null;
            }
        };

        // LINE WRAP / WRAP STYLE WORD
        CFunction fnSetLineWrap = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextArea ta = (JTextArea) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                boolean b = false;
                if (v instanceof Boolean) b = (Boolean) v;
                else if (v != null) b = Boolean.parseBoolean(String.valueOf(v));
                ta.setLineWrap(b);
                return null;
            }
        };
        CFunction fnSetWrapStyleWord = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextArea ta = (JTextArea) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                boolean b = false;
                if (v instanceof Boolean) b = (Boolean) v;
                else if (v != null) b = Boolean.parseBoolean(String.valueOf(v));
                ta.setWrapStyleWord(b);
                return null;
            }
        };

        // EDITABLE
        CFunction fnSetEditable = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JTextArea ta = (JTextArea) c._("m_jcontrol");
                Object v = c._("m_propvalue");
                boolean editable = false;
                if (v instanceof Boolean) editable = (Boolean) v;
                else if (v != null) editable = Boolean.parseBoolean(String.valueOf(v));
                ta.setEditable(editable);
                return null;
            }
        };

        // ONCHANGE (DocumentListener)
        CFunction fnOnChange = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl c = (CControl) obj;
                final JTextArea ta = (JTextArea) c._("m_jcontrol");
                final String command = (String) c._("m_propvalue");

                Object prev = c._("m_onchange_listener");
                if (prev instanceof DocumentListener) {
                    ta.getDocument().removeDocumentListener((DocumentListener) prev);
                }

                DocumentListener dl = new DocumentListener() {
                    private void fire() {
                        CControlStateMemory.save(c, ta.getText());
                        if (command != null && !command.trim().isEmpty()) {
                            __.exec_command(command + " " + (String) c._("m_strid"));
                        }
                    }
                    public void insertUpdate(DocumentEvent e) { fire(); }
                    public void removeUpdate(DocumentEvent e) { fire(); }
                    public void changedUpdate(DocumentEvent e) { fire(); }
                };
                ta.getDocument().addDocumentListener(dl);
                c._("m_onchange_listener", dl);
                return null;
            }
        };

        // REGISTER
        cprocessor._("textarea->create", fnCreate);
        cprocessor._("textarea->set->text", fnSetText);
        cprocessor._("textarea->get->text", fnGetText);
        cprocessor._("textarea->set->value", fnSetText);
        cprocessor._("textarea->get->value", fnGetText);
        cprocessor._("textarea->set->rows", fnSetRows);
        cprocessor._("textarea->get->rows", fnGetRows);
        cprocessor._("textarea->set->columns", fnSetColumns);
        cprocessor._("textarea->get->columns", fnGetColumns);
        cprocessor._("textarea->set->linewrap", fnSetLineWrap);
        cprocessor._("textarea->set->wrapstyleword", fnSetWrapStyleWord);
        cprocessor._("textarea->set->editable", fnSetEditable);
        cprocessor._("textarea->set->onchange", fnOnChange);

        // visibility passthrough
        cprocessor._("textarea->set->visible", cprocessor._("ccontrol->set->visible"));
        cprocessor._("textarea->get->visible", cprocessor._("ccontrol->get->visible"));
    }
}
