import java.text.NumberFormat;
import java.text.ParseException;

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
	public static String toMoney(double amount) {
		if (currency == null) currency = NumberFormat.getCurrencyInstance();
		return currency.format(amount);
	}
	public static String toMoney(String amount) {
		try {
			return toMoney(Double.parseDouble(amount));
		} catch (Exception e) {
			System.err.println("Value could not be parsed as money.");
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
}
