//-------------------------------------------------------
// name: CControlsProgram.java
// desc: Minimal CControls app with a blank panel.
//-------------------------------------------------------
package c3dclasses;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

public class CControlsSchemaFromPathProgram {
    private static final String DEFAULT_RELATIVE_PATH = "cplatform";
    private static final Pattern CCONTROL_SUBEXT_JSON_PATTERN = Pattern.compile("(?i)^.+\\.[a-z0-9_-]+\\.json$");
    private static final Pattern JSON_PATTERN = Pattern.compile("(?i)^.+\\.json$");

    public static void main(String[] args) {
        String inputPath = (args != null && args.length > 0) ? args[0] : "";
        String resolvedPath = resolvePath(inputPath);
        listDirectoryContents(resolvedPath);

        // openDirectory(resolvedPath);

        CControlsSchema schema = new CControlsSchema();
        File schemaFile = resolveSchemaFile(resolvedPath);
        CControls ccontrols = schemaFile == null ? null : schema.renderFromFile(schemaFile.getAbsolutePath());
        boolean controlsLoaded = ccontrols != null;

        if (!controlsLoaded) {
            ccontrols = new CControls();
            String statusText = "Path: " + resolvedPath + "\nControls Loaded: false";
            ccontrols.form("main-form", "Blank Panel App", null);
            ccontrols.label("panel-path", "Path: " + resolvedPath, null);
            ccontrols.textarea("panel-status", statusText, null);
            ccontrols.endform();
            showForm(ccontrols, "main-form");
            return;
        }

        String formId = schema.getFormId();
        if (formId == null) {
            return;
        }
        showForm(ccontrols, formId);
    }

    private static String resolvePath(String candidatePath) {
        String trimmed = candidatePath == null ? "" : candidatePath.trim();
        if (!trimmed.isEmpty()) {
            File input = new File(trimmed);
            if (input.exists()) {
                return input.getAbsolutePath();
            }

            File relativeToCwd = new File(System.getProperty("user.dir"), trimmed);
            if (relativeToCwd.exists()) {
                return relativeToCwd.getAbsolutePath();
            }
        }

        File projectRoot = findProjectRoot();
        if (projectRoot != null) {
            File defaultPath = new File(projectRoot, DEFAULT_RELATIVE_PATH);
            if (defaultPath.exists()) {
                return defaultPath.getAbsolutePath();
            }
        }

        return new File(System.getProperty("user.dir"), DEFAULT_RELATIVE_PATH).getAbsolutePath();
    }

    private static File findProjectRoot() {
        File current = new File(System.getProperty("user.dir")).getAbsoluteFile();
        while (current != null) {
            File ccore = new File(current, "ccore");
            File cplatform = new File(current, "cplatform");
            if (ccore.exists() && ccore.isDirectory() && cplatform.exists() && cplatform.isDirectory()) {
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    private static File resolveSchemaFile(String path) {
        File input = new File(path);
        if (input.isFile() && JSON_PATTERN.matcher(input.getName()).matches()) {
            return input;
        }
        if (!input.isDirectory()) {
            return null;
        }

        File[] files = input.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            return null;
        }

        Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        for (File file : files) {
            if (file.getName().equalsIgnoreCase("ccontrols.schema.json")) {
                return file;
            }
        }
        for (File file : files) {
            if (file.getName().toLowerCase().endsWith(".schema.json")) {
                return file;
            }
        }
        for (File file : files) {
            if (JSON_PATTERN.matcher(file.getName()).matches()) {
                return file;
            }
        }
        return null;
    }

    private static void showForm(CControls ccontrols, String formId) {
        CControl form = ccontrols == null ? null : ccontrols.retrieve(formId);
        if (form == null) {
            return;
        }
        form.setProp("grid", "true");
        form.setProp("visible", "true");
        form.setProp("pack", "true");
        form.setProp("close", "true");
    }

    private static void openDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(dir);
            } catch (IOException e) {
                System.out.println("[WARN] Unable to open directory: " + path);
                System.out.println("[WARN] " + e.getMessage());
            }
        }
    }

    private static String listDirectoryContents(String path) {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            String message = "[ERROR] Directory not found: " + path;
            System.out.println(message);
            return message;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            String message = "[INFO] Directory is empty: " + path;
            System.out.println(message);
            return message;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Directory Contents\n");
        sb.append(path).append("\n\n");

        for (File file : files) {
            String type = file.isDirectory() ? "[DIR] " : "[FILE] ";
            String fileName = file.getName();
            String line = type + fileName;
            if (file.isFile() && isCControlConfigFile(fileName)) {
                line = "[CCONTROL-CONFIG] " + line;
            }
            System.out.println(line);
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    private static boolean isCControlConfigFile(String fileName) {
        return CCONTROL_SUBEXT_JSON_PATTERN.matcher(fileName).matches();
    }
}
