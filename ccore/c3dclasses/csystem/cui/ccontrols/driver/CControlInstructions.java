//--------------------------------------------------------------
// name: CControlInstructions
// desc: implements control instruction set
//--------------------------------------------------------------
package c3dclasses;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

//--------------------------------------------------------
// name: CControlInstructions
// desc: implements control instruction set
//--------------------------------------------------------
public class CControlInstructions extends CInstructions {	
	public CControlInstructions(CProcessor cprocessor) {	
		super(cprocessor);
		
		CFunction fnSetVisible = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Component jcontrol = (Component) ccontrol._("m_jcontrol");
			jcontrol.setVisible(toBoolean(ccontrol._("m_propvalue")));
			return null;	
		}}; // end fnSetVisible()
			
		CFunction fnGetVisible = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Component jcontrol = (Component) ccontrol._("m_jcontrol");
			ccontrol._("m_propvalue", jcontrol.isVisible());
			return null;	
		}}; // end fnGetVisible()
		
		CFunction fnSetFont = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Component jcontrol = (Component) ccontrol._("m_jcontrol");
			Font font = toFont(ccontrol._("m_propvalue"));
			if(font != null)
				jcontrol.setFont(font);
			return null;	
		}}; // end fnSetFont()
			
		CFunction fnGetFont = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Component jcontrol = (Component) ccontrol._("m_jcontrol");
			ccontrol._("m_propvalue", jcontrol.getFont());
			return null;	
		}}; // end fnGetFont()
		
		CFunction fnSetEnable = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Component jcontrol = (Component) ccontrol._("m_jcontrol");
			jcontrol.setEnabled(toBoolean(ccontrol._("m_propvalue")));
			return null;	
		}}; // end fnSetEnable()
			
		CFunction fnGetEnable = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Component jcontrol = (Component) ccontrol._("m_jcontrol");
			ccontrol._("m_propvalue", jcontrol.isEnabled());
			return null;	
		}}; // end fnGetEnable()
		
		CFunction fnSetIcon = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Icon icon = toIcon(ccontrol._("m_propvalue"));
			setIcon(ccontrol._("m_jcontrol"), icon);
			return null;	
		}}; // end fnSetIcon()
			
		CFunction fnGetIcon = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			ccontrol._("m_propvalue", getIcon(ccontrol._("m_jcontrol")));
			return null;	
		}}; // end fnGetIcon()
		
		CFunction fnSetText = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			setText(ccontrol._("m_jcontrol"), ccontrol._("m_propvalue"));
			return null;	
		}}; // end fnSetText()
			
		CFunction fnGetText = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			ccontrol._("m_propvalue", getText(ccontrol._("m_jcontrol")));
			return null;	
		}}; // end fnGetText()
		
		CFunction fnSetHorizontalAlignment = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			setHorizontalAlignment(ccontrol._("m_jcontrol"), ccontrol._("m_propvalue"));
			return null;	
		}}; // end fnSetHorizontalAlignment()
			
		CFunction fnGetHorizontalAlignment = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			ccontrol._("m_propvalue", getHorizontalAlignment(ccontrol._("m_jcontrol")));
			return null;	
		}}; // end fnGetHorizontalAlignment()
		
		CFunction fnSetVerticalAlignment = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			setVerticalAlignment(ccontrol._("m_jcontrol"), ccontrol._("m_propvalue"));
			return null;	
		}}; // end fnSetVerticalAlignment()
			
		CFunction fnGetVerticalAlignment = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			ccontrol._("m_propvalue", getVerticalAlignment(ccontrol._("m_jcontrol")));
			return null;	
		}}; // end fnGetVerticalAlignment()

		// Register the common control instructions.
		cprocessor._("ccontrol->set->visible", fnSetVisible);
		cprocessor._("ccontrol->get->visible", fnGetVisible);
		cprocessor._("ccontrol->set->font", fnSetFont);
		cprocessor._("ccontrol->get->font", fnGetFont);
		cprocessor._("ccontrol->set->enable", fnSetEnable);
		cprocessor._("ccontrol->get->enable", fnGetEnable);
		cprocessor._("ccontrol->set->icon", fnSetIcon);
		cprocessor._("ccontrol->get->icon", fnGetIcon);
		cprocessor._("ccontrol->set->text", fnSetText);
		cprocessor._("ccontrol->get->text", fnGetText);
		cprocessor._("ccontrol->set->horizontalalignment", fnSetHorizontalAlignment);
		cprocessor._("ccontrol->get->horizontalalignment", fnGetHorizontalAlignment);
		cprocessor._("ccontrol->set->verticalalignment", fnSetVerticalAlignment);
		cprocessor._("ccontrol->get->verticalalignment", fnGetVerticalAlignment);
	} // end CControlInstructions()

	private static boolean toBoolean(Object value) {
		return value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value));
	}

	private static Font toFont(Object value) {
		if(value instanceof Font)
			return (Font) value;
		if(value == null)
			return null;
		return Font.decode(String.valueOf(value));
	}

	private static Icon toIcon(Object value) {
		if(value instanceof Icon)
			return (Icon) value;
		if(value == null || String.valueOf(value).trim().isEmpty())
			return null;
		return new ImageIcon(String.valueOf(value));
	}

	private static void setIcon(Object control, Icon icon) {
		if(control instanceof AbstractButton)
			((AbstractButton) control).setIcon(icon);
		else if(control instanceof JLabel)
			((JLabel) control).setIcon(icon);
	}

	private static Icon getIcon(Object control) {
		if(control instanceof AbstractButton)
			return ((AbstractButton) control).getIcon();
		if(control instanceof JLabel)
			return ((JLabel) control).getIcon();
		return null;
	}

	private static void setText(Object control, Object value) {
		String text = value == null ? "" : String.valueOf(value);
		if(control instanceof AbstractButton)
			((AbstractButton) control).setText(text);
		else if(control instanceof JLabel)
			((JLabel) control).setText(text);
		else if(control instanceof JTextComponent)
			((JTextComponent) control).setText(text);
	}

	private static String getText(Object control) {
		if(control instanceof AbstractButton)
			return ((AbstractButton) control).getText();
		if(control instanceof JLabel)
			return ((JLabel) control).getText();
		if(control instanceof JTextComponent)
			return ((JTextComponent) control).getText();
		return null;
	}

	private static void setHorizontalAlignment(Object control, Object value) {
		int alignment = parseAlignment(value, true);
		if(control instanceof AbstractButton)
			((AbstractButton) control).setHorizontalAlignment(alignment);
		else if(control instanceof JLabel)
			((JLabel) control).setHorizontalAlignment(alignment);
		else if(control instanceof JTextField)
			((JTextField) control).setHorizontalAlignment(alignment);
	}

	private static int getHorizontalAlignment(Object control) {
		if(control instanceof AbstractButton)
			return ((AbstractButton) control).getHorizontalAlignment();
		if(control instanceof JLabel)
			return ((JLabel) control).getHorizontalAlignment();
		if(control instanceof JTextField)
			return ((JTextField) control).getHorizontalAlignment();
		return -1;
	}

	private static void setVerticalAlignment(Object control, Object value) {
		int alignment = parseAlignment(value, false);
		if(control instanceof AbstractButton)
			((AbstractButton) control).setVerticalAlignment(alignment);
		else if(control instanceof JLabel)
			((JLabel) control).setVerticalAlignment(alignment);
	}

	private static int getVerticalAlignment(Object control) {
		if(control instanceof AbstractButton)
			return ((AbstractButton) control).getVerticalAlignment();
		if(control instanceof JLabel)
			return ((JLabel) control).getVerticalAlignment();
		return -1;
	}

	private static int parseAlignment(Object value, boolean horizontal) {
		if(value instanceof Number)
			return ((Number) value).intValue();
		String alignment = String.valueOf(value).trim().toLowerCase();
		if("left".equals(alignment) || "top".equals(alignment))
			return horizontal ? javax.swing.SwingConstants.LEFT : javax.swing.SwingConstants.TOP;
		if("right".equals(alignment) || "bottom".equals(alignment))
			return horizontal ? javax.swing.SwingConstants.RIGHT : javax.swing.SwingConstants.BOTTOM;
		if("leading".equals(alignment))
			return javax.swing.SwingConstants.LEADING;
		if("trailing".equals(alignment))
			return javax.swing.SwingConstants.TRAILING;
		return javax.swing.SwingConstants.CENTER;
	}

	// Instruction set helper functions.
	public static Object createJControl(CControl ccontrol, Component jcontrol) {
		if(jcontrol == null)
			return null;
		ccontrol._("m_jcontrol", jcontrol);	
		Container parent = (Container) CControlInstructions.getParentContainer(ccontrol);
		if(parent != null)
			parent.add(jcontrol);
		return ccontrol;
	} // end createJControl()
	
	public static Object getParentContainer(CControl ccontrol) {
		if(ccontrol == null)
			return null;
		CControl container = (CControl) ccontrol._("m_container");
		if(container == null)
			return null;
		return (Object) container._("m_jcontrol");	
	} // end getParentContainer()
} // end CControlInstructions