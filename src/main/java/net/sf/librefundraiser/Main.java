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
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import net.sf.librefundraiser.db.FileDBASE;
import net.sf.librefundraiser.db.FileLFD;
import net.sf.librefundraiser.db.FileLFD.DatabaseIOException;
import net.sf.librefundraiser.gui.MainWindow;
import net.sf.librefundraiser.gui.NewDatabaseWizard;
import net.sf.librefundraiser.io.Donor;

//TODO: fix fixed-width dialogs to render properly on high-dpi displays
public class Main {
	private static FileLFD localDB = null;
	private static NumberFormat currency = null;
	private static MainWindow window;
	//TODO make locale a setting
	private static Locale locale = Locale.US;
	private final static Properties settings = new Properties();
	private static final String dateFormatString = "yyyy-MM-dd";
	private static final DateFormat dateFormat = new SimpleDateFormat(getDateFormatString());
//	private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
	public static final String version = "(Development Snapshot)";

	public static void main(String args[]) {
		try {
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
			window = new MainWindow();
			window.open(importDb);
		} catch (Exception e) {
			Util.exceptionError(e);
		}
		Display.getCurrent().dispose();
	}
	public static FileLFD getDonorDB() {
		if (localDB == null) localDB = new FileLFD();
		return localDB;
	}
	public static void resetLocalDB() {
		try {
			localDB = new FileLFD(getSetting("lastDB"));
		} catch (IOException e) {
			throw new DatabaseIOException("Could not reset local DB", e);
		}
	}
	public static String toMoney(double amount) {
		if (currency == null) currency = NumberFormat.getCurrencyInstance();
		return currency.format(amount);
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
			@Override
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
	public static String getDateFormatString() {
		return dateFormatString;
	}

	//I'm keeping this method written this way because it's hilarious
	public static boolean fileExists (String path) {
		boolean realFile = false;
		try {
			File file = new File(path);
			if (file.exists()) realFile = true;
		} catch (Exception e) {
		}
		return realFile;
	}
	public static boolean pathWritable(String path) {
		boolean canCreate = false;
		try {
			File file = new File(path);
			if (file.exists()) return file.canWrite();
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
		String lastBrowse = Main.getSetting("lastBrowse");
		if (lastBrowse != null) fileDialog.setFilterPath(lastBrowse);
		String path = "";
		boolean goodPath = false;
		while (!goodPath) {
			do {
				path = fileDialog.open();
				if (path == null) {
					goodPath = true;
					break;
				}
				File file = new File(path);
				goodPath = !file.exists();
				if (!goodPath) {
					MessageBox verify = new MessageBox(shell,SWT.YES | SWT.NO | SWT.ICON_WARNING);
					verify.setMessage(file.getName() + " already exists. Do you want to overwrite it?");
					verify.setText("LibreFundraiser Warning");
					int r = verify.open();
					goodPath = r == SWT.YES;
				}
			} while(!pathWritable(path));
		}
		Main.addSetting("lastBrowse", fileDialog.getFilterPath());
		return path;
	}
	
	public static void importFromFRBW(final Display display, final Shell parent, final MainWindow mainWindow, final String path) {
		if (path == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				FileDBASE db = new FileDBASE(path);
				Donor[] importedDonors = new Donor[] {};
				try {
					importedDonors = db.importFRBW();
				} catch (Exception e) {
					e.printStackTrace();
					display.asyncExec(new Runnable() {
						public void run() {
							MessageBox error = new MessageBox(parent,SWT.ICON_ERROR);
							error.setText("LibreFundraiser Error");
							error.setMessage("Could not load donors. This probably isn't a FundRaiser basic installation folder...");
							error.open();
						}
					});
				}
				Main.getDonorDB().saveDonors(importedDonors);
				Main.getWindow().refresh();				
			}
		}).start();
	}
	public static Locale getLocale() {
		return locale;
	}
}
