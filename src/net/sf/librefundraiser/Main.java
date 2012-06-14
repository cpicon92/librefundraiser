package net.sf.librefundraiser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import net.sf.librefundraiser.db.SQLite;
import net.sf.librefundraiser.gui.MainWindow;
import net.sf.librefundraiser.gui.NewDatabaseDialog;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolItem;

/* Left to do: 
 * implement menu items
 * disable ones that won't be implemented for 1.0
 * about dialog
 * rewrite tablecolumn sorter
 */

public class Main {
	private static SQLite localDB = null;
	private static NumberFormat currency = null;
	private static MainWindow window;
	private final static Properties settings = new Properties();
	public static Image[] logo;
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static void main(String args[]) {
		loadSettings();
		Display display = Display.getDefault();
		logo = new Image[]{
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon16.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon24.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon32.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon48.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon64.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon128.png")),
				new Image(display,Main.class.getResourceAsStream("/net/sf/librefundraiser/logo/balloon256.png"))
				};
		if (getSetting("lastDB") == null || !(new File(getSetting("lastDB")).exists())) {
			NewDatabaseDialog dialog = new NewDatabaseDialog();
			addSetting("lastDB",dialog.open());
		}
		localDB = new SQLite(getSetting("lastDB"));
		try {
			window = new MainWindow();
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static SQLite getLocalDB() {
		if (localDB == null) localDB = new SQLite();
		return localDB;
	}
	public static String toMoney(double amount) {
		if (currency == null) currency = NumberFormat.getCurrencyInstance();
		return currency.format(amount);
	}
	public static String toMoney(String amount) {
		if (amount.trim().equals("")) amount = "0.00";
		try {
			return toMoney(Double.parseDouble(amount));
		} catch (Exception e) {
			System.err.println("Value \""+amount+"\" could not be parsed as money.");
			return amount;
		}
	}
	public static double fromMoney(String amount) {
		if (currency == null) currency = NumberFormat.getCurrencyInstance();
		try {
			return currency.parse(amount).doubleValue();
		} catch (ParseException e) {}
		return 0;
	}
	public static ToolItem getSaveButton() {
		return window.getSaveButton();
	}
	public static void setSaveAction(Runnable r) {
		window.setSaveAction(r);
	}
	public static void refresh() {
		window.refresh();
	}
	public static void reloadDonors() {
		window.reload();
	}
	public static void loadSettings() {
		String path = System.getenv("AppData");
		if (path == null) {
			path = System.getProperty("user.home")+"/.librefundraiser/settings.xml";
		} else {
			path = path + "\\LibreFundraiser\\settings.xml";
		}
		try {
			settings.loadFromXML(new BufferedInputStream(new FileInputStream(path)));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	public static void saveSettings() {
		String path = System.getenv("AppData");
		if (path == null) {
			new File(System.getProperty("user.home")+"/.librefundraiser").mkdirs();
			path = System.getProperty("user.home")+"/.librefundraiser/settings.xml";
		} else {
			new File(path + "\\LibreFundraiser").mkdirs();
			path = path + "\\LibreFundraiser\\settings.xml";
		}
		try {
			settings.storeToXML(new BufferedOutputStream(new FileOutputStream(path)),"LibreFundraiser");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	public static void addSetting(String key, String value) {
		settings.setProperty(key, value);
		new Thread(new Runnable() {
			public void run() {
				saveSettings();
			}
		}).start();
	}
	public static String getSetting(String key) {
		return settings.getProperty(key);
	}
	public static MainWindow getWindow() {
		return window;
	}
	public static DateFormat getDateFormat() {
		return dateFormat;
	}
}
