package net.sf.librefundraiser.gui;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ResourceManager;
import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.io.GiftStats;
import net.sf.librefundraiser.tabs.TabFolder;
import net.sf.librefundraiser.tabs.TabItem;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jopendocument.dom.OOUtils;
import org.jopendocument.dom.spreadsheet.Column;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import au.com.bytecode.opencsv.CSVWriter;



public class DonorTable extends Composite {
	private Table table;

	final static public String[][] columns = { { "Account", "account" },
		{ "Type", "type" }, { "Last Name/Business", "lastname" },
		{ "First Name", "firstname" },
		{ "Spouse/Contact Last", "spouselast" },
		{ "Spouse/Contact First", "spousefrst" },
		{ "Salutation", "salutation" }, { "Home Phone", "homephone" },
		{ "Work Phone", "workphone" }, { "Fax", "fax" },
		{ "Category", "category1" }, { "Donor Source", "category2" },
		{ "Mail Name", "mailname" }, { "Address 1", "address1" },
		{ "Address 2", "address2" }, { "City", "city" },
		{ "State", "state" }, { "Zip", "zip" }, { "Country", "country" },
		{ "Email", "email" }, { "Other Email", "email2" }, { "Web", "web" }, { "Last Change", "changedate" },
		{ "Last Gift Date", "lastgivedt" }, { "Last Gift", "lastamt" },
		{ "Total Gifts", "alltime" }, { "Year-to-date", "yeartodt" },
		{ "First Gift", "firstgift" }, { "Largest Gift", "largest" } , { "Last Entry Date", "lastentdt" }, { "Last Entry Amount", "lastentamt" } };

	public Donor[] donors = null;

	private TableViewer tableViewer;

	private TabFolder tabFolder;


	public static class DonorLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object donor, int columnIndex) {
			Object txt  = this.getColumnData((Donor) donor, columnIndex);
			if (txt instanceof Date) {
				txt = Main.getDateFormat().format((Date) txt);
			}
			return String.valueOf(txt);
		}
		
		public Object getColumnData(Donor donor, int columnIndex) {
			GiftStats stats = donor.getGiftStats();
			Object data;
			switch (columnIndex) {
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
			default:
				data = "Missing Data";
			}
			return data;
		}
	}
	private static class ContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}
		@Override
		public void dispose() {
		}
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public DonorTable(Composite parent, int style) {
		super(parent, style);
		if (donors == null) donors = new Donor[] {};
		this.setLayout(new FillLayout(SWT.HORIZONTAL));


		Composite compositeTable = new Composite(this, SWT.NONE);
		GridLayout gl_compositeTable = new GridLayout(2, false);
		gl_compositeTable.marginHeight = 0;
		gl_compositeTable.marginWidth = 0;
		compositeTable.setLayout(gl_compositeTable);
		
		Label lblFilter = new Label(compositeTable, SWT.NONE);
		lblFilter.setAlignment(SWT.RIGHT);
		GridData gd_lblFilter = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_lblFilter.widthHint = 20;
		lblFilter.setLayoutData(gd_lblFilter);
		lblFilter.setImage(ResourceManager.getIcon("filter.png"));
		
		final DonorListFilter tableFilter = new DonorListFilter();
		
		final Text txtFilter = new Text(compositeTable, SWT.BORDER);
		txtFilter.addModifyListener(new ModifyListener() {
			private long recentId;
			@Override
			public void modifyText(ModifyEvent e) {
				recentId = System.currentTimeMillis();
				DonorTable.this.getDisplay().timerExec(300, new Runnable() {
					long id = recentId;
					@Override
					public void run() {
						if (id != recentId) return;
						tableFilter.setFilter(txtFilter.getText());
						tableViewer.refresh();
					}
				});
			}
		});
		txtFilter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		tableViewer = new TableViewer(compositeTable, SWT.FULL_SELECTION | SWT.MULTI);
		tableViewer.setFilters(new ViewerFilter[] {tableFilter});
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				TableItem selectedItem = table.getSelection()[0];
				int id = Integer.parseInt(selectedItem.getText(columnSearch("account")));
				DonorTab newTab = DonorTable.this.openDonorTab(id);
				DonorTable.this.tabFolder.setSelection(newTab);
			}
		});
		table.setHeaderVisible(true);

		for (String[] c : columns) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tableColumn = tableViewerColumn.getColumn();
			tableColumn.setWidth(100);
			tableColumn.setText(c[0]);
		}

		tableViewer.setLabelProvider(new DonorLabelProvider());
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setInput(donors);
		new DonorListSorter(tableViewer);

		Menu menuDonorList = new Menu(table);
		table.setMenu(menuDonorList);

		MenuItem mntmOpenDonor = new MenuItem(menuDonorList, SWT.NONE);
		mntmOpenDonor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final TableItem[] items = table.getSelection();
				final int[] i = {0};
				for (TableItem selectedItem : items) {
					final int id = Integer.parseInt(selectedItem.getText(columnSearch("account")));
					new Thread(new Runnable() {
						@Override
						public void run() {
							final Donor[] donor = {Main.getDonorDB().getDonor(id)};
							if (donor[0] == null) {
								donor[0] = new Donor(id);
							}
							getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									DonorTab dt = DonorTable.this.openDonorTab(donor[0]);
									if (i[0] == items.length - 1) {
										DonorTable.this.tabFolder.setSelection(dt);
									}
									i[0]++;
								}
							});
						}
					}).start();
				}
			}
		});
		mntmOpenDonor.setText("Open Donor(s)");

		MenuItem mntmOpenBackground = new MenuItem(menuDonorList, SWT.NONE);
		mntmOpenBackground.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (TableItem selectedItem : table.getSelection()) {
					final int id = Integer.parseInt(selectedItem.getText(columnSearch("account")));
					new Thread(new Runnable() {
						@Override
						public void run() {
							final Donor[] donor = new Donor[] {Main.getDonorDB().getDonor(id)};
							if (donor[0] == null) {
								donor[0] = new Donor(id);
							}
							getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									DonorTable.this.openDonorTab(donor[0]);
								}
							});
						}
					}).start();
				}
			}
		});
		mntmOpenBackground.setText("Open Donor(s) in the Background");

		new MenuItem(menuDonorList, SWT.SEPARATOR);

		MenuItem mntmDeleteDonor = new MenuItem(menuDonorList, SWT.NONE);
		mntmDeleteDonor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteDonors();
			}
		});
		mntmDeleteDonor.setText("Delete Donor(s)");
		packColumns();
	}

	public void refresh() {
		setVisible(false);
		final long beforetime = System.currentTimeMillis();
		tableViewer.setInput(donors);
//		tableViewer.refresh();
		packColumns();
		System.out.printf("List refresh took: %ds\n", (System.currentTimeMillis()-beforetime)/1000);
		setVisible(true);
	}


	public void packColumns() {
		for (TableColumn tc : table.getColumns()) {
			tc.pack();
		}
	}

	@Deprecated
	private static int columnSearch(String columnName) {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i][1].equals(columnName)) return i;
		}
		return -1;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void newDonor() {
		int id = Main.getDonorDB().getMaxAccount()+1;
		for (TabItem t : tabFolder.getItems()) {
			try {
				DonorTab dt = (DonorTab) t;
				int dtId = dt.getDonor().getId();
				if (dtId >= id) id = dtId+1;
			} catch (Exception e) {
			}
		}
		tabFolder.setSelection(DonorTable.this.openDonorTab(id));
	}

	public void saveAll() {
		for (TabItem i : tabFolder.getItems()) {
			if (i.getClass().equals(DonorTab.class)) {
				((DonorTab)i).save(false);
			}
		}
		TabItem t = tabFolder.getSelection();
		if (!t.getClass().equals(DonorTab.class)) {
			Main.getWindow().getSaveButton().setEnabled(false);
		} else {
			((DonorTab)t).alterSaveButton();
		}
		Main.getWindow().refresh(true, false);
	}

	public void deleteDonors() {
		MessageBox warning = new MessageBox(this.getShell(),SWT.ICON_WARNING|SWT.YES|SWT.NO);
		warning.setText("LibreFundraiser Warning");
		warning.setMessage("All data for these donors will be erased IRRETRIEVABLY. Do you want to continue?");
		if (warning.open() == SWT.NO) return;
		int[] ids = new int[table.getSelectionCount()];
		int i = 0;
		for (TableItem selectedItem : table.getSelection()) {
			int id = Integer.parseInt(selectedItem.getText(columnSearch("account")));
			ids[i] = id;
			i++;
		}
		Main.getDonorDB().deleteDonors(ids);
		Main.getWindow().refresh(true, false);
	}

	public boolean closeAllTabs() {
		for (TabItem closing : tabFolder.getItems()) {
			if (!closing.getText().substring(0, 1).equals("*")) continue;
			MessageBox verify = new MessageBox(getShell(),SWT.YES | SWT.NO | SWT.ICON_WARNING);
			verify.setMessage(closing.getText().substring(1)+" has unsaved changes, are you sure you want to close this donor?");
			verify.setText("LibreFundraiser Warning");
			if (verify.open() != SWT.YES) {
				return false;
			}
			closing.dispose();
		}
		return true;
	}

	public void writeCSV(File f) {
		ArrayList<String[]> l = new ArrayList<>();
		String[] columnTitles = new String[columns.length+1];
		columnTitles[columns.length] = "Notes";
		for (int i = 0; i < columnTitles.length-1; i++) {
			columnTitles[i] = columns[i][0];
		}
		l.add(columnTitles);
		for (Donor d : donors) {
			String[] row = new String[columns.length+1];
			for (int i = 0; i < row.length-1; i++) {
				row[i] = d.getData(columns[i][1]);
			}
			String notes = d.getData("notes");
			boolean valid = false;
			try {
				valid = !notes.contains(new String(new char[]{(char)0}));
			} catch (Exception e) {
			}
			row[columns.length] = valid?d.getData("notes"):"";
			l.add(row);
		}
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(f));
			writer.writeAll(l);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeODS(final File f, final boolean openFile) {
		new Thread(new Runnable(){
			@Override
			public void run() {
				ArrayDeque<String[]> l = new ArrayDeque<>();
				String[] columnTitles = new String[columns.length+1];
				columnTitles[columns.length] = "Notes";
				for (int i = 0; i < columnTitles.length-1; i++) {
					columnTitles[i] = columns[i][0];
				}
				for (Donor d : donors) {
					String[] row = new String[columns.length+1];
					for (int i = 0; i < row.length-1; i++) {
						row[i] = d.getData(columns[i][1]);
					}
					String notes = d.getData("notes");
					boolean valid = false;
					try {
						valid = !notes.contains(new String(new char[]{(char)0}));
					} catch (Exception e) {
					}
					row[columns.length] = valid?d.getData("notes"):"";
					l.add(row);
				}
				String[][] sheetData = l.toArray(new String[][]{});
				l.addFirst(columnTitles);
				String[][] sheetDataWithTitles = l.toArray(new String[][]{});
				TableModel model = new DefaultTableModel(sheetData, columnTitles); 
				try {
					SpreadSheet outputSpreadSheet = SpreadSheet.createEmpty(model);
					Sheet donorSheet = outputSpreadSheet.getSheet(0);
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
					//stupid hack to optimise column width since jopendocument makes it impossibly hard to do this otherwise
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

	public TabFolder getTabFolder() {
		return tabFolder;
	}

	public void setTabFolder(TabFolder tabFolder) {
		this.tabFolder = tabFolder;
	}
	
	public DonorTab openDonorTab(Donor donor) {
		return new DonorTab(donor, DonorTable.this.tabFolder);
	}	
	
	public DonorTab openDonorTab(int id) {
		return new DonorTab(id, DonorTable.this.tabFolder);
	}
}
