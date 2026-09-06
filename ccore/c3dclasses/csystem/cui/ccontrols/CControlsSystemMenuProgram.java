//-------------------------------------------------------
// name: CControlsSystemMenuProgram.java
// desc: Demonstrates system tray menu usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlsSystemMenuProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();
		String defaultIconPath = __.dir_path("CControlsSystemMenuProgram.java") + "/test.ico";
		String trayIconPath = (args != null && args.length > 0) ? args[0] : defaultIconPath;
		__.println("[INFO] System tray demo started. Right-click the tray icon to open the menu.");

		// Build a basic form so the program has a root control context.
		ccontrols.form("main-form", "System Menu Demo", null);
			ccontrols.label("info-label", "Use the system tray menu items.", null);
		ccontrols.endform();

		// Build the system tray menu hierarchy.
		ccontrols.sysmenubar("tray-menubar", trayIconPath, null);
			ccontrols.sysmenu("app-menu", "Application", null);
				ccontrols.sysmenuitem("show-item", "Show", null);
				ccontrols.sysmenuitem_checkbox("auto-start-item", "Auto Start", null);
				ccontrols.sysmenuitem_seperator();
				ccontrols.sysmenuitem("exit-item", "Exit", null);
			ccontrols.endsysmenu();

			ccontrols.sysmenu("help-menu", "Help", null);
				ccontrols.sysmenuitem("about-item", "About", null);
			ccontrols.endsysmenu();
		ccontrols.endsysmenubar();

		ccontrols.retrieve("main-form").setProp("grid", "true");
		ccontrols.retrieve("main-form").setProp("visible", "true");
		ccontrols.retrieve("main-form").setProp("pack", "true");
		ccontrols.retrieve("main-form").setProp("close", "true");

		CControl showItem = findControl(ccontrols,
			"tray-menubar app-menu show-item",
			"main-form tray-menubar app-menu show-item");
		if (showItem != null) {
			showItem.setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				__.println("System menu: Show clicked");
				ccontrols.retrieve("main-form").setProp("visible", "true");
				return null;
			}
			});
		}

		CControl exitItem = findControl(ccontrols,
			"tray-menubar app-menu exit-item",
			"main-form tray-menubar app-menu exit-item");
		if (exitItem != null) {
			exitItem.setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				__.println("System menu: Exit clicked");
				ccontrols.retrieve("main-form").setProp("close", "true");
				return null;
			}
			});
		}

		CControl aboutItem = findControl(ccontrols,
			"tray-menubar help-menu about-item",
			"main-form tray-menubar help-menu about-item");
		if (aboutItem != null) {
			aboutItem.setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				__.alert("CControlsSystemMenuProgram: system menu demo");
				return null;
			}
			});
		}
	}

	private static CControl findControl(CControls ccontrols, String... pathIds) {
		if (ccontrols == null || pathIds == null) {
			return null;
		}
		for (String pathId : pathIds) {
			if (pathId == null || pathId.trim().isEmpty()) {
				continue;
			}
			CControl control = ccontrols.retrieve(pathId);
			if (control != null) {
				return control;
			}
		}
		return null;
	}
}
