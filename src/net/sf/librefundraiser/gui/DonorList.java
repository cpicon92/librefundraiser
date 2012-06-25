package net.sf.librefundraiser.gui;
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


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
		tableViewer = new TableViewer(compositeTable, SWT.FULL_SELECTION);
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
		packColumns();
	}

	public void refresh() {
		tableViewer.setInput(donors);
		tableViewer.refresh();
		packColumns();
	}
	public void packColumns() {
		for (TableColumn tc : table.getColumns()) {
	        tc.pack();
		}
	}
	
	private int columnSearch(String columnName) {
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
		System.out.println(id);
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
}
