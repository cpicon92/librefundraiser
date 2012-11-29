package net.sf.librefundraiser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import net.sf.librefundraiser.db.IDonorDB;
import net.sf.librefundraiser.db.SQLite;
import net.sf.librefundraiser.gui.DonorList;
import net.sf.librefundraiser.gui.FundRaiserImportDialog;
import net.sf.librefundraiser.gui.MainWindow;
import net.sf.librefundraiser.gui.NewDatabaseWizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;

public class Main {
	private static IDonorDB localDB = null;
	private static NumberFormat currency = null;
	private static MainWindow window;
	private final static Properties settings = new Properties();
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final String version = "(Development Snapshot)";

	//TODO: quicksearch should search spouse names too
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
		final FundRaiserImportDialog dialog = new FundRaiserImportDialog(parent,SWT.NONE);
		if (path == null) return;
		new Thread(new Runnable() {
			public void run() {
				FileDBASE db = new FileDBASE(path);
				display.asyncExec(new Runnable() {
					public void run() {
						dialog.setCancelable(false);
						dialog.setStatusText("Importing donor list...");
					}
				});
				if (!db.loadTable("Master.dbf","donors")) {
					display.asyncExec(new Runnable() {
						public void run() {
							MessageBox error = new MessageBox(parent,SWT.ICON_ERROR);
							error.setText("LibreFundraiser Error");
							error.setMessage("Could not load donors. This probably isn't a FundRaiser basic installation folder...");
							dialog.dispose();
						}
					});
					return;
				}
				display.asyncExec(new Runnable() {
					public void run() {
						dialog.setProgress(25);
						dialog.setStatusText("Importing gifts...");
					}
				});
				db.loadTable("Gifts.dbf","gifts");
				if (mainWindow != null) {
					display.asyncExec(new Runnable() {
						public void run() {
							dialog.setProgress(50);
							dialog.setStatusText("Consolidating donors and gifts...");
						}
					});
					final DonorList compositeDonorList = mainWindow.getCompositeDonorList();
					compositeDonorList.donors = Main.getDonorDB().getDonors();
					display.asyncExec(new Runnable() {
						public void run() {
							dialog.setProgress(75);
							dialog.setStatusText("Refreshing donor list...");
						}
					});
					display.asyncExec(new Runnable() {
						public void run() {
							mainWindow.refresh(false);
						}
					});
				}
				display.asyncExec(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});
				
			}
		}).start();
		dialog.open();
	}
}
