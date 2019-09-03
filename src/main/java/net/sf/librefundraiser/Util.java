package net.sf.librefundraiser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.nebula.widgets.opal.dialog.Dialog;
import org.eclipse.nebula.widgets.opal.dialog.Dialog.OpalDialogType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.jopendocument.dom.OOUtils;
import org.jopendocument.dom.spreadsheet.Column;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.io.GiftStats;

public class Util {

	public static void writeODS(final List<Donor> donors, final File f, final boolean openFile) {
		final String[] headers = { "Account", "Type", "Last Name/Business",
				"First Name", "Spouse/Contact Last", "Spouse/Contact First",
				"Salutation", "Home Phone", "Work Phone", "Fax", "Category",
				"Donor Source", "Mail Name", "Address 1", "Address 2", "City",
				"State", "Zip", "Country", "Email", "Other Email", "Web",
				"Last Change", "Last Gift Date", "Last Gift", "Total Gifts",
				"Year-to-date", "First Gift", "Largest Gift",
				"Last Entry Date", "Last Entry Amount", "Notes" };
		new Thread(new Runnable(){
			@Override
			public void run() {
				ArrayDeque<String[]> l = new ArrayDeque<>();
				for (Donor d : donors) {
					String[] row = new String[headers.length+1];
					for (int i = 0; i < row.length-1; i++) {
						//TODO fix this
//						row[i] = d.getData(headers[i][1]);
					}
					String notes = d.data.getNotes();
					boolean valid = false;
					try {
						valid = !notes.contains(new String(new char[]{(char)0}));
					} catch (Exception e) {
					}
					row[headers.length] = valid?d.getData("notes"):"";
					l.add(row);
				}
				l.addFirst(headers);
				String[][] sheetDataWithTitles = l.toArray(new String[][]{});
				try {
//					SpreadSheet outputSpreadSheet = SpreadSheet.createEmpty(model);
					SpreadSheet outputSpreadSheet = SpreadSheet.createEmpty(new DefaultTableModel(headers, donors.size()));
					Sheet donorSheet = outputSpreadSheet.getSheet(0);
					int r = 0;
					for (Donor d : donors) {
						donorSheet.getCellAt(0, r++).setValue(d.getAccountNum());
					}
					donorSheet.setName("Donors");
					final Display display = Display.getDefault();
					final double[] pixelsPerMm = new double[1];
					final GC[] gcA = new GC[1];
					display.syncExec(new Runnable(){
						@Override
						public void run() {
							final GC gc = new GC(new Image(display, new Rectangle(0,0,10,10)));
							final Font arial = new Font(display, new FontData("Arial", 10, SWT.NORMAL));
							gc.setFont(arial);
							pixelsPerMm[0] = (display.getDPI().x)/25.4;
							gcA[0] = gc;
						}
					});
					final GC gc = gcA[0];
					//stupid hack to optimize column width since jopendocument makes it impossibly hard to do this otherwise
					for (int i = 0; i < donorSheet.getColumnCount(); i++) {
						Column<SpreadSheet> c = donorSheet.getColumn(i);
						double maxWidth = 0;
						for (int j = 0; j < sheetDataWithTitles.length; j++) {
							String[] cellLines = sheetDataWithTitles[j][i].split("\n");
							for (String line : cellLines) {
								//padding and mmWidth (and maxWidth) are in mm
								double padding = 4;
								double mmWidth = ((gc.stringExtent(line).x)/pixelsPerMm[0])*1.1+padding;
								if (mmWidth > maxWidth) {
									maxWidth = mmWidth;
								}
							}
						}
						c.setWidth(maxWidth);
					}
					outputSpreadSheet.saveAs(f);
					if (openFile) OOUtils.open(f);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	
	public static void writeCSV(List<Donor> donors, File file) {
		//TODO make this universal somehow
		final String[] headers = { "Account", "Type", "Last Name/Business",
				"First Name", "Spouse/Contact Last", "Spouse/Contact First",
				"Salutation", "Home Phone", "Work Phone", "Fax", "Category",
				"Donor Source", "Mail Name", "Address 1", "Address 2", "City",
				"State", "Zip", "Country", "Email", "Other Email", "Web",
				"Last Change", "Last Gift Date", "Last Gift", "Total Gifts",
				"Year-to-date", "First Gift", "Largest Gift",
				"Last Entry Date", "Last Entry Amount", "Notes" };
		try (CSVPrinter csv = CSVFormat.EXCEL.withHeader(headers).print(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
			for (Donor d : donors) {
				GiftStats stats = d.getGiftStats();
				csv.printRecord(
					d.getAccountNum(),
					d.data.getType().toString(), d.data.getLastname(),
					d.data.getFirstname(), d.data.getSpouselast(),
					d.data.getSpousefrst(), d.data.getSalutation(),
					d.data.getHomephone(), d.data.getWorkphone(),
					d.data.getFax(), d.data.getCategory(),
					d.data.getSource(), d.data.getMailname(),
					d.data.getAddress1(), d.data.getAddress2(),
					d.data.getCity(), d.data.getState(), d.data.getZip(),
					d.data.getCountry(), d.data.getEmail(),
					d.data.getEmail2(), d.data.getWeb(),
					csvDate(d.data.getChangedate()),
					csvDate(stats.getLastGiveDt()),
					stats.getLastAmt().toString(""),
					stats.getAllTime().toString(""),
					stats.getYearToDt().toString(""),
					csvDate(stats.getFirstGift()),
					stats.getLargest().toString(""),
					csvDate(stats.getLastEntDt()),
					stats.getLastEntAmt().toString(""), 
					d.data.getNotes() 
				);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error writing CSV file", e);
		}
	}
	
	public static String csvDate(Date d) {
		return d != null ? Main.getDateFormat().format(d) : "Never";
	}

	public static CSVParser readCSV(File f, CSVFormat format) throws IOException {
		try {
			return format.parse(new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8")));
		} catch (Exception e) {
			throw new IOException("Could not parse "+f.getName()+" as CSV", e);
		}
	}
	
	public static String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
	}
	
	public static void exceptionError(Exception e) {
		e.printStackTrace();
		final Dialog dialog = new Dialog();
		dialog.setTitle("LibreFundraiser Takes Exception!");
		dialog.getMessageArea().setTitle("Illegal Action: " + e.getClass().getSimpleName())
		.setText("Send detailed error below to a developer for help.")
		.setIcon(Display.getCurrent().getSystemImage(SWT.ICON_ERROR));
		dialog.setButtonType(OpalDialogType.OK);
		dialog.getFooterArea().setExpanded(false).setDetailText(Util.getStackTrace(e));
		dialog.show();
	}
}
