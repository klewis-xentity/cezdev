//-------------------------------------------------------
// name: CControlsProgram3.java
// desc: 
//-------------------------------------------------------
import c3dclasses.*;
import java.io.File;


public class CControlsProgram3 {				
	private static CHash loadOptionsFromCVar(String key) {
		CHash options = new CHash();
		String cvarsPath = System.getenv("CMETADATA_CVARS");
		if (cvarsPath == null || cvarsPath.equals("")) {
			return options;
		}

		if (CMemory.include("cvars", cvarsPath, "c3dclasses.CJSONMemoryDriver", null) == null) {
			return options;
		}

		CMemory cmemory = CMemory.use("cvars");
		if (cmemory == null) {
			return options;
		}

		CReturn creturn = cmemory.retrieve("cezdev_ui.cplatform");
		if (creturn == null || creturn.data() == null) {
			cmemory.close();
			return options;
		}

		CHash cvar = creturn._chash();
		String json = (cvar != null) ? cvar._string("m_value") : "";
		if (json == null || json.equals("")) {
			cmemory.close();
			return options;
		}

		Object decoded = CJSON.decode(json);
		if (decoded instanceof CHash) {
			CHash root = (CHash) decoded;
			CArray values = root._carray(key);
			if (values != null && values.length() > 0 && values._chash(0) != null) {
				CHash valueMap = values._chash(0);
				CArray names = valueMap.keys();
				for (int i = 0; i < names.length(); i++) {
					String name = names._string(i);
					String path = valueMap._string(name);
					options.set(name, path);
				}
			}
		}

		cmemory.close();
		return options;
	}

	private static void loadOptionsFromDirectory(CHash options, String envVarName) {
		String basePath = System.getenv(envVarName);
		if (basePath == null) {
			return;
		}
		File dir = new File(basePath);
		File[] entries = dir.listFiles(File::isDirectory);
		if (entries == null) {
			return;
		}
		for (File entry : entries) {
			String name = entry.getName();
			options.set(name, entry.getAbsolutePath());
		}
	}

	public static void main(String[] args) {
		// Build select options from cvar memory first.
		CHash projectOptions = loadOptionsFromCVar("cprojects");
		CHash environmentOptions = loadOptionsFromCVar("cenvironments");
		CHash libraryOptions = loadOptionsFromCVar("clibraries");

		// Fall back to direct folder scan when cvar is missing/empty.
		if (projectOptions.keys().length() == 0) {
			loadOptionsFromDirectory(projectOptions, "CPROJECTS");
		}
		if (environmentOptions.keys().length() == 0) {
			loadOptionsFromDirectory(environmentOptions, "CENVIRONMENTS");
		}
		if (libraryOptions.keys().length() == 0) {
			loadOptionsFromDirectory(libraryOptions, "CLIBRARIES");
		}

		CControls ccontrols = new CControls();
		ccontrols.form("myform", "This is the form title", null);
			ccontrols.panel("panel-cprojects", "cprojects", null);
				ccontrols.select("cprojects", null, projectOptions, null);
			ccontrols.endpanel();
			ccontrols.panel("panel-cenvironments", "cenvironments", null);
				ccontrols.select("cenvironments", null, environmentOptions, null);
			ccontrols.endpanel();
			ccontrols.panel("panel-clibraries", "clibraries", null);
				ccontrols.select("clibraries", null, libraryOptions, null);
			ccontrols.endpanel();
		ccontrols.endform();

		// Print the currently selected folder path whenever a select value changes.
		ccontrols.retrieve("myform panel-cprojects cprojects").setProp("onchange", "test.bat");
		ccontrols.retrieve("myform panel-cenvironments cenvironments").setProp("onchange", "test.bat");
		ccontrols.retrieve("myform panel-clibraries clibraries").setProp("onchange", "test.bat");

		ccontrols.retrieve("myform").setProp("grid","true");
		ccontrols.retrieve("myform").setProp("visible","true");
		//ccontrols.retrieve("myform").setProp("pack","true");
		ccontrols.retrieve("myform").setProp("close","true");
	} // end main()
} // end CControlsProgram3
