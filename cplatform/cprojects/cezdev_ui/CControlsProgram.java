//-------------------------------------------------------
// name: CControlsProgram.java
// desc: Minimal CControls app with a blank panel.
//-------------------------------------------------------
import c3dclasses.CControls;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class CControlsProgram {
    private static final String DEFAULT_PATH = System.getProperty("user.home") + "/Desktop/cezdev/cplatform";
    private static final Pattern CCONTROL_SUBEXT_JSON_PATTERN = Pattern.compile("(?i)^.+\\.[a-z0-9_-]+\\.json$");

    public static void main(String[] args) {
        String inputPath = (args != null && args.length > 0) ? args[0] : "";
        String resolvedPath = resolvePath(inputPath);
        String listing = listDirectoryContents(resolvedPath);

        // openDirectory(resolvedPath);

        CControls ccontrols = new CControls();
        String statusText = "Path: " + resolvedPath + "\nControls Loaded: ";

        ccontrols.form("main-form", "Blank Panel App", null);
        ccontrols.label("panel-path", "Path: " + resolvedPath, null);
        ccontrols.textarea("panel-status", statusText + "loading...", null);

        boolean controlsLoaded = ccontrols.loadCControlsFromPath(resolvedPath);

        ccontrols.label("panel-load-status", "Controls Loaded: " + controlsLoaded, null);
        ccontrols.retrieve("main-form panel-status").setProp("text", statusText + controlsLoaded);
        ccontrols.endform();

        ccontrols.retrieve("main-form").setProp("grid", "true");
        ccontrols.retrieve("main-form").setProp("visible", "true");
        ccontrols.retrieve("main-form").setProp("pack", "true");
        ccontrols.retrieve("main-form").setProp("close", "true");
    }

    private static String resolvePath(String candidatePath) {
        String trimmed = candidatePath == null ? "" : candidatePath.trim();
        if (!trimmed.isEmpty() && new File(trimmed).exists()) {
            return trimmed;
        }

        if (new File(DEFAULT_PATH).exists()) {
            return DEFAULT_PATH;
        }

        return DEFAULT_PATH;
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
