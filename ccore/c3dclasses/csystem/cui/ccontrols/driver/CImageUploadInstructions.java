//--------------------------------------------------------------
// name: CImageUploadInstructions
// desc: implements image upload picker instruction set
//--------------------------------------------------------------
package c3dclasses;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

//--------------------------------------------------------
// name: CImageUploadInstructions
// desc: implements image control behavior
//--------------------------------------------------------
class CImageUploadInstructions extends CInstructions {
	public CImageUploadInstructions(CProcessor cprocessor) {
		super(cprocessor);
		final CControlInstructions ccontrolinstructions =
			(CControlInstructions) cprocessor.getCInstructions("CControlInstructions");

		CFunction fnCreateImageUpload = new CFunction() { public CReturn call(CObject obj) {
			final CControl ccontrol = (CControl) obj;
			String label = (String) ccontrol._("m_value");
			if (label == null || label.trim().isEmpty()) {
				label = "Choose Image";
			}

			final JButton button = new JButton(label);

			Object restored = CControlStateMemory.load(ccontrol);
			if (restored != null) {
				String restoredPath = String.valueOf(restored);
				ccontrol._("m_selected_image", restoredPath);
				updateImageButton(button, restoredPath);
			}

			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String current = (String) ccontrol._("m_selected_image");
					JFileChooser chooser = new JFileChooser();
					chooser.setFileFilter(new FileNameExtensionFilter(
						"Image Files (*.png, *.jpg, *.jpeg, *.gif, *.bmp, *.webp)",
						"png", "jpg", "jpeg", "gif", "bmp", "webp"
					));
					chooser.setAcceptAllFileFilterUsed(true);

					if (current != null && !current.trim().isEmpty()) {
						File existing = new File(current);
						if (existing.exists()) {
							File parent = existing.getParentFile();
							if (parent != null && parent.exists()) {
								chooser.setCurrentDirectory(parent);
							}
						}
					}

					int result = chooser.showOpenDialog(button);
					if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
						String selectedPath = chooser.getSelectedFile().getAbsolutePath();
						ccontrol._("m_selected_image", selectedPath);
						updateImageButton(button, selectedPath);
						CControlStateMemory.save(ccontrol, selectedPath);
					}
				}
			});

			ccontrolinstructions.createJControl(ccontrol, button);
			return CReturn._done(ccontrol);
		}};

		CFunction fnSetValue = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			JButton button = (JButton) ccontrol._("m_jcontrol");
			Object value = ccontrol._("m_propvalue");
			String path = (value == null) ? "" : String.valueOf(value);
			ccontrol._("m_selected_image", path);
			updateImageButton(button, path);
			CControlStateMemory.save(ccontrol, path);
			return null;
		}};

		CFunction fnGetValue = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			String selected = (String) ccontrol._("m_selected_image");
			if (selected == null || selected.trim().isEmpty()) {
				Object restored = CControlStateMemory.load(ccontrol);
				if (restored != null) {
					selected = String.valueOf(restored);
					ccontrol._("m_selected_image", selected);
				}
			}
			ccontrol._("m_propvalue", selected == null ? "" : selected);
			return null;
		}};

		CFunction fnSetText = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			JButton button = (JButton) ccontrol._("m_jcontrol");
			Object value = ccontrol._("m_propvalue");
			button.setText(value == null ? "" : String.valueOf(value));
			return null;
		}};

		CFunction fnGetText = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			JButton button = (JButton) ccontrol._("m_jcontrol");
			ccontrol._("m_propvalue", button.getText());
			return null;
		}};

		cprocessor._("image->create", fnCreateImageUpload);
		cprocessor._("image->set->visible", cprocessor._("ccontrol->set->visible"));
		cprocessor._("image->get->visible", cprocessor._("ccontrol->get->visible"));
		cprocessor._("image->set->value", fnSetValue);
		cprocessor._("image->get->value", fnGetValue);
		cprocessor._("image->set->text", fnSetText);
		cprocessor._("image->get->text", fnGetText);
		cprocessor._("image->set->onclick", cprocessor._("button->set->onclick"));
	}

	private static void updateImageButton(JButton button, String path) {
		if (button == null) {
			return;
		}
		if (path == null || path.trim().isEmpty()) {
			button.setText("Choose Image");
			button.setIcon(null);
			return;
		}

		button.setText(path);
		try {
			File f = new File(path);
			if (f.exists() && f.isFile()) {
				ImageIcon icon = new ImageIcon(path);
				if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
					Image scaled = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
					button.setIcon(new ImageIcon(scaled));
					return;
				}
			}
		} catch (Exception ex) {
			// keep text-only fallback
		}
		button.setIcon(null);
	}
}
