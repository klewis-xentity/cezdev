//-------------------------------------------------------
// name: CControlsFileUploadProgram.java
// desc: Demonstrates file upload picker usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlsFileUploadProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("fileupload-form", "File Upload Demo", null);
			ccontrols.label("upload-label", "Choose a file and click Show Selected File.", null);
			ccontrols.fileupload("project-file", "Choose File", null);
			ccontrols.button("show-file", "Show Selected File", null);
		ccontrols.endform();

		ccontrols.retrieve("fileupload-form").setProp("grid", "true");
		ccontrols.retrieve("fileupload-form").setProp("visible", "true");
		ccontrols.retrieve("fileupload-form").setProp("pack", "true");
		ccontrols.retrieve("fileupload-form").setProp("close", "true");

		ccontrols.retrieve("fileupload-form show-file").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				CControl upload = ccontrols.retrieve("fileupload-form project-file");
				String selected = (String) upload.getProp("value");
				__.alert("Selected file: " + selected);
				__.println("Selected file: " + selected);
				return null;
			}
		});
	}
}
