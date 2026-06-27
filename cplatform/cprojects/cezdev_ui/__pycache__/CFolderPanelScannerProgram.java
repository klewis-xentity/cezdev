//-------------------------------------------------------
// name: CFolderPanelScannerProgram.java
// desc: Swing UI that scans a folder for *.cbx.json configs and loads each one as a combo box
//-------------------------------------------------------
package c3dclasses;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class CFolderPanelScannerProgram {
    private static final Pattern STRING_FIELD_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");

    private final JFrame m_frame;
    private final JTextField m_folderPathField;
    private final JPanel m_resultsPanel;
    private final JLabel m_statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String initialPath = (args != null && args.length > 0) ? args[0] : "";
            new CFolderPanelScannerProgram(initialPath).showUi();
        });
    }

    public CFolderPanelScannerProgram(String initialPath) {
        this.m_frame = new JFrame("Combo Box Config Scanner");
        this.m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.m_frame.setLayout(new BorderLayout(12, 12));

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBorder(new EmptyBorder(12, 12, 0, 12));

        JPanel inputRow = new JPanel(new BorderLayout(8, 8));
        inputRow.add(new JLabel("Folder path:"), BorderLayout.WEST);

        this.m_folderPathField = new JTextField(initialPath == null ? "" : initialPath);
        inputRow.add(this.m_folderPathField, BorderLayout.CENTER);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> this.chooseFolder());

        JButton scanButton = new JButton("Scan");
        scanButton.addActionListener(e -> this.scanAndRender());

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.add(browseButton);
        actionRow.add(scanButton);

        topPanel.add(inputRow, BorderLayout.CENTER);
        topPanel.add(actionRow, BorderLayout.EAST);

        this.m_resultsPanel = new JPanel();
        this.m_resultsPanel.setLayout(new BoxLayout(this.m_resultsPanel, BoxLayout.Y_AXIS));
        this.m_resultsPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(this.m_resultsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(760, 520));

        this.m_statusLabel = new JLabel("Choose a folder and click Scan.");
        this.m_statusLabel.setBorder(new EmptyBorder(0, 12, 12, 12));

        this.m_frame.add(topPanel, BorderLayout.NORTH);
        this.m_frame.add(scrollPane, BorderLayout.CENTER);
        this.m_frame.add(this.m_statusLabel, BorderLayout.SOUTH);
    }

    private void showUi() {
        this.m_frame.pack();
        this.m_frame.setLocationRelativeTo(null);
        this.m_frame.setVisible(true);
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        String currentPath = this.cleanPath(this.m_folderPathField.getText());
        if (!currentPath.isEmpty()) {
            chooser.setCurrentDirectory(new File(currentPath));
        }

        if (chooser.showOpenDialog(this.m_frame) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (selected != null) {
                this.m_folderPathField.setText(selected.getAbsolutePath());
            }
        }
    }

    private void scanAndRender() {
        String folderPath = this.cleanPath(this.m_folderPathField.getText());
        if (folderPath.isEmpty()) {
            this.setStatus("Enter a folder path first.");
            this.showEmptyState("No folder selected.");
            return;
        }

        File rootFolder = new File(folderPath);
        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            this.setStatus("Folder not found: " + folderPath);
            this.showEmptyState("Invalid folder path.");
            return;
        }

        List<File> configFiles = new ArrayList<File>();
        this.collectComboConfigFiles(rootFolder, configFiles);
        configFiles.sort(Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER));

        this.m_resultsPanel.removeAll();

        if (configFiles.isEmpty()) {
            this.showEmptyState("No .cbx.json files found.");
            this.setStatus("Scanned 0 combo box configs in " + rootFolder.getAbsolutePath());
            return;
        }

        int renderedCount = 0;
        for (File configFile : configFiles) {
            ComboBoxConfig config = this.loadConfig(configFile);
            this.m_resultsPanel.add(this.createComboBoxRow(config));
            this.m_resultsPanel.add(Box.createVerticalStrut(10));
            renderedCount++;
        }

        this.m_resultsPanel.revalidate();
        this.m_resultsPanel.repaint();
        this.setStatus("Loaded " + renderedCount + " combo box config(s) from " + rootFolder.getAbsolutePath());
    }

    private void collectComboConfigFiles(File folder, List<File> configFiles) {
        File[] entries = folder.listFiles();
        if (entries == null) {
            return;
        }

        Arrays.sort(entries, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File entry : entries) {
            if (entry.isDirectory()) {
                this.collectComboConfigFiles(entry, configFiles);
            } else if (entry.isFile() && entry.getName().toLowerCase().endsWith(".cbx.json")) {
                configFiles.add(entry);
            }
        }
    }

    private JPanel createComboBoxRow(ComboBoxConfig config) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(config.controlName),
            new EmptyBorder(6, 8, 8, 8)
        ));

        JLabel label = new JLabel(config.label + ":");
        panel.add(label, BorderLayout.WEST);

        JComboBox<String> comboBox = new JComboBox<String>();
        comboBox.setPreferredSize(new Dimension(320, comboBox.getPreferredSize().height));

        for (ComboOption option : config.options) {
            comboBox.addItem(option.label);
        }

        if (config.options.isEmpty()) {
            comboBox.addItem("No options available");
            comboBox.setEnabled(false);
        } else {
            this.applyInitialSelection(comboBox, config);
        }

        comboBox.addActionListener(e -> {
            ComboOption selected = this.getSelectedOption(config, comboBox.getSelectedItem());
            if (selected == null) {
                return;
            }

            this.setStatus("Loaded " + config.controlName + " as a combo box. Selected " + selected.label + ".");
            if (config.handler != null) {
                this.executeHandler(config, selected);
            }
        });

        panel.add(comboBox, BorderLayout.CENTER);

        if (config.errorMessage != null && !config.errorMessage.isEmpty()) {
            JLabel errorLabel = new JLabel(config.errorMessage);
            errorLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
            panel.add(errorLabel, BorderLayout.SOUTH);
        }

        return panel;
    }

    private void applyInitialSelection(JComboBox<String> comboBox, ComboBoxConfig config) {
        if (config.selectedValue == null || config.selectedValue.isEmpty()) {
            comboBox.setSelectedIndex(0);
            return;
        }

        for (ComboOption option : config.options) {
            if (config.selectedValue.equals(option.label) || config.selectedValue.equals(option.value)) {
                comboBox.setSelectedItem(option.label);
                return;
            }
        }

        comboBox.setSelectedIndex(0);
    }

    private ComboOption getSelectedOption(ComboBoxConfig config, Object selectedItem) {
        if (selectedItem == null) {
            return null;
        }

        String label = String.valueOf(selectedItem);
        for (ComboOption option : config.options) {
            if (label.equals(option.label)) {
                return option;
            }
        }

        return null;
    }

    private void executeHandler(ComboBoxConfig config, ComboOption selected) {
        try {
            String command = this.quote(config.handler.getAbsolutePath())
                + " " + this.quote(config.controlName)
                + " " + this.quote(selected.label)
                + " " + this.quote(selected.value);

            ProcessBuilder builder;
            if (this.isWindows()) {
                builder = new ProcessBuilder("cmd.exe", "/c", command);
            } else {
                builder = new ProcessBuilder("sh", "-c", command);
            }

            builder.directory(config.configFile.getParentFile());
            builder.start();
        } catch (IOException ex) {
            this.setStatus("Handler failed for " + config.controlName + ": " + ex.getMessage());
        }
    }

    private ComboBoxConfig loadConfig(File configFile) {
        ComboBoxConfig config = new ComboBoxConfig();
        config.configFile = configFile;
        config.controlName = this.getControlName(configFile.getName());
        config.label = config.controlName;

        try {
            String json = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
            config.label = this.firstNonEmpty(this.readStringField(json, "label"), config.controlName);
            config.selectedValue = this.firstNonEmpty(this.readStringField(json, "selected"), this.readStringField(json, "value"));

            String optionsFolder = this.firstNonEmpty(
                this.readStringField(json, "optionsFromFolders"),
                this.readStringField(json, "optionsFromDirectoryPath")
            );
            if (optionsFolder != null) {
                config.optionsFolder = this.resolvePath(configFile.getParentFile(), optionsFolder);
            }

            String handler = this.readStringField(json, "handler");
            if (handler != null && !handler.isEmpty()) {
                config.handler = this.resolvePath(configFile.getParentFile(), handler);
            }

            config.options = this.loadOptions(config.optionsFolder);

            if (config.optionsFolder == null) {
                config.errorMessage = "Missing optionsFromFolders or optionsFromDirectoryPath.";
            } else if (!config.optionsFolder.exists() || !config.optionsFolder.isDirectory()) {
                config.errorMessage = "Options folder not found.";
            } else if (config.options.isEmpty()) {
                config.errorMessage = "Options folder has no subfolders.";
            }
        } catch (IOException ex) {
            config.errorMessage = "Unable to read config file.";
            config.options = new ArrayList<ComboOption>();
        }

        if (config.options == null) {
            config.options = new ArrayList<ComboOption>();
        }

        return config;
    }

    private List<ComboOption> loadOptions(File optionsFolder) {
        List<ComboOption> options = new ArrayList<ComboOption>();
        if (optionsFolder == null || !optionsFolder.exists() || !optionsFolder.isDirectory()) {
            return options;
        }

        File[] subFolders = optionsFolder.listFiles(File::isDirectory);
        if (subFolders == null) {
            return options;
        }

        Arrays.sort(subFolders, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File subFolder : subFolders) {
            ComboOption option = new ComboOption();
            option.label = subFolder.getName();
            option.value = subFolder.getAbsolutePath();
            options.add(option);
        }

        return options;
    }

    private String readStringField(String json, String fieldName) {
        if (json == null || fieldName == null) {
            return null;
        }

        Matcher matcher = STRING_FIELD_PATTERN.matcher(json);
        while (matcher.find()) {
            if (fieldName.equals(matcher.group(1))) {
                return this.unescapeJsonString(matcher.group(2));
            }
        }

        return null;
    }

    private String unescapeJsonString(String value) {
        if (value == null) {
            return null;
        }

        return value
            .replace("\\\\", "\\")
            .replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t");
    }

    private File resolvePath(File baseFolder, String path) {
        String cleanedPath = this.cleanPath(path);
        if (cleanedPath.isEmpty()) {
            return null;
        }

        File file = new File(cleanedPath);
        if (file.isAbsolute()) {
            return file;
        }

        return new File(baseFolder, cleanedPath);
    }

    private String getControlName(String fileName) {
        if (fileName == null) {
            return "combobox";
        }

        String lower = fileName.toLowerCase();
        if (lower.endsWith(".cbx.json")) {
            return fileName.substring(0, fileName.length() - ".cbx.json".length());
        }

        return fileName;
    }

    private void showEmptyState(String message) {
        this.m_resultsPanel.removeAll();
        JLabel emptyLabel = new JLabel(message);
        emptyLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
        this.m_resultsPanel.add(emptyLabel);
        this.m_resultsPanel.revalidate();
        this.m_resultsPanel.repaint();
    }

    private void setStatus(String message) {
        this.m_statusLabel.setText(message);
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
        if (first != null && !first.trim().isEmpty()) {
            return first;
        }
        return second;
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private String quote(String value) {
        String safeValue = (value == null) ? "" : value.replace("\"", "\\\"");
        return "\"" + safeValue + "\"";
    }

    private static class ComboBoxConfig {
        private String controlName;
        private String label;
        private String selectedValue;
        private String errorMessage;
        private File configFile;
        private File optionsFolder;
        private File handler;
        private List<ComboOption> options = new ArrayList<ComboOption>();
    }

    private static class ComboOption {
        private String label;
        private String value;
    }
}