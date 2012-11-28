package net.sf.librefundraiser.gui;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import net.sf.librefundraiser.Donor;
import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ResourceManager;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jopendocument.dom.OOUtils;
import org.jopendocument.dom.spreadsheet.Column;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import au.com.bytecode.opencsv.CSVWriter;



public class DonorList extends Composite {
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
		{ "First Gift", "firstgift" }, { "Largest Gift", "largest" } };
	public Donor[] donors = null;

	private TableViewer tableViewer;

	public CTabFolder tabFolder;

	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			Donor donor = (Donor) element;
			if (columns[columnIndex][1].matches("(yeartodt|lastamt|largest|alltime)")) {
				return Main.toMoney(donor.getData(columns[columnIndex][1]));
			}
			if (columns[columnIndex][1].matches("web")) {
				String urls = donor.getData(columns[columnIndex][1]);
				urls = urls.replace("\n", "; ");
				if (urls.length() > 2) urls = urls.substring(0, urls.length()-2);
				return urls;
			}
			return donor.getData(columns[columnIndex][1]);
		}
	}
	private static class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public DonorList(Composite parent, int style) {
		super(parent, style);
		donors = Main.getDonorDB().getDonors();
		this.setLayout(new FillLayout(SWT.HORIZONTAL));
		tabFolder = new CTabFolder(this, SWT.FLAT);
		tabFolder.setTabHeight(20);
		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				CTabItem closing = ((CTabItem)event.item);
				if (!closing.getText().substring(0, 1).equals("*")) return;
				MessageBox verify = new MessageBox(getShell(),SWT.YES | SWT.NO | SWT.ICON_WARNING);
				verify.setMessage(closing.getText().substring(1)+" has unsaved changes, are you sure you want to close this donor?");
				verify.setText("LibreFundraiser Warning");
				event.doit = verify.open() == SWT.YES;
			}
		});
		tabFolder.setSelectionBackground(new Color[]{this.getDisplay().getSystemColor(SWT.COLOR_WHITE), this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)}, new int[]{40}, true);
		tabFolder.setRenderer(new CTabFolderRenderer(tabFolder) {
			protected void draw(int part, int state, Rectangle bounds, GC gc)  {
				switch (part) {
				case PART_BACKGROUND:
					break;
				case PART_BODY:
					break;
				case PART_HEADER:
					break;
				case PART_MAX_BUTTON:
					super.draw(part, state, bounds, gc);
					break;
				case PART_MIN_BUTTON:
					super.draw(part, state, bounds, gc);
					break;
				case PART_CHEVRON_BUTTON:
					super.draw(part, state, bounds, gc);
					break;
				case PART_BORDER:
					break;
				case PART_CLOSE_BUTTON:
					super.draw(part, state, bounds, gc);
					break;
				default:
					if (0 <= part && part < parent.getItemCount()) {
						if (bounds.width == 0 || bounds.height == 0) return;
						super.draw(part, state, bounds, gc);
						gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
						int x = bounds.x;
						int y = bounds.y;
						int width = bounds.width;
						if (part > 0 && part < parent.getSelectionIndex()) {
							width += 3;
						}
						if (part > parent.getSelectionIndex()) {
							x -= 2;
						}
						if (part == parent.getSelectionIndex() && part == parent.getItemCount() - 1) {
							width -= 10;
						}
						int[] shape = new int[]{x,y,x+width,y};
						gc.drawPolyline(shape);
					}

					break;
				}
			}
		});

		final CTabItem tbtmDonors = new CTabItem(tabFolder, SWT.NONE);
		tbtmDonors.setImage(ResourceManager.getIcon("home-tab.png"));
		tbtmDonors.setText("Donors");

		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CTabItem t = tabFolder.getSelection();
				if (!t.getClass().equals(DonorTab.class)) {
					Main.getSaveButton().setEnabled(false);
				} else {
					((DonorTab)t).alterSaveButton();
				}
			}
		});
		Composite compositeTable = new Composite(tabFolder, SWT.NONE);
		GridLayout gl_compositeTable = new GridLayout(1, false);
		gl_compositeTable.marginHeight = 0;
		gl_compositeTable.marginWidth = 0;
		compositeTable.setLayout(gl_compositeTable);
		tableViewer = new TableViewer(compositeTable, SWT.FULL_SELECTION | SWT.MULTI);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				TableItem selectedItem = table.getSelection()[0];
				int id = Integer.parseInt(selectedItem.getText(columnSearch("account")));
				DonorTab newTab = new DonorTab(id,tabFolder);
				tabFolder.setSelection(newTab);
			}
		});
		table.setHeaderVisible(true);
		tbtmDonors.setControl(compositeTable);

		for (String[] c : columns) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tableColumn = tableViewerColumn.getColumn();
			tableColumn.setWidth(100);
			tableColumn.setText(c[0]);
		}

		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setInput(donors);
		tabFolder.setSelection(0);
		new DonorListSorter(tableViewer);

		Menu menuDonorList = new Menu(table);
		table.setMenu(menuDonorList);

		MenuItem mntmOpenDonor = new MenuItem(menuDonorList, SWT.NONE);
		mntmOpenDonor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DonorTab newTab = null;
				for (TableItem selectedItem : table.getSelection()) {
					int id = Integer.parseInt(selectedItem.getText(columnSearch("account")));
					newTab = new DonorTab(id,tabFolder);
				}
				if (newTab != null) tabFolder.setSelection(newTab);
			}
		});
		mntmOpenDonor.setText("Open Donor(s)");

		MenuItem mntmOpenBackground = new MenuItem(menuDonorList, SWT.NONE);
		mntmOpenBackground.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (TableItem selectedItem : table.getSelection()) {
					int id = Integer.parseInt(selectedItem.getText(columnSearch("account")));
					new DonorTab(id,tabFolder);
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

	public void refresh(boolean pack) {
		tableViewer.setInput(donors);
		tableViewer.refresh();
		if (pack) packColumns();
	}

	public void refresh() {
		refresh(true);
	}

	public void packColumns() {
		for (TableColumn tc : table.getColumns()) {
			tc.pack();
		}
	}

	private static int columnSearch(String columnName) {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i][1].equals(columnName)) return i;
		}
		return -1;
	}

	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void newDonor() {
		int id = Main.getDonorDB().getMaxAccount()+1;
		for (CTabItem t : tabFolder.getItems()) {
			try {
				DonorTab dt = (DonorTab) t;
				int dtId = dt.getDonor().getId();
				if (dtId >= id) id = dtId+1;
			} catch (Exception e) {
			}
		}
		tabFolder.setSelection(new DonorTab(id,tabFolder));
	}

	public void saveAll() {
		for (CTabItem i : tabFolder.getItems()) {
			if (i.getClass().equals(DonorTab.class)) {
				((DonorTab)i).save();
			}
		}
		CTabItem t = tabFolder.getSelection();
		if (!t.getClass().equals(DonorTab.class)) {
			Main.getSaveButton().setEnabled(false);
		} else {
			((DonorTab)t).alterSaveButton();
		}
	}

	public void deleteDonors() {
		MessageBox warning = new MessageBox(this.getShell(),SWT.ICON_WARNING|SWT.YES|SWT.NO);
		warning.setText("LibreFundraiser Warning");
		warning.setMessage("All data for these donors will be erased IRRETRIEVABLY. Do you want to continue?");
		if (warning.open() == SWT.NO) return;
		for (TableItem selectedItem : table.getSelection()) {
			int id = Integer.parseInt(selectedItem.getText(columnSearch("account")));
			Main.getDonorDB().deleteDonor(id);
		}
		donors = Main.getDonorDB().getDonors();
		refresh(false);
	}

	public boolean closeAllTabs() {
		for (CTabItem closing : tabFolder.getItems()) {
			if (!closing.getText().substring(0, 1).equals("*")) continue;
			MessageBox verify = new MessageBox(getShell(),SWT.YES | SWT.NO | SWT.ICON_WARNING);
			verify.setMessage(closing.getText().substring(1)+" has unsaved changes, are you sure you want to close this donor?");
			verify.setText("LibreFundraiser Warning");
			if (verify.open() != SWT.YES) {
				return false;
			} else {
				closing.dispose();
			}
		}
		return true;
	}

	public void writeCSV(File f) {
		ArrayList<String[]> l = new ArrayList<String[]>();
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

	public void writeODS(final File f) {
		new Thread(new Runnable(){
			@Override
			public void run() {
				ArrayDeque<String[]> l = new ArrayDeque<String[]>();
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
					OOUtils.open(f);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		
	}
}
