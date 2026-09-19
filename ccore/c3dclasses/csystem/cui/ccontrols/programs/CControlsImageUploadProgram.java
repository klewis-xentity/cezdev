//-------------------------------------------------------
// name: CControlsImageUploadProgram.java
// desc: Demonstrates image upload picker usage in CControls.
//-------------------------------------------------------
package c3dclasses;

public class CControlsImageUploadProgram {
	public static void main(String[] args) {
		CControls ccontrols = new CControls();

		ccontrols.form("imageupload-form", "Image Upload Demo", null);
			ccontrols.label("image-label", "Choose an image and click Show Selected Image.", null);
			ccontrols.image("project-image", "Choose Image", null);
			ccontrols.button("show-image", "Show Selected Image", null);
		ccontrols.endform();

		ccontrols.retrieve("imageupload-form").setProp("grid", "true");
		ccontrols.retrieve("imageupload-form").setProp("visible", "true");
		ccontrols.retrieve("imageupload-form").setProp("pack", "true");
		ccontrols.retrieve("imageupload-form").setProp("close", "true");

		ccontrols.retrieve("imageupload-form show-image").setProp("onclick", new CFunction() {
			public CReturn call(CObject obj) {
				CControl image = ccontrols.retrieve("imageupload-form project-image");
				String selected = (String) image.getProp("value");
				__.alert("Selected image: " + selected);
				__.println("Selected image: " + selected);
				return null;
			}
		});
	}
}
