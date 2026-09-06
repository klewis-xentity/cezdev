//--------------------------------------------------------------
// name: CColorInstructions
// desc: implements color picker instruction set
//--------------------------------------------------------------
package c3dclasses;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;

//--------------------------------------------------------
// name: CColorInstructions
// desc: implements color control behavior
//--------------------------------------------------------
class CColorInstructions extends CInstructions {
	public CColorInstructions(CProcessor cprocessor) {
		super(cprocessor);
		final CControlInstructions ccontrolinstructions =
			(CControlInstructions) cprocessor.getCInstructions("CControlInstructions");

		CFunction fnCreateColorPicker = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			String label = (String) ccontrol._("m_value");
			if (label == null || label.trim().isEmpty()) {
				label = "Choose Color";
			}

			JButton button = new JButton(label);
			Object restored = CControlStateMemory.load(ccontrol);
			String selectedColor = (restored == null) ? label : String.valueOf(restored);
			Color initialColor = parseColor(selectedColor);
			if (initialColor != null) {
				button.setBackground(initialColor);
				button.setOpaque(true);
				button.setBorderPainted(false);
				String initialHex = toHex(initialColor);
				button.setText(initialHex);
				ccontrol._("m_selected_color", initialHex);
				CControlStateMemory.save(ccontrol, ccontrol._("m_selected_color"));
			}

			final CControl ref = ccontrol;
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color current = parseColor((String) ref._("m_selected_color"));
					if (current == null) {
						current = button.getBackground();
					}
					Color chosen = JColorChooser.showDialog(button, "Pick a color", current);
					if (chosen != null) {
						button.setBackground(chosen);
						button.setOpaque(true);
						button.setBorderPainted(false);
						String hex = toHex(chosen);
						button.setText(hex);
						ref._("m_selected_color", hex);
						CControlStateMemory.save(ref, hex);
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
			if (value != null) {
				Color color = parseColor(value.toString());
				if (color != null) {
					button.setBackground(color);
					button.setOpaque(true);
					button.setBorderPainted(false);
					String hex = toHex(color);
					button.setText(hex);
					ccontrol._("m_selected_color", hex);
					CControlStateMemory.save(ccontrol, hex);
				}
			}
			return null;
		}};

		CFunction fnGetValue = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			JButton button = (JButton) ccontrol._("m_jcontrol");
			String selected = toHex(button.getBackground());
			if (selected == null || selected.trim().isEmpty()) {
				selected = (String) ccontrol._("m_selected_color");
			}
			if (selected != null) {
				ccontrol._("m_selected_color", selected);
				button.setText(selected);
			}
			ccontrol._("m_propvalue", selected);
			return null;
		}};

		CFunction fnSetText = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			JButton button = (JButton) ccontrol._("m_jcontrol");
			button.setText((String) ccontrol._("m_propvalue"));
			return null;
		}};

		CFunction fnGetText = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			JButton button = (JButton) ccontrol._("m_jcontrol");
			ccontrol._("m_propvalue", button.getText());
			return null;
		}};

		cprocessor._("color->create", fnCreateColorPicker);
		cprocessor._("color->set->visible", cprocessor._("ccontrol->set->visible"));
		cprocessor._("color->get->visible", cprocessor._("ccontrol->get->visible"));
		cprocessor._("color->set->value", fnSetValue);
		cprocessor._("color->get->value", fnGetValue);
		cprocessor._("color->set->text", fnSetText);
		cprocessor._("color->get->text", fnGetText);
		cprocessor._("color->set->onclick", cprocessor._("button->set->onclick"));
	}

	private static Color parseColor(String value) {
		if (value == null) {
			return null;
		}
		String v = value.trim();
		if (v.isEmpty()) {
			return null;
		}
		if (!v.startsWith("#") && v.matches("[0-9a-fA-F]{6}")) {
			v = "#" + v;
		}
		try {
			return Color.decode(v);
		} catch (Exception ex) {
			return null;
		}
	}

	private static String toHex(Color color) {
		if (color == null) {
			return "#000000";
		}
		return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
	}
}
