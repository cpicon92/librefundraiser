import java.text.NumberFormat;

import org.eclipse.swt.widgets.Display;


public class LibreFundraiser {
	private static SQLite localDB = null;
	private static NumberFormat currency = null;
	public static void main(String args[]) {
		try {
			FileDBASE db = new FileDBASE("C:\\FRBW");
			db.loadTable("Master.dbf","donors");
			db.loadTable("Gifts.dbf","gifts");
			MainWindow window = new MainWindow();
			window.setBlockOnOpen(true);
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
	public static String money(double amount) {
		if (currency == null) currency = NumberFormat.getCurrencyInstance();
		return currency.format(amount);
	}
	public static String money(String amount) {
		return money(Double.parseDouble(amount));
	}
}
