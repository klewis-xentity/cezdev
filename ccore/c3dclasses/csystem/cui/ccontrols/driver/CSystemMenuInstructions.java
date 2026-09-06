//--------------------------------------------------------------
// name: CSystemMenuInstructions
// desc: implements system tray menu instruction handlers
//--------------------------------------------------------------
package c3dclasses;

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

//--------------------------------------------------------
// name: CSystemMenuInstructions
// desc: implements system tray menu controls
//--------------------------------------------------------
class CSystemMenuInstructions extends CInstructions {
	private static final List<TrayIcon> M_TRAY_ICONS = new ArrayList<TrayIcon>();

	public CSystemMenuInstructions(CProcessor cprocessor) {
		super(cprocessor);

		CFunction fnCreateTrayMenuBar = new CFunction() { public CReturn call(CObject obj) {
			CControl control = (CControl) obj;
			CSystemMenuInstructions.createTrayMenuBar(control);
			return CReturn._done(control);
		}};

		CFunction fnCreateTrayMenu = new CFunction() { public CReturn call(CObject obj) {
			CControl control = (CControl) obj;
			CSystemMenuInstructions.createTrayMenu(control, new Menu((String) control._("m_value")));
			return CReturn._done(control);
		}};

		CFunction fnCreateTrayMenuItem = new CFunction() { public CReturn call(CObject obj) {
			CControl control = (CControl) obj;
			CSystemMenuInstructions.createTrayMenuItem(control, new MenuItem((String) control._("m_value")));
			return CReturn._done(control);
		}};

		CFunction fnCreateTrayCheckboxMenuItem = new CFunction() { public CReturn call(CObject obj) {
			CControl control = (CControl) obj;
			CSystemMenuInstructions.createTrayCheckboxMenuItem(control, new CheckboxMenuItem((String) control._("m_value")));
			return CReturn._done(control);
		}};

		CFunction fnAddTrayMenuSeparator = new CFunction() { public CReturn call(CObject obj) {
			CSystemMenuInstructions.addTrayMenuSeparator((CControl) obj);
			return null;
		}};

		CFunction fnGetItemCount = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Object jcontrol = ccontrol._("m_jcontrol");
			if (jcontrol instanceof Menu) {
				ccontrol._("m_propvalue", ((Menu) jcontrol).getItemCount());
			}
			return null;
		}};

		CFunction fnDeleteAllItems = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Object jcontrol = ccontrol._("m_jcontrol");
			if (jcontrol instanceof Menu) {
				((Menu) jcontrol).removeAll();
			}
			return null;
		}};

		CFunction fnOnClick = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Object jcontrol = ccontrol._("m_jcontrol");
			final String command = (String) ccontrol._("m_propvalue") + " " + (String) ccontrol._("m_strid");
			if (jcontrol instanceof MenuItem && command != null && !command.trim().isEmpty()) {
				((MenuItem) jcontrol).addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						__.exec_command(command);
					}
				});
			}
			return null;
		}};

		CFunction fnDoClick = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Object jcontrol = ccontrol._("m_jcontrol");
			if (jcontrol instanceof MenuItem) {
				MenuItem item = (MenuItem) jcontrol;
				ActionListener[] listeners = item.getActionListeners();
				ActionEvent event = new ActionEvent(item, ActionEvent.ACTION_PERFORMED, item.getActionCommand());
				for (ActionListener listener : listeners) {
					listener.actionPerformed(event);
				}
			}
			return null;
		}};

		CFunction fnSetSelected = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Object jcontrol = ccontrol._("m_jcontrol");
			Object value = ccontrol._("m_propvalue");
			if (jcontrol instanceof CheckboxMenuItem) {
				boolean selected = false;
				if (value instanceof Boolean) {
					selected = (Boolean) value;
				} else if (value != null) {
					selected = Boolean.parseBoolean(value.toString());
				}
				((CheckboxMenuItem) jcontrol).setState(selected);
			}
			return null;
		}};

		CFunction fnGetSelected = new CFunction() { public CReturn call(CObject obj) {
			CControl ccontrol = (CControl) obj;
			Object jcontrol = ccontrol._("m_jcontrol");
			if (jcontrol instanceof CheckboxMenuItem) {
				ccontrol._("m_propvalue", ((CheckboxMenuItem) jcontrol).getState());
			}
			return null;
		}};

		cprocessor._("systray-menubar->create", fnCreateTrayMenuBar);
		cprocessor._("systray-menu->create", fnCreateTrayMenu);
		cprocessor._("systray-menu->get->itemcount", fnGetItemCount);
		cprocessor._("systray-menu->set->removeitems", fnDeleteAllItems);
		cprocessor._("systray-menuitem->create", fnCreateTrayMenuItem);
		cprocessor._("systray-menuitem-seperator->create", fnAddTrayMenuSeparator);
		cprocessor._("systray-menuitem->set->onclick", fnOnClick);
		cprocessor._("systray-menuitem->set->click", fnDoClick);
		cprocessor._("systray-menuitem-checkbox->create", fnCreateTrayCheckboxMenuItem);
		cprocessor._("systray-menuitem-checkbox->set->onclick", fnOnClick);
		cprocessor._("systray-menuitem-checkbox->set->selected", fnSetSelected);
		cprocessor._("systray-menuitem-checkbox->get->selected", fnGetSelected);
	}

	public static Object createTrayMenuBar(CControl ccontrol) {
		if (ccontrol == null) {
			return null;
		}

		PopupMenu popupMenu = new PopupMenu();
		ccontrol._("m_jcontrol", popupMenu);

		if (GraphicsEnvironment.isHeadless()) {
			__.println("[WARN] Headless mode is enabled. System tray is unavailable.");
			return ccontrol;
		}
		if (!SystemTray.isSupported()) {
			__.println("[WARN] SystemTray is not supported on this environment.");
			return ccontrol;
		}

		try {
			Image icon = buildTrayIcon((String) ccontrol._("m_value"));
			TrayIcon trayIcon = new TrayIcon(icon);
			trayIcon.setImageAutoSize(true);
			trayIcon.setToolTip("C3DClasses: " + String.valueOf(ccontrol._("m_strid")));
			trayIcon.setPopupMenu(popupMenu);
			SystemTray.getSystemTray().add(trayIcon);
			M_TRAY_ICONS.add(trayIcon);
			ccontrol._("m_systray_trayicon", trayIcon);
			__.println("[INFO] System tray icon created: " + String.valueOf(ccontrol._("m_strid")));
		} catch (Exception ex) {
			__.println("[WARN] Failed to attach tray icon: " + ex.getMessage());
		}
		return ccontrol;
	}

	public static Object createTrayMenu(CControl ccontrol, Menu menu) {
		if (ccontrol == null || menu == null) {
			return null;
		}
		ccontrol._("m_jcontrol", menu);
		Object parent = CControlInstructions.getParentContainer(ccontrol);
		if (parent instanceof PopupMenu) {
			((PopupMenu) parent).add(menu);
		} else if (parent instanceof Menu) {
			((Menu) parent).add(menu);
		}
		return ccontrol;
	}

	public static Object createTrayMenuItem(CControl ccontrol, MenuItem menuItem) {
		if (ccontrol == null || menuItem == null) {
			return null;
		}
		ccontrol._("m_jcontrol", menuItem);
		Object parent = CControlInstructions.getParentContainer(ccontrol);
		if (parent instanceof Menu) {
			((Menu) parent).add(menuItem);
		} else if (parent instanceof PopupMenu) {
			((PopupMenu) parent).add(menuItem);
		}
		return ccontrol;
	}

	public static Object createTrayCheckboxMenuItem(CControl ccontrol, CheckboxMenuItem menuItem) {
		if (ccontrol == null || menuItem == null) {
			return null;
		}
		ccontrol._("m_jcontrol", menuItem);
		Object parent = CControlInstructions.getParentContainer(ccontrol);
		if (parent instanceof Menu) {
			((Menu) parent).add(menuItem);
		} else if (parent instanceof PopupMenu) {
			((PopupMenu) parent).add(menuItem);
		}
		return ccontrol;
	}

	public static CControl addTrayMenuSeparator(CControl ccontrol) {
		if (ccontrol == null) {
			return null;
		}
		Object parent = CControlInstructions.getParentContainer(ccontrol);
		if (parent instanceof Menu) {
			((Menu) parent).addSeparator();
		} else if (parent instanceof PopupMenu) {
			((PopupMenu) parent).addSeparator();
		}
		return ccontrol;
	}

	private static Image buildTrayIcon(String iconPath) {
		try {
			if (iconPath != null && !iconPath.trim().isEmpty()) {
				File iconFile = new File(iconPath);
				if (iconFile.exists()) {
					Image image = Toolkit.getDefaultToolkit().getImage(iconFile.getAbsolutePath());
					if (image != null) {
						return image;
					}
				}
			}
		} catch (Exception ex) {
			// fallback below
		}

		BufferedImage fallback = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = fallback.createGraphics();
		g.setColor(new Color(0x1D, 0x70, 0xB8));
		g.fillRoundRect(0, 0, 16, 16, 4, 4);
		g.setColor(Color.WHITE);
		g.fillOval(4, 4, 8, 8);
		g.dispose();
		return fallback;
	}

	public static void clearTrayIcons() {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			for (TrayIcon trayIcon : M_TRAY_ICONS) {
				if (trayIcon != null) {
					tray.remove(trayIcon);
				}
			}
		}
		M_TRAY_ICONS.clear();
	}
}
