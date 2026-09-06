//--------------------------------------------------------------
// name: CFileUploadInstructions
// desc: implements file upload picker instruction set
//--------------------------------------------------------------
package c3dclasses;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;

//--------------------------------------------------------
// name: CFileUploadInstructions
// desc: implements fileupload control behavior
//--------------------------------------------------------
class CFileUploadInstructions extends CInstructions {
	public CFileUploadInstructions(CProcessor cprocessor) {
		super(cprocessor);
		final CControlInstructions ccontrolinstructions =
			(CControlInstructions) cprocessor.getCInstructions("CControlInstructions");

		CFunction fnCreateFileUpload = new CFunction() { public CReturn call(CObject obj) {
			final CControl ccontrol = (CControl) obj;
			String label = (String) ccontrol._("m_value");
			if (label == null || label.trim().isEmpty()) {
				label = "Choose File";
			}

			final JButton button = new JButton(label);

			Object restored = CControlStateMemory.load(ccontrol);
			if (restored != null) {
				String restoredPath = String.valueOf(restored);
				ccontrol._("m_selected_file", restoredPath);
				button.setText(restoredPath);
			}

			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String current = (String) ccontrol._("m_selected_file");
					JFileChooser chooser = new JFileChooser();
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
						ccontrol._("m_selected_file", selectedPath);
						button.setText(selectedPath);
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
			ccontrol._("m_selected_file", path);
			button.setText(path.isEmpty() ? "Choose File" : path);
			CControlStateMemory.save(ccontrol, path);
			return null;
		}};

		CFunction fnGetValue = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			String selected = (String) ccontrol._("m_selected_file");
			if (selected == null || selected.trim().isEmpty()) {
				Object restored = CControlStateMemory.load(ccontrol);
				if (restored != null) {
					selected = String.valueOf(restored);
					ccontrol._("m_selected_file", selected);
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

		cprocessor._("fileupload->create", fnCreateFileUpload);
		cprocessor._("fileupload->set->visible", cprocessor._("ccontrol->set->visible"));
		cprocessor._("fileupload->get->visible", cprocessor._("ccontrol->get->visible"));
		cprocessor._("fileupload->set->value", fnSetValue);
		cprocessor._("fileupload->get->value", fnGetValue);
		cprocessor._("fileupload->set->text", fnSetText);
		cprocessor._("fileupload->get->text", fnGetText);
		cprocessor._("fileupload->set->onclick", cprocessor._("button->set->onclick"));
	}
}
