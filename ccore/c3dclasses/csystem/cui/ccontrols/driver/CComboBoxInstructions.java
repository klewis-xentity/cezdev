//--------------------------------------------------------------
// name: CComboBoxInstructions
// desc: implements JComboBox instruction set
//--------------------------------------------------------------
package c3dclasses;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

class CComboBoxInstructions extends CInstructions {
    CControlInstructions m_ccontrolinstructions;

    public CComboBoxInstructions(CProcessor cprocessor) {
        super(cprocessor);

        final CControlInstructions ccontrolinstructions =
            this.m_ccontrolinstructions = (CControlInstructions) cprocessor.getCInstructions("CControlInstrunctions");
        final CComboBoxInstructions _this = this;

        CFunction fnCreateJComboBox = new CFunction() {
    @SuppressWarnings("unchecked")
    public CReturn call(CObject obj) {
        CControl control = (CControl) obj;

        CHash options = null;
        CHash params = (CHash) control._("m_params");
        if (params != null) options = (CHash) params._("m_options");
        
        // set the options
        control._("m_options", options);

        // the control options
        if (options != null) {
            __.print("The control options: " + options.toString());
        }

        // Expect a HashMap<String,String> of items
        HashMap<String, String> items = null;
        if (options != null) {
            items = (HashMap<String, String>) options.valueOf();
        }

        JComboBox<String> comboBox;
        if (items != null && !items.isEmpty()) {
            // Convert HashMap values (labels) to String[]
            String[] labels = items.keySet().toArray(new String[0]);
            comboBox = new JComboBox<>(labels);

            // Store the HashMap for later use (key lookup)
            control._("m_itemmap", items);
        } else {
            comboBox = new JComboBox<>(new String[]{});
        }

        // Register the Swing control
        ccontrolinstructions.createJControl(control, comboBox);

        Object restored = CControlStateMemory.load(control);
        if (restored != null) {
            String restoredValue = String.valueOf(restored);
            boolean applied = false;

            for (int i = 0; i < comboBox.getItemCount(); i++) {
                Object item = comboBox.getItemAt(i);
                if (item != null && restoredValue.equals(String.valueOf(item))) {
                    comboBox.setSelectedItem(item);
                    applied = true;
                    break;
                }
            }

            if (!applied && options != null) {
                CArray keys = options.keys();
                for (int i = 0; i < keys.length(); i++) {
                    String key = keys._string(i);
                    String value = options._string(key);
                    if (restoredValue.equals(value)) {
                        comboBox.setSelectedItem(key);
                        break;
                    }
                }
            }
        }

        // Always persist current selection for retrieval even if onchange is never configured.
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox<?> source = (JComboBox<?>) e.getSource();
                Object selected = source.getSelectedItem();
                CHash hash = (CHash) control._("m_options");
                String selectedValue = (hash != null) ? (String) hash._(selected) : null;
                String valueToStore = (selectedValue != null) ? selectedValue : (selected != null ? selected.toString() : "");
                CControlStateMemory.save(control, valueToStore);
            }
        });

        return CReturn._done(control);
    }
};

        // --- GET SELECTED ITEM ---
        CFunction fnGetSelectedItem = new CFunction() {
            public CReturn call(CObject obj) {
                CControl ccontrol = (CControl) obj;
                JComboBox<?> combo = (JComboBox<?>) ccontrol._("m_jcontrol");
                Object value = combo.getSelectedItem();
                ccontrol._("m_propvalue", value != null ? value.toString() : null);
                return null;
            }
        };

        // --- SET SELECTED ITEM ---
        CFunction fnSetSelectedItem = new CFunction() {
            public CReturn call(CObject obj) {
                CControl ccontrol = (CControl) obj;
                JComboBox<?> combo = (JComboBox<?>) ccontrol._("m_jcontrol");
                String value = (String) ccontrol._("m_propvalue");
                combo.setSelectedItem(value);
                return null;
            }
        };

        // --- ONCHANGE HANDLER ---
        CFunction fnOnChange = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl ccontrol = (CControl) obj;
                JComponent jcomponent = (JComponent) ccontrol._("m_jcontrol");
                final String baseCommand = (String) ccontrol._("m_propvalue");

                if (jcomponent instanceof JComboBox) {
                    ((JComboBox<?>) jcomponent).addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                             // The source of the event is the JComboBox
                            JComboBox<?> combo = (JComboBox<?>) e.getSource();
        
                            // Get the selected item at the moment of the event
                            Object selected = combo.getSelectedItem();
                            CHash hash = (CHash) ccontrol._("m_options");
                            String selectedValue = (hash != null) ? (String) hash._(selected) : null;
                            String valueToStore = (selectedValue != null) ? selectedValue : (selected != null ? selected.toString() : "");

                            CControlStateMemory.save(ccontrol, valueToStore);

                            if (baseCommand != null && !baseCommand.trim().isEmpty()) {
                                String command = baseCommand + " " 
                                            + (String) ccontrol._("m_strid") + " " 
                                            + selected + " " + valueToStore;
                                __.exec_command(command);
                            }

                        }
                    });
                }
                return null;
            }
        };

        // --- REGISTER COMMANDS ---
        cprocessor._("combobox->create", fnCreateJComboBox);
        cprocessor._("combobox->get->selected", fnGetSelectedItem);
        cprocessor._("combobox->set->selected", fnSetSelectedItem);
        cprocessor._("combobox->set->onchange", fnOnChange);
        cprocessor._("select->create", fnCreateJComboBox);
        cprocessor._("select->get->selected", fnGetSelectedItem);
        cprocessor._("select->set->selected", fnSetSelectedItem);
        cprocessor._("select->set->onchange", fnOnChange);
    
    }
}
