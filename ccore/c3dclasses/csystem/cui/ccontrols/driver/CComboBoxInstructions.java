//--------------------------------------------------------------
// name: CComboBoxInstructions
// desc: implements JComboBox instruction set
//--------------------------------------------------------------
package c3dclasses;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import java.io.File;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

class CComboBoxInstructions extends CInstructions {
    CControlInstructions m_ccontrolinstructions;
    private static CControls s_ccontrols = null;

    public CComboBoxInstructions(CProcessor cprocessor) {
        super(cprocessor);

        final CControlInstructions ccontrolinstructions =
            this.m_ccontrolinstructions = (CControlInstructions) cprocessor.getCInstructions("CControlInstructions");
        final CComboBoxInstructions _this = this;

        CFunction fnCreateJComboBox = new CFunction() {
    @SuppressWarnings("unchecked")
    public CReturn call(CObject obj) {
        CControl control = (CControl) obj;

        CHash options = null;
        CHash params = (CHash) control._("m_params");
        if (params != null) options = (CHash) params._("m_options");
        control._("m_component", "select");
        
        // set the options
        control._("m_options", options);

        CHash restoredState = CControlStateMemory.loadState(control);
        String restoredSelectedOption = null;
        String restoredValue = null;
        if (restoredState != null) {
            Object restoredOption = restoredState._("m_selectedoption");
            restoredSelectedOption = restoredOption == null ? null : String.valueOf(restoredOption);

            Object restored = restoredState._("m_value");
            restoredValue = restored == null ? null : String.valueOf(restored);

            if ((options == null || options.keys().length() == 0) && restoredState._("m_options") instanceof CHash) {
                options = (CHash) restoredState._("m_options");
                control._("m_options", options);
            }

            if ((restoredValue == null || restoredValue.trim().isEmpty()) && options != null
                    && restoredSelectedOption != null && options.containsKey(restoredSelectedOption)) {
                restoredValue = options._string(restoredSelectedOption);
            }
        }

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

        boolean applied = false;
        if (restoredSelectedOption != null && !restoredSelectedOption.trim().isEmpty()) {
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                Object item = comboBox.getItemAt(i);
                if (item != null && restoredSelectedOption.equals(String.valueOf(item))) {
                    comboBox.setSelectedItem(item);
                    applied = true;
                    break;
                }
            }
        }

        if (!applied && restoredValue != null) {
            String normalizedRestored = normalizePathValue(restoredValue);

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
                    if (restoredValue.equals(value) || normalizedRestored.equals(normalizePathValue(value))) {
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
                renderSelectedDirectoryPanel(control, selected, valueToStore);
            }
        });

        Object initiallySelected = comboBox.getSelectedItem();
        CHash initialHash = (CHash) control._("m_options");
        String initialSelectedValue = (initialHash != null) ? initialHash._string(String.valueOf(initiallySelected)) : null;
        String initialPath = (initialSelectedValue != null) ? initialSelectedValue
                : (initiallySelected == null ? "" : String.valueOf(initiallySelected));
        renderSelectedDirectoryPanel(control, initiallySelected, initialPath);

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
                boolean applied = false;
                CHash options = (CHash) ccontrol._("m_options");

                if (value != null && options != null) {
                    String normalizedValue = normalizePathValue(value);
                    CArray keys = options.keys();
                    for (int i = 0; i < keys.length(); i++) {
                        String key = keys._string(i);
                        String mappedValue = options._string(key);
                        if (value.equals(key) || value.equals(mappedValue)
                                || normalizedValue.equals(normalizePathValue(mappedValue))) {
                            combo.setSelectedItem(key);
                            applied = true;
                            break;
                        }
                    }
                }

                if (!applied) {
                    combo.setSelectedItem(value);
                }

                Object selected = combo.getSelectedItem();
                String selectedValue = (options != null) ? options._string(String.valueOf(selected)) : null;
                String valueToStore = (selectedValue != null) ? selectedValue : (selected != null ? selected.toString() : "");
                CControlStateMemory.save(ccontrol, valueToStore);
                return null;
            }
        };

        // --- SET LOADING CONTROL ID ---
        CFunction fnSetLoadingControlId = new CFunction() {
            public CReturn call(CObject obj) {
                CControl ccontrol = (CControl) obj;
                String loadingControlId = (String) ccontrol._("m_propvalue");
                ccontrol._("m_loadingControlId", loadingControlId);
                return null;
            }
        };

        // --- ONCHANGE HANDLER ---
        CFunction fnOnChange = new CFunction() {
            public CReturn call(CObject obj) {
                final CControl ccontrol = (CControl) obj;
                JComponent jcomponent = (JComponent) ccontrol._("m_jcontrol");
                final String baseCommand = (String) ccontrol._("m_propvalue");
                final String loadingControlId = (String) ccontrol._("m_loadingControlId");

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

                            // Show loading indicator if available
                            if (loadingControlId != null && !loadingControlId.trim().isEmpty()) {
                                try {
                                    CControls ccontrols = CComboBoxInstructions.getCControls();
                                    if (ccontrols != null) {
                                        ccontrols.retrieve(loadingControlId).updateProp("visible", "true");
                                    }
                                } catch (Exception ex) {
                                    __.print("Failed to show loading control: " + ex.getMessage());
                                }
                            }

                            if (baseCommand != null && !baseCommand.trim().isEmpty()) {
                                String command = baseCommand + " " 
                                            + quoteArg((String) ccontrol._("m_strid")) + " " 
                                            + quoteArg(selected != null ? selected.toString() : "") + " " 
                                            + quoteArg(valueToStore);
                                __.exec_command(command);
                            }

                            // Hide loading indicator after 2 seconds in a background thread
                            if (loadingControlId != null && !loadingControlId.trim().isEmpty()) {
                                new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            Thread.sleep(2000);
                                            CControls ccontrols = CComboBoxInstructions.getCControls();
                                            if (ccontrols != null) {
                                                ccontrols.retrieve(loadingControlId).updateProp("visible", "false");
                                            }
                                        } catch (Exception ex) {
                                            __.print("Failed to hide loading control: " + ex.getMessage());
                                        }
                                    }
                                }).start();
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
        cprocessor._("combobox->set->loadingControlId", fnSetLoadingControlId);
        cprocessor._("combobox->set->onchange", fnOnChange);
        cprocessor._("select->create", fnCreateJComboBox);
        cprocessor._("select->get->selected", fnGetSelectedItem);
        cprocessor._("select->set->selected", fnSetSelectedItem);
        cprocessor._("select->set->loadingControlId", fnSetLoadingControlId);
        cprocessor._("select->set->onchange", fnOnChange);
    
    }

    private static String quoteArg(String value) {
        String safeValue = (value == null) ? "" : value.replace("\"", "\\\"");
        return "\"" + safeValue + "\"";
    }

    private static String normalizePathValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace("\\", "/").toLowerCase(Locale.ROOT);
    }

    private static void renderSelectedDirectoryPanel(CControl ccontrol, Object selectedLabel, String selectedPath) {
        if (ccontrol == null || selectedPath == null || selectedPath.trim().isEmpty()) {
            return;
        }

        Object marker = ccontrol._("m_options_from_directory");
        if (marker != null && !"true".equalsIgnoreCase(String.valueOf(marker))) {
            return;
        }

        File selectedDirectory = new File(selectedPath);
        if (!selectedDirectory.exists() || !selectedDirectory.isDirectory()) {
            return;
        }

        CControls ccontrols = (CControls) ccontrol._("m_ccontrols");
        String controlPath = (String) ccontrol._("m_strpathid");
        if (ccontrols == null || controlPath == null) {
            return;
        }

        int lastSpace = controlPath.lastIndexOf(" ");
        if (lastSpace <= 0) {
            return;
        }

        String parentPath = controlPath.substring(0, lastSpace);
        String panelId = "panel-dir-" + shortHash(selectedDirectory.getAbsolutePath());
        String panelPath = parentPath + " " + panelId;
        String previousPanelPath = (String) ccontrol._("m_selected_directory_panel_path");

        if (previousPanelPath != null && !previousPanelPath.equals(panelPath)) {
            deleteControlTree(ccontrols, previousPanelPath);
        }

        if (ccontrols.retrieve(panelPath) == null) {
            String panelLabel = selectedLabel == null ? selectedDirectory.getName() : String.valueOf(selectedLabel);
            CControlsSchema schema = new CControlsSchema();
            schema.createCControlsFromPathIntoContainer(selectedDirectory.getAbsolutePath(), ccontrols,
                    parentPath, panelId, panelLabel);
        }

        CControl panel = ccontrols.retrieve(panelPath);
        if (panel != null) {
            panel.setProp("visible", "true");
            panel.setProp("grid", "true");
            ensurePanelAfterOwnerControl(ccontrol, panel);
            ccontrol._("m_selected_directory_panel_path", panelPath);
        }
    }

    private static void ensurePanelAfterOwnerControl(CControl ownerControl, CControl panelControl) {
        if (ownerControl == null || panelControl == null) {
            return;
        }

        Object ownerComponentObj = ownerControl._("m_jcontrol");
        Object panelComponentObj = panelControl._("m_jcontrol");
        if (!(ownerComponentObj instanceof Component) || !(panelComponentObj instanceof Component)) {
            return;
        }

        Component ownerComponent = (Component) ownerComponentObj;
        Component panelComponent = (Component) panelComponentObj;
        Container parent = ownerComponent.getParent();
        if (parent == null || panelComponent.getParent() != parent) {
            return;
        }

        int ownerIndex = parent.getComponentZOrder(ownerComponent);
        int panelIndex = parent.getComponentZOrder(panelComponent);
        if (ownerIndex < 0 || panelIndex < 0) {
            return;
        }

        int desiredIndex = ownerIndex + 1;
        int maxIndex = parent.getComponentCount() - 1;
        if (desiredIndex > maxIndex) {
            desiredIndex = maxIndex;
        }

        if (panelIndex != desiredIndex) {
            parent.setComponentZOrder(panelComponent, desiredIndex);
            parent.revalidate();
            parent.repaint();
        }
    }

    private static void deleteControlTree(CControls ccontrols, String rootPath) {
        if (ccontrols == null || rootPath == null || rootPath.trim().isEmpty()) {
            return;
        }

        CControl rootControl = ccontrols.retrieve(rootPath);
        if (rootControl == null) {
            ccontrols.getCControls().remove(rootPath);
            return;
        }

        CArray keys = ccontrols.getCControls().keys();
        ArrayList<String> descendants = new ArrayList<String>();
        String prefix = rootPath + " ";
        for (int i = 0; i < keys.length(); i++) {
            String key = String.valueOf(keys._(i));
            if (key != null && key.startsWith(prefix)) {
                descendants.add(key);
            }
        }

        Collections.sort(descendants, new Comparator<String>() {
            public int compare(String a, String b) {
                return b.length() - a.length();
            }
        });

        for (String key : descendants) {
            CControl child = ccontrols.retrieve(key);
            if (child != null) {
                child.delete();
            }
            ccontrols.getCControls().remove(key);
        }

        Object rootJControl = rootControl._("m_jcontrol");
        Object rootContainer = rootControl._("m_container");
        Object parentJControl = (rootContainer instanceof CControl) ? ((CControl) rootContainer)._("m_jcontrol") : null;
        if (rootJControl instanceof Component && parentJControl instanceof Container) {
            Container parent = (Container) parentJControl;
            parent.remove((Component) rootJControl);
            parent.revalidate();
            parent.repaint();
        }

        rootControl.delete();
        ccontrols.getCControls().remove(rootPath);
    }

    private static String shortHash(String text) {
        if (text == null) {
            return "0";
        }
        int hash = text.hashCode();
        if (hash == Integer.MIN_VALUE) {
            hash = 0;
        }
        hash = Math.abs(hash);
        return Integer.toString(hash, 36).toUpperCase(Locale.ROOT);
    }

    public static void setCControls(CControls ccontrols) {
        s_ccontrols = ccontrols;
    }

    private static CControls getCControls() {
        return s_ccontrols;
    }
}
