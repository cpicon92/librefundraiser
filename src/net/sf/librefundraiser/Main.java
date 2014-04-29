package net.sf.librefundraiser;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import net.sf.librefundraiser.db.FileDBASE;
import net.sf.librefundraiser.db.FileLFD;
import net.sf.librefundraiser.gui.MainWindow;
import net.sf.librefundraiser.gui.NewDatabaseWizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

//TODO: fix fixed-width dialogs to render properly on high-dpi displays
public class Main {
	private static FileLFD localDB = null;
	private static NumberFormat currency = null;
	private static MainWindow window;
	private final static Properties settings = new Properties();
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//	private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
	public static final String version = "(Development Snapshot)";

	public static void main(String args[]) {
		if (args.length > 1) {
			System.err.println("Syntax: librefundraiser [filename]");
		}
		loadSettings();
		if (args.length == 1) {
			addSetting("lastDB",args[0]);
		}
		String importDb = null;
		if (getSetting("lastDB") == null || !(new File(getSetting("lastDB")).exists())) {
			NewDatabaseWizard dialog = new NewDatabaseWizard();
			addSetting("lastDB",dialog.open());
			importDb = dialog.getFrbwImportFile();
		}
		resetLocalDB();
		try {
			window = new MainWindow();
			window.open(importDb);
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static FileLFD getDonorDB() {
		if (localDB == null) localDB = new FileLFD();
		return localDB;
	}
	public static void resetLocalDB() {
		try {
			localDB = new FileLFD(getSetting("lastDB"));
		} catch (IOException e) {
			// TODO Display gui error message about this
			e.printStackTrace();
		}
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
			settings.storeToXML(new FileOutputStream(path),"LibreFundraiser");
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
		} catch (Exception e) {
		}
		return realFile;
	}
	public static boolean fileCreationPossible(String path) throws IOException {
		if (fileExists(path)) {
			throw new IOException(String.format("The file \"%s\" already exists.", path));
		}
		boolean canCreate = true;
		try {
			File file = new File(path);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(" ");
			writer.close();
			if (file.exists()) canCreate = true;
			file.delete();
		} catch (Exception e) {
		}
		return canCreate;
	}
	public static String newDbFilePrompt(Shell shell) {
		FileDialog fileDialog = new FileDialog(shell,SWT.SAVE);
		fileDialog.setFilterExtensions(new String[]{"*.lfd","*.*"});
		fileDialog.setFilterNames(new String[]{"LibreFundraiser Database (*.lfd)","All Files"});
		String path = "";
		boolean goodPath = false;
		while (!goodPath) {
			try {
				do {
					path = fileDialog.open();
				} while(!fileCreationPossible(path));
				goodPath = true;
			} catch (IOException e) {
				File file = new File(path);
				MessageBox verify = new MessageBox(shell,SWT.YES | SWT.NO | SWT.ICON_WARNING);
				verify.setMessage(file.getName() + " already exists. Do you want to overwrite it?");
				verify.setText("LibreFundraiser Warning");
				goodPath = verify.open() == SWT.YES;
			}
		}
		return path;
	}
	
	public static void importFromFRBW(final Display display, final Shell parent, final MainWindow mainWindow, final String path) {
		if (path == null) return;
		new Thread(new Runnable() {
			public void run() {
				FileDBASE db = new FileDBASE(path);
				Donor[] importedDonors = new Donor[] {};
				try {
					importedDonors = db.importFRBW();
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox error = new MessageBox(parent,SWT.ICON_ERROR);
					error.setText("LibreFundraiser Error");
					error.setMessage("Could not load donors. This probably isn't a FundRaiser basic installation folder...");
				}
				Main.getDonorDB().saveDonors(importedDonors);
				Main.getWindow().refresh(true, true);				
			}
		}).start();
	}
}
