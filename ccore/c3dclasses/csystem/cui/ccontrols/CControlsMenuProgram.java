//-------------------------------------------------------
// name: CControlsMenuProgram.java
// desc: Demonstrates menu and menu item usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlsMenuProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("menu-form", "CControls Menu Demo", null);
			ccontrols.label("menu-label", "Use the menubar items to trigger actions.", null);

			ccontrols.menubar("main-menubar", "Main Menu", null);
				ccontrols.menu("file-menu", "File", null);
					ccontrols.menuitem("new-item", "New", null);
					ccontrols.menuitem("open-item", "Open", null);
					ccontrols.menuitem_seperator();
					ccontrols.menuitem("exit-item", "Exit", null);
				ccontrols.endmenu();

				ccontrols.menu("edit-menu", "Edit", null);
					ccontrols.menuitem("copy-item", "Copy", null);
					ccontrols.menuitem("paste-item", "Paste", null);
					ccontrols.menuitem_checkbox("autosave-item", "Auto Save", null);
				ccontrols.endmenu();

				ccontrols.menu("help-menu", "Help", null);
					ccontrols.menuitem("about-item", "About", null);
				ccontrols.endmenu();
			ccontrols.endmenubar();

			ccontrols.button("status-button", "Click Me", null);
		ccontrols.endform();

		ccontrols.retrieve("menu-form").setProp("grid", "true");
		ccontrols.retrieve("menu-form").setProp("visible", "true");
		ccontrols.retrieve("menu-form").setProp("pack", "true");
		ccontrols.retrieve("menu-form").setProp("close", "true");

		ccontrols.retrieve("menu-form main-menubar file-menu new-item").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				__.alert("File > New clicked");
				return null;
			}
		});

		ccontrols.retrieve("menu-form main-menubar file-menu open-item").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				__.println("File > Open clicked");
				return null;
			}
		});

		ccontrols.retrieve("menu-form main-menubar file-menu exit-item").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				__.println("File > Exit clicked");
				ccontrols.retrieve("menu-form").setProp("close", "true");
				return null;
			}
		});

		ccontrols.retrieve("menu-form main-menubar help-menu about-item").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				__.alert("CControlsMenuProgram: menu demo");
				return null;
			}
		});
	}
}
