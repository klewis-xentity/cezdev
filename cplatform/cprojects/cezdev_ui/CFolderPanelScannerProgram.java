//-------------------------------------------------------
// name: CFolderPanelScannerProgram.java
// desc: CControls UI that scans a folder for *.cbx.json (combo), *.btn.json (button), and *.ta.json (textarea) configs
//-------------------------------------------------------
import c3dclasses.*;

import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class CFolderPanelScannerProgram {
    private static final String FORM_ID = "folder-panel-scanner";
    private static final String PATH_FIELD_ID = "scan-folder-path";
    private static final String STATUS_LABEL_ID = "scan-status";
    private volatile boolean m_isRescanning = false;
    private volatile LoadingDialogState m_loadingState = null;

    private static class LoadingDialogState {
        private final JDialog dialog;
        private final JLabel messageLabel;
        private final String displayPath;

        LoadingDialogState(JDialog dialog, JLabel messageLabel, String displayPath) {
            this.dialog = dialog;
            this.messageLabel = messageLabel;
            this.displayPath = displayPath;
        }
    }

    public static void main(String[] args) {
        final String folderPath = (args != null && args.length > 0) ? args[0] : "";
        final CFolderPanelScannerProgram program = new CFolderPanelScannerProgram();

        // Show loading dialog immediately so the user sees something right away,
        // then build controls on a background thread.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                final LoadingDialogState loadingState = program.showLoadingDialog(null, folderPath, "Preparing initial scan...");
                Thread scanThread = new Thread(new Runnable() {
                    public void run() {
                        program.updateLoadingMessage("Scanning for configuration files...");
                        program.show(folderPath);
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                if (loadingState != null && loadingState.dialog != null) {
                                    loadingState.dialog.dispose();
                                }
                            }
                        });
                    }
                });
                scanThread.setDaemon(true);
                scanThread.start();
            }
        });
    }

    private void show(String folderPath) {
        String cleanedFolderPath = this.cleanPath(folderPath);
        CControls ccontrols = this.buildControls(cleanedFolderPath);

        ccontrols.retrieve(FORM_ID).setProp("grid", "true");
        ccontrols.retrieve(FORM_ID).setProp("visible", "true");
        ccontrols.retrieve(FORM_ID).setProp("pack", "true");
        ccontrols.retrieve(FORM_ID).setProp("close", "true");
    }

    private CControls buildControls(String folderPath) {
        CControls ccontrols = new CControls();
        ccontrols.form(FORM_ID, "Combo Box Config Scanner", null);

        ccontrols.label("path-label", "Folder path", null);
        ccontrols.text(PATH_FIELD_ID, "", null);
        CControl pathControl = ccontrols.retrieve(FORM_ID + " " + PATH_FIELD_ID);
        if (pathControl != null) {
            pathControl.setProp("text", folderPath);
            this.attachPathEnterHandler(ccontrols, pathControl);
        }

        String statusMessage = this.populateControls(ccontrols, folderPath);
        ccontrols.label(STATUS_LABEL_ID, statusMessage, null);

        ccontrols.endform();
        return ccontrols;
    }

    private void attachPathEnterHandler(final CControls ccontrols, CControl pathControl) {
        Object jcontrol = this.getInternalValue(pathControl, "m_jcontrol");
        if (!(jcontrol instanceof JTextField)) {
            return;
        }

        final JTextField textField = (JTextField) jcontrol;
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (m_isRescanning) {
                    return;
                }
                m_isRescanning = true;
                String enteredPath = textField.getText();
                reopenWithPath(ccontrols, enteredPath);
            }
        });
    }

    private void reopenWithPath(CControls ccontrols, String folderPath) {
        final String cleanedFolderPath = this.cleanPath(folderPath);
        CControl formControl = ccontrols.retrieve(FORM_ID);
        final JFrame oldFrame;
        if (formControl != null) {
            Object jform = this.getInternalValue(formControl, "m_jcontrol");
            oldFrame = (jform instanceof JFrame) ? (JFrame) jform : null;
        } else {
            oldFrame = null;
        }

        if (oldFrame != null) {
            oldFrame.setTitle("Combo Box Config Scanner (Scanning...)");
        }

        final LoadingDialogState loadingState = this.showLoadingDialog(oldFrame, cleanedFolderPath, "Preparing rescan...");

        Thread scanThread = new Thread(new Runnable() {
            public void run() {
                try {
                    updateLoadingMessage("Scanning selected folder and rebuilding panels...");
                    show(cleanedFolderPath);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            if (loadingState != null && loadingState.dialog != null) {
                                loadingState.dialog.dispose();
                            }
                            if (oldFrame != null) {
                                oldFrame.dispose();
                            }
                        }
                    });
                } catch (Exception ex) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            if (loadingState != null && loadingState.dialog != null) {
                                loadingState.dialog.dispose();
                            }
                            if (oldFrame != null) {
                                oldFrame.setTitle("Combo Box Config Scanner");
                            }
                        }
                    });
                } finally {
                    m_isRescanning = false;
                }
            }
        });
        scanThread.setDaemon(true);
        scanThread.start();
    }

    private LoadingDialogState showLoadingDialog(JFrame owner, String path, String initialMessage) {
        JDialog dialog = new JDialog(owner, "Loading", false);
        dialog.setLayout(new BorderLayout(8, 8));

        String displayPath = (path == null || path.trim().equals("")) ? "(empty path)" : path;
        JLabel label = new JLabel(this.formatLoadingText(initialMessage, displayPath));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setSize(520, 110);
        dialog.setLocationRelativeTo(owner);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        LoadingDialogState state = new LoadingDialogState(dialog, label, displayPath);
        this.m_loadingState = state;
        return state;
    }

    private String formatLoadingText(String message, String displayPath) {
        String safeMessage = (message == null || message.trim().equals("")) ? "Loading..." : message;
        String safePath = (displayPath == null || displayPath.trim().equals("")) ? "(empty path)" : displayPath;
        return "<html><div style='font-family: sans-serif;'>" + safeMessage + "<br>Scanning in: " + safePath + "</div></html>";
    }

    private void updateLoadingMessage(final String message) {
        final LoadingDialogState state = this.m_loadingState;
        if (state == null || state.messageLabel == null) {
            return;
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (state.messageLabel != null) {
                    state.messageLabel.setText(formatLoadingText(message, state.displayPath));
                }
            }
        });
    }

    private Object getInternalValue(CControl control, String key) {
        if (control == null) {
            return null;
        }
        try {
            Method method = control.getClass().getMethod("_", Object.class);
            return method.invoke(control, key);
        } catch (Exception ex) {
            return null;
        }
    }

    private String populateControls(CControls ccontrols, String folderPath) {
        if (folderPath == null || folderPath.trim().equals("")) {
            ccontrols.label("empty-path", "Pass a folder path as the first program argument.", null);
            return "No folder path provided.";
        }

        File rootFolder = new File(folderPath);
        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            ccontrols.label("invalid-path", "Folder not found: " + folderPath, null);
            return "Invalid folder path.";
        }

        List<File> cbxFiles = new ArrayList<File>();
        List<File> btnFiles = new ArrayList<File>();
        List<File> taFiles = new ArrayList<File>();
        this.collectConfigFiles(rootFolder, cbxFiles, btnFiles, taFiles);
        cbxFiles.sort(Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER));
        btnFiles.sort(Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER));
        taFiles.sort(Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER));

        if (cbxFiles.isEmpty() && btnFiles.isEmpty() && taFiles.isEmpty()) {
            ccontrols.label("empty-scan", "No .cbx.json, .btn.json, or .ta.json files found in the selected folder.", null);
            return "Loaded 0 configs.";
        }

        int index = 0;
        List<File> comboOptionRoots = new ArrayList<File>();
        for (File configFile : cbxFiles) {
            ComboBoxConfig config = this.loadConfig(configFile);
            if (config.optionsFolder != null) {
                comboOptionRoots.add(config.optionsFolder);
            }
            this.addComboBoxPanel(ccontrols, config, index);
            index++;
        }

        int btnIndex = 0;
        for (File configFile : btnFiles) {
            if (this.isInsideAnyFolder(configFile, comboOptionRoots)) {
                continue;
            }
            ButtonConfig config = this.loadButtonConfig(configFile);
            this.addButtonPanel(ccontrols, config, btnIndex);
            btnIndex++;
        }

        int taIndex = 0;
        for (File configFile : taFiles) {
            if (this.isInsideAnyFolder(configFile, comboOptionRoots)) {
                continue;
            }
            TextAreaConfig config = this.loadTextAreaConfig(configFile);
            this.addTextAreaPanel(ccontrols, config, taIndex);
            taIndex++;
        }

        return "Loaded " + cbxFiles.size() + " combo(s), " + btnIndex + " button(s), " + taIndex + " text area(s).";
    }

    private boolean isInsideAnyFolder(File file, List<File> folders) {
        if (file == null || folders == null || folders.isEmpty()) {
            return false;
        }

        String filePath = file.getAbsolutePath().toLowerCase();
        for (File folder : folders) {
            if (folder == null) {
                continue;
            }

            String folderPath = folder.getAbsolutePath().toLowerCase();
            if (filePath.equals(folderPath) || filePath.startsWith(folderPath + File.separator.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void addComboBoxPanel(CControls ccontrols, ComboBoxConfig config, int index) {
        String panelId = "combo-panel-" + index;
        String selectId = "combo-select-" + index;
        String loadingId = "combo-loading-" + index;
        String panelPath = FORM_ID + " " + panelId;
        String selectPath = FORM_ID + " " + panelId + " " + selectId;
        String loadingPath = FORM_ID + " " + panelId + " " + loadingId;

        ccontrols.panel(panelId, config.controlName, null);
        ccontrols.label("combo-label-" + index, config.label, null);
        ccontrols.select(selectId, null, config.options, null);
        ccontrols.label(loadingId, "⏳ Loading...", null);

        CControl selectControl = ccontrols.retrieve(selectPath);
        CControl loadingControl = ccontrols.retrieve(loadingPath);
        
        if (loadingControl != null) {
            loadingControl.setProp("visible", "false");
        }
        
        if (selectControl != null) {
            if (config.selectedValue != null && !config.selectedValue.trim().equals("")) {
                selectControl.setProp("selected", config.selectedValue);
            }
            if (config.handlerPath != null && !config.handlerPath.trim().equals("")) {
                selectControl.setProp("onchange", config.handlerPath);
                selectControl.setProp("loadingControlId", loadingId);
            }
        }

        ccontrols.label("combo-config-path-" + index, config.configFile.getAbsolutePath(), null);
        if (config.errorMessage != null && !config.errorMessage.trim().equals("")) {
            ccontrols.label("combo-error-" + index, config.errorMessage, null);
        }

        CControl panelControl = ccontrols.retrieve(panelPath);
        this.attachSelectedFolderLoader(ccontrols, panelControl, selectControl, loadingControl, config, index);
        ccontrols.endpanel();
    }

    private void attachSelectedFolderLoader(
        final CControls ccontrols,
        CControl panelControl,
        final CControl selectControl,
        final CControl loadingControl,
        ComboBoxConfig config,
        int index
    ) {
        if (panelControl == null || selectControl == null || config.optionsFolder == null) {
            return;
        }

        Object jpanel = this.getInternalValue(panelControl, "m_jcontrol");
        Object jselect = this.getInternalValue(selectControl, "m_jcontrol");
        if (!(jpanel instanceof JPanel) || !(jselect instanceof JComboBox)) {
            return;
        }

        final JPanel panel = (JPanel) jpanel;
        final JComboBox<?> combo = (JComboBox<?>) jselect;
        final File optionsRootFolder = config.optionsFolder;
        final JPanel dynamicHost = new JPanel();
        dynamicHost.setName("dynamic-host-" + index);
        dynamicHost.setLayout(new BoxLayout(dynamicHost, BoxLayout.Y_AXIS));
        panel.add(dynamicHost);

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedFolder = getSelectedFolderPath(selectControl, combo);
                loadFolderControlsIntoPanel(selectedFolder, optionsRootFolder, dynamicHost, loadingControl);
            }
        };

        combo.addActionListener(listener);

        // Initial load based on current/remembered selection.
        String initialSelectedFolder = this.getSelectedFolderPath(selectControl, combo);
        this.loadFolderControlsIntoPanel(initialSelectedFolder, optionsRootFolder, dynamicHost, loadingControl);
    }

    private String getSelectedFolderPath(CControl selectControl, JComboBox<?> combo) {
        if (selectControl == null || combo == null) {
            return null;
        }

        Object selected = combo.getSelectedItem();
        if (selected == null) {
            return null;
        }

        CHash options = (CHash) this.getInternalValue(selectControl, "m_options");
        String mapped = (options != null) ? options._string(String.valueOf(selected)) : null;
        if (mapped != null && !mapped.trim().equals("")) {
            return mapped;
        }
        return String.valueOf(selected);
    }

    private void loadFolderControlsIntoPanel(
        final String selectedFolder,
        final File optionsRootFolder,
        final JPanel dynamicHost,
        final CControl loadingControl
    ) {
        if (dynamicHost == null) {
            return;
        }

        if (loadingControl != null) {
            loadingControl.setProp("text", "Loading child controls...");
            loadingControl.setProp("visible", "true");
        }

        Thread loadThread = new Thread(new Runnable() {
            public void run() {
                final List<ComboBoxConfig> comboConfigs = new ArrayList<ComboBoxConfig>();
                final List<ButtonConfig> buttonConfigs = new ArrayList<ButtonConfig>();
                final List<TextAreaConfig> textAreaConfigs = new ArrayList<TextAreaConfig>();
                final String[] error = new String[] { null };

                try {
                    String folderPath = cleanPath(selectedFolder);
                    if (folderPath.equals("")) {
                        error[0] = "Selected folder is empty.";
                    } else {
                        updateLoadingMessage("Scanning selected folder: " + folderPath);
                        File folder = new File(folderPath);
                        if (!folder.exists() || !folder.isDirectory()) {
                            error[0] = "Selected folder not found: " + folderPath;
                        } else {
                            if (loadingControl != null) {
                                loadingControl.setProp("text", "Scanning: " + folder.getName());
                            }

                            Map<String, File> cbxByPath = new LinkedHashMap<String, File>();
                            Map<String, File> btnByPath = new LinkedHashMap<String, File>();
                            Map<String, File> taByPath = new LinkedHashMap<String, File>();

                            List<File> cbxFiles = new ArrayList<File>();
                            List<File> btnFiles = new ArrayList<File>();
                            List<File> taFiles = new ArrayList<File>();
                            updateLoadingMessage("Walking folder contents and collecting config files...");
                            updateLoadingMessage("Finding .cbx.json, .btn.json, and .ta.json files...");
                            collectConfigFiles(folder, cbxFiles, btnFiles, taFiles);
                            updateLoadingMessage("Loading control definitions...");
                            for (File f : cbxFiles) {
                                cbxByPath.put(f.getAbsolutePath(), f);
                            }
                            for (File f : btnFiles) {
                                btnByPath.put(f.getAbsolutePath(), f);
                            }
                            for (File f : taFiles) {
                                taByPath.put(f.getAbsolutePath(), f);
                            }

                            // Also include top-level config files in the options root folder so
                            // shared controls like cenvironments/*.btn.json appear in this panel.
                            if (optionsRootFolder != null && optionsRootFolder.exists() && optionsRootFolder.isDirectory()) {
                                List<File> rootCbx = new ArrayList<File>();
                                List<File> rootBtn = new ArrayList<File>();
                                List<File> rootTa = new ArrayList<File>();
                                collectDirectConfigFiles(optionsRootFolder, rootCbx, rootBtn, rootTa);
                                for (File f : rootCbx) {
                                    cbxByPath.put(f.getAbsolutePath(), f);
                                }
                                for (File f : rootBtn) {
                                    btnByPath.put(f.getAbsolutePath(), f);
                                }
                                for (File f : rootTa) {
                                    taByPath.put(f.getAbsolutePath(), f);
                                }
                            }

                            cbxFiles = new ArrayList<File>(cbxByPath.values());
                            btnFiles = new ArrayList<File>(btnByPath.values());
                            taFiles = new ArrayList<File>(taByPath.values());
                            updateLoadingMessage("Rendering loaded controls...");
                            cbxFiles.sort(Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER));
                            btnFiles.sort(Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER));
                            taFiles.sort(Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER));

                            for (File file : cbxFiles) {
                                comboConfigs.add(loadConfig(file));
                            }
                            for (File file : btnFiles) {
                                buttonConfigs.add(loadButtonConfig(file));
                            }
                            for (File file : taFiles) {
                                textAreaConfigs.add(loadTextAreaConfig(file));
                            }

                            if (comboConfigs.isEmpty() && buttonConfigs.isEmpty() && textAreaConfigs.isEmpty()) {
                                error[0] = "No .cbx.json, .btn.json, or .ta.json found in selected folder.";
                            }
                        }
                    }
                } catch (Exception ex) {
                    error[0] = "Failed to load selected folder controls.";
                }

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        renderDynamicControls(dynamicHost, comboConfigs, buttonConfigs, textAreaConfigs, error[0]);
                        if (loadingControl != null) {
                            loadingControl.setProp("text", "Loading complete");
                            loadingControl.setProp("visible", "false");
                        }
                    }
                });
            }
        });
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void collectDirectConfigFiles(File folder, List<File> cbxFiles, List<File> btnFiles, List<File> taFiles) {
        File[] entries = folder.listFiles();
        if (entries == null) {
            return;
        }

        Arrays.sort(entries, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File entry : entries) {
            if (!entry.isFile()) {
                continue;
            }
            String lower = entry.getName().toLowerCase();
            if (lower.endsWith(".cbx.json")) {
                cbxFiles.add(entry);
            } else if (lower.endsWith(".btn.json")) {
                btnFiles.add(entry);
            } else if (lower.endsWith(".ta.json")) {
                taFiles.add(entry);
            }
        }
    }

    private void renderDynamicControls(
        JPanel host,
        List<ComboBoxConfig> comboConfigs,
        List<ButtonConfig> buttonConfigs,
        List<TextAreaConfig> textAreaConfigs,
        String errorMessage
    ) {
        host.removeAll();

        if (errorMessage != null && !errorMessage.trim().equals("")) {
            host.add(new JLabel(errorMessage));
        }

        for (ComboBoxConfig cfg : comboConfigs) {
            this.addDynamicComboPanel(host, cfg);
        }

        for (ButtonConfig cfg : buttonConfigs) {
            this.addDynamicButtonControl(host, cfg);
        }

        for (TextAreaConfig cfg : textAreaConfigs) {
            this.addDynamicTextAreaControl(host, cfg);
        }

        host.revalidate();
        host.repaint();
    }

    private void addDynamicComboPanel(JPanel host, final ComboBoxConfig config) {
        final JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));

        comboPanel.add(new JLabel(config.label));

        final Map<String, String> labelToValue = this.toLabelValueMap(config.options);
        String[] labels = labelToValue.keySet().toArray(new String[0]);
        final JComboBox<String> combo = new JComboBox<String>(labels);
        final JPanel childHost = new JPanel();
        childHost.setLayout(new BoxLayout(childHost, BoxLayout.Y_AXIS));

        if (config.selectedValue != null && !config.selectedValue.trim().equals("")) {
            String selectedLabel = this.findLabelForValue(labelToValue, config.selectedValue);
            if (selectedLabel != null) {
                combo.setSelectedItem(selectedLabel);
            }
        }

        comboPanel.add(combo);
        comboPanel.add(childHost);

        if (config.handlerPath != null && !config.handlerPath.trim().equals("")) {
            combo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object selected = combo.getSelectedItem();
                    String selectedLabel = (selected == null) ? "" : String.valueOf(selected);
                    String selectedValue = labelToValue.containsKey(selectedLabel)
                        ? labelToValue.get(selectedLabel)
                        : selectedLabel;
                    String command = config.handlerPath + " "
                        + quoteArg(config.controlName) + " "
                        + quoteArg(selectedLabel) + " "
                        + quoteArg(selectedValue);
                    __.exec_command(command);
                }
            });
        }

        if (config.optionsFolder != null) {
            combo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String selectedFolder = labelToValue.containsKey(String.valueOf(combo.getSelectedItem()))
                        ? labelToValue.get(String.valueOf(combo.getSelectedItem()))
                        : String.valueOf(combo.getSelectedItem());
                    loadFolderControlsIntoPanel(selectedFolder, config.optionsFolder, childHost, null);
                }
            });

            String selectedFolder = this.getSelectedFolderPathFromMap(combo, labelToValue);
            this.loadFolderControlsIntoPanel(selectedFolder, config.optionsFolder, childHost, null);
        }

        if (config.errorMessage != null && !config.errorMessage.trim().equals("")) {
            comboPanel.add(new JLabel(config.errorMessage));
        }

        host.add(comboPanel);
    }

    private String getSelectedFolderPathFromMap(JComboBox<String> combo, Map<String, String> labelToValue) {
        if (combo == null) {
            return null;
        }

        Object selected = combo.getSelectedItem();
        if (selected == null) {
            return null;
        }

        String selectedLabel = String.valueOf(selected);
        if (labelToValue != null && labelToValue.containsKey(selectedLabel)) {
            return labelToValue.get(selectedLabel);
        }
        return selectedLabel;
    }

    private void addDynamicButtonControl(JPanel host, final ButtonConfig config) {
        JButton button = new JButton(config.label);
        if (config.handlerPath != null && !config.handlerPath.trim().equals("")) {
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    __.exec_command(config.handlerPath + " " + quoteArg(config.controlName));
                }
            });
        }
        host.add(button);

        if (config.errorMessage != null && !config.errorMessage.trim().equals("")) {
            host.add(new JLabel(config.errorMessage));
        }
    }

    private void addDynamicTextAreaControl(JPanel host, TextAreaConfig config) {
        JPanel textAreaPanel = new JPanel();
        textAreaPanel.setLayout(new BoxLayout(textAreaPanel, BoxLayout.Y_AXIS));
        textAreaPanel.add(new JLabel(config.label));

        JTextArea textArea = new JTextArea(config.value, config.rows, config.columns);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(config.editable);
        textAreaPanel.add(new JScrollPane(textArea));

        if (config.errorMessage != null && !config.errorMessage.trim().equals("")) {
            textAreaPanel.add(new JLabel(config.errorMessage));
        }

        host.add(textAreaPanel);
    }

    private Map<String, String> toLabelValueMap(CHash options) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        if (options == null) {
            return map;
        }

        CArray keys = options.keys();
        for (int i = 0; i < keys.length(); i++) {
            String key = keys._string(i);
            map.put(key, options._string(key));
        }
        return map;
    }

    private String findLabelForValue(Map<String, String> labelToValue, String value) {
        if (labelToValue == null || value == null) {
            return null;
        }

        for (Map.Entry<String, String> entry : labelToValue.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void collectConfigFiles(File folder, List<File> cbxFiles, List<File> btnFiles, List<File> taFiles) {
        File[] entries = folder.listFiles();
        if (entries == null) {
            return;
        }

        Arrays.sort(entries, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File entry : entries) {
            if (entry.isDirectory()) {
                this.collectConfigFiles(entry, cbxFiles, btnFiles, taFiles);
            } else if (entry.isFile()) {
                String lower = entry.getName().toLowerCase();
                if (lower.endsWith(".cbx.json")) {
                    cbxFiles.add(entry);
                } else if (lower.endsWith(".btn.json")) {
                    btnFiles.add(entry);
                } else if (lower.endsWith(".ta.json")) {
                    taFiles.add(entry);
                }
            }
        }
    }

    private ComboBoxConfig loadConfig(File configFile) {
        ComboBoxConfig config = new ComboBoxConfig();
        config.configFile = configFile;
        config.controlName = this.getControlName(configFile.getName());
        config.label = config.controlName;
        config.options = __.chash();

        String json = __.get_file_contents(configFile.getAbsolutePath());
        if (json == null || json.trim().equals("")) {
            config.errorMessage = "Unable to read config file.";
            return config;
        }

        CHash parsed = CJSON.decode(json);
        if (parsed == null) {
            config.errorMessage = "Invalid JSON config.";
            return config;
        }

        config.label = this.firstNonEmpty(parsed._string("label"), config.controlName);
        config.selectedValue = this.firstNonEmpty(parsed._string("selected"), parsed._string("value"));

        String optionsFolder = this.firstNonEmpty(
            parsed._string("optionsFromFolders"),
            parsed._string("optionsFromDirectoryPath")
        );
        if (optionsFolder != null && !optionsFolder.trim().equals("")) {
            config.optionsFolder = this.resolvePath(configFile.getParentFile(), optionsFolder);
        }

        String handler = parsed._string("handler");
        if (handler != null && !handler.trim().equals("")) {
            File handlerFile = this.resolvePath(configFile.getParentFile(), handler);
            if (handlerFile != null) {
                config.handlerPath = this.toHandlerCommand(handlerFile);
            }
        }

        config.options = this.loadOptions(config.optionsFolder);

        if (config.optionsFolder == null) {
            config.errorMessage = "Missing optionsFromFolders or optionsFromDirectoryPath.";
        } else if (!config.optionsFolder.exists() || !config.optionsFolder.isDirectory()) {
            config.errorMessage = "Options folder not found.";
        } else if (config.options.keys().length() == 0) {
            config.errorMessage = "Options folder has no subfolders.";
        }

        return config;
    }

    private CHash loadOptions(File optionsFolder) {
        CHash options = __.chash();
        if (optionsFolder == null || !optionsFolder.exists() || !optionsFolder.isDirectory()) {
            return options;
        }

        File[] subFolders = optionsFolder.listFiles(File::isDirectory);
        if (subFolders == null) {
            return options;
        }

        Arrays.sort(subFolders, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File subFolder : subFolders) {
            options.set(subFolder.getName(), subFolder.getAbsolutePath());
        }

        return options;
    }

    private File resolvePath(File baseFolder, String path) {
        String cleanedPath = this.cleanPath(path);
        if (cleanedPath.equals("")) {
            return null;
        }

        File file = new File(cleanedPath);
        if (file.isAbsolute()) {
            return file;
        }

        return new File(baseFolder, cleanedPath);
    }



    private String cleanPath(String path) {
        if (path == null) {
            return "";
        }

        String trimmed = path.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        return trimmed;
    }

    private String firstNonEmpty(String first, String second) {
        if (first != null && !first.trim().equals("")) {
            return first;
        }
        return second;
    }

    private String quoteArg(String value) {
        String safeValue = (value == null) ? "" : value.replace("\"", "\\\"");
        return "\"" + safeValue + "\"";
    }

    private String toHandlerCommand(File handlerFile) {
        String absolutePath = handlerFile.getAbsolutePath();
        String lowerName = absolutePath.toLowerCase();
        if (lowerName.endsWith(".bat") || lowerName.endsWith(".cmd")) {
            return "call " + this.quoteArg(absolutePath);
        }
        return this.quoteArg(absolutePath);
    }

    private void addButtonPanel(CControls ccontrols, ButtonConfig config, int index) {
        String buttonId = "btn-control-" + index;
        String buttonPath = FORM_ID + " " + buttonId;

        ccontrols.button(buttonId, config.label, null);

        CControl buttonControl = ccontrols.retrieve(buttonPath);
        if (buttonControl != null && config.handlerPath != null && !config.handlerPath.trim().equals("")) {
            buttonControl.setProp("onclick", config.handlerPath);
        }

        if (config.errorMessage != null && !config.errorMessage.trim().equals("")) {
            ccontrols.label("btn-error-" + index, config.errorMessage, null);
        }
    }

    private void addTextAreaPanel(CControls ccontrols, TextAreaConfig config, int index) {
        String textAreaId = "textarea-control-" + index;
        String textAreaPath = FORM_ID + " " + textAreaId;

        ccontrols.label("textarea-label-" + index, config.label, null);
        ccontrols.textarea(textAreaId, config.value, null);

        CControl textAreaControl = ccontrols.retrieve(textAreaPath);
        if (textAreaControl != null) {
            textAreaControl.setProp("rows", String.valueOf(config.rows));
            textAreaControl.setProp("columns", String.valueOf(config.columns));
            textAreaControl.setProp("editable", String.valueOf(config.editable));
        }

        if (config.errorMessage != null && !config.errorMessage.trim().equals("")) {
            ccontrols.label("textarea-error-" + index, config.errorMessage, null);
        }
    }

    private ButtonConfig loadButtonConfig(File configFile) {
        ButtonConfig config = new ButtonConfig();
        config.configFile = configFile;
        config.controlName = this.getControlName(configFile.getName());
        config.label = config.controlName;

        String json = __.get_file_contents(configFile.getAbsolutePath());
        if (json == null || json.trim().equals("")) {
            config.errorMessage = "Unable to read config file.";
            return config;
        }

        CHash parsed = CJSON.decode(json);
        if (parsed == null) {
            config.errorMessage = "Invalid JSON config.";
            return config;
        }

        config.label = this.firstNonEmpty(parsed._string("label"), config.controlName);

        String handler = parsed._string("handler");
        if (handler != null && !handler.trim().equals("")) {
            File handlerFile = this.resolvePath(configFile.getParentFile(), handler);
            if (handlerFile != null) {
                config.handlerPath = this.toHandlerCommand(handlerFile);
            }
        }

        return config;
    }

    private TextAreaConfig loadTextAreaConfig(File configFile) {
        TextAreaConfig config = new TextAreaConfig();
        config.configFile = configFile;
        config.controlName = this.getControlName(configFile.getName());
        config.label = config.controlName;
        config.value = "";
        config.rows = 8;
        config.columns = 48;
        config.editable = true;

        String json = __.get_file_contents(configFile.getAbsolutePath());
        if (json == null || json.trim().equals("")) {
            config.errorMessage = "Unable to read config file.";
            return config;
        }

        CHash parsed = CJSON.decode(json);
        if (parsed == null) {
            config.errorMessage = "Invalid JSON config.";
            return config;
        }

        config.label = this.firstNonEmpty(parsed._string("label"), config.controlName);
        config.value = this.firstNonEmpty(parsed._string("value"), parsed._string("text"));

        String valueFromFile = parsed._string("valueFromFile");
        if (valueFromFile != null && !valueFromFile.trim().equals("")) {
            File valueFile = this.resolvePath(configFile.getParentFile(), valueFromFile);
            String fileValue = (valueFile == null) ? null : __.get_file_contents(valueFile.getAbsolutePath());
            if (fileValue == null) {
                config.errorMessage = "Unable to read valueFromFile.";
            } else {
                config.value = fileValue;
            }
        }

        config.rows = this.parsePositiveInt(parsed._string("rows"), config.rows);
        config.columns = this.parsePositiveInt(parsed._string("columns"), config.columns);
        String editable = parsed._string("editable");
        if (editable != null && !editable.trim().equals("")) {
            config.editable = !editable.equalsIgnoreCase("false");
        }

        return config;
    }

    private int parsePositiveInt(String value, int fallback) {
        if (value == null || value.trim().equals("")) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return (parsed > 0) ? parsed : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String getControlName(String fileName) {
        if (fileName == null) {
            return "control";
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".cbx.json")) {
            return fileName.substring(0, fileName.length() - ".cbx.json".length());
        }
        if (lower.endsWith(".btn.json")) {
            return fileName.substring(0, fileName.length() - ".btn.json".length());
        }
        if (lower.endsWith(".ta.json")) {
            return fileName.substring(0, fileName.length() - ".ta.json".length());
        }
        return fileName;
    }

    private static class ButtonConfig {
        private String controlName;
        private String label;
        private String errorMessage;
        private String handlerPath;
        private File configFile;
    }

    private static class ComboBoxConfig {
        private String controlName;
        private String label;
        private String selectedValue;
        private String errorMessage;
        private String handlerPath;
        private File configFile;
        private File optionsFolder;
        private CHash options;
    }

    private static class TextAreaConfig {
        private String controlName;
        private String label;
        private String value;
        private String errorMessage;
        private File configFile;
        private int rows;
        private int columns;
        private boolean editable;
    }
}
