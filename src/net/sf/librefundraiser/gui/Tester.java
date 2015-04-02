package net.sf.librefundraiser.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.db.FileLFD;
import net.sf.librefundraiser.gui.flextable.FlexTable;
import net.sf.librefundraiser.gui.flextable.FlexTableDataProvider;
import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.io.GiftStats;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Tester extends Shell {
	
	
	public static void main(String[] args) {
		Shell s = new Tester();
		s.open();
		final Display display = s.getDisplay();
		while (!s.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public Tester() {
		super();
		FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
		fillLayout.marginHeight = 10;
		fillLayout.marginWidth = 10;
		setLayout(fillLayout);
//		DateTime dateTime = new DateTime(this, SWT.BORDER | SWT.CALENDAR);
//		dateTime.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//			}
//		});
		Main.loadSettings();
		FileLFD lfd;
		try {
			lfd = new FileLFD(Main.getSetting("lastDB"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		final List<Donor> donors = lfd.getDonors();
		Collections.sort(donors, new Comparator<Donor>() {
			@Override
			public int compare(Donor d0, Donor d1) {
				return Integer.compare(d0.id, d1.id);
			}
		});
		final String[] headers = { "Account", "Type", "Last Name/Business",
				"First Name", "Spouse/Contact Last", "Spouse/Contact First",
				"Salutation", "Home Phone", "Work Phone", "Fax", "Category",
				"Donor Source", "Mail Name", "Address 1", "Address 2", "City",
				"State", "Zip", "Country", "Email", "Other Email", "Web",
				"Last Change", "Last Gift Date", "Last Gift", "Total Gifts",
				"Year-to-date", "First Gift", "Largest Gift",
				"Last Entry Date", "Last Entry Amount", "Notes" };
		FlexTable<Donor> dt = new FlexTable<>(this, SWT.BORDER);
		dt.setDataProvider(new FlexTableDataProvider<Donor>() {
			@Override
			public int size() {
				return donors.size();
			}
			
			@Override
			public String[] getHeaders() {
				return headers;
			}
			
			@Override
			public String get(int i, int field) {
				Donor donor = this.get(i);
				GiftStats stats = donor.getGiftStats();
				Object data;
				switch (field) {
				case 0:
					data = donor.getAccountNum();
					break;
				case 1:
					data = donor.data.getType();
					break;
				case 2:
					data = donor.data.getLastname();
					break;
				case 3:
					data = donor.data.getFirstname();
					break;
				case 4:
					data = donor.data.getSpouselast();
					break;
				case 5:
					data = donor.data.getSpousefrst();
					break;
				case 6:
					data = donor.data.getSalutation();
					break;
				case 7:
					data = donor.data.getHomephone();
					break;
				case 8:
					data = donor.data.getWorkphone();
					break;
				case 9:
					data = donor.data.getFax();
					break;
				case 10:
					data = donor.data.getCategory1();
					break;
				case 11:
					data = donor.data.getCategory2();
					break;
				case 12:
					data = donor.data.getMailname();
					break;
				case 13:
					data = donor.data.getAddress1();
					break;
				case 14:
					data = donor.data.getAddress2();
					break;
				case 15:
					data = donor.data.getCity();
					break;
				case 16:
					data = donor.data.getState();
					break;
				case 17:
					data = donor.data.getZip();
					break;
				case 18:
					data = donor.data.getCountry();
					break;
				case 19:
					data = donor.data.getEmail();
					break;
				case 20:
					data = donor.data.getEmail2();
					break;
				case 21:
					String url = donor.data.getWeb();
					// display web urls on one line in table
					url = url.replace("\n", "; ");
					// remove trailing semi-colon
					if (url.length() > 2) {
						url = url.substring(0, url.length() - 2);
					}
					data = url;
					break;
				case 22:
					data = donor.data.getChangedate();
					break;
				case 23:
					data = stats.getLastGiveDt();
					break;
				case 24:
					data = stats.getLastAmt();
					break;
				case 25:
					data = stats.getAllTime();
					break;
				case 26:
					data = stats.getYearToDt();
					break;
				case 27:
					data = stats.getFirstGift();
					break;
				case 28:
					data = stats.getLargest();
					break;
				case 29:
					data = stats.getLastEntDt();
					break;
				case 30:
					data = stats.getLastEntAmt();
					break;
				case 31:
					data = donor.data.getNotes();
					break;
				default:
					data = "Missing Data";
				}
				if (data instanceof Date) {
					data = formatDate((Date) data);
				}
				return String.valueOf(data);
			}
			
			@Override
			public Donor get(int i) {
				return donors.get(i);
			}
			
			@Override
			public int columnCount() {
				return headers.length;
			}

			@Override
			public void refresh() {
				
			}
		});

	}
	
	public static String formatDate(Date d) {
		return d != null ? Main.getDateFormat().format(d) : "Never";
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
