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

import net.sf.librefundraiser.db.IDonorDB;
import net.sf.librefundraiser.db.SQLite;
import net.sf.librefundraiser.gui.MainWindow;
import net.sf.librefundraiser.gui.NewDatabaseWizard;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolItem;

public class Main {
	private static IDonorDB localDB = null;
	private static NumberFormat currency = null;
	private static MainWindow window;
	private final static Properties settings = new Properties();
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final String version = "(Development Snapshot)";

	public static void main(String args[]) {
		loadSettings();
		if (getSetting("lastDB") == null || !(new File(getSetting("lastDB")).exists())) {
			NewDatabaseWizard dialog = new NewDatabaseWizard();
			addSetting("lastDB",dialog.open());
		}
		resetLocalDB();
		try {
			window = new MainWindow();
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static IDonorDB getDonorDB() {
		if (localDB == null) localDB = new SQLite();
		return localDB;
	}
	public static void resetLocalDB() {
		localDB = new SQLite(getSetting("lastDB"));
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
	public static boolean fileExists (String path) {
		boolean realFile = false;
		try {
			File file = new File(path);
			if (file.exists()) realFile = true;
		} catch (Exception e1) {
		}
		return realFile;
	}
}
