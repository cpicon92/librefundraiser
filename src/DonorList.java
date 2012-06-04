import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;


public class DonorList extends Composite {
	private Table table;
	
	final static public String[][] columns = {{"Account","account"},{"Type","type"},{"Last Name/Business","lastname"},{"First Name","firstname"},{"Spouse/Contact Last","spouselast"},{"Spouse/Contact First","spousefrst"},{"Salutation","salutation"},{"Home Phone","homephone"},{"Work Phone","workphone"},{"Fax","fax"},{"Category","category1"},{"Donor Source","category2"},{"Mail Name","mailname"},{"Address 1","address1"},{"Address 2","address2"},{"City","city"},{"State","state"},{"Zip","zip"},{"Country","country"},{"Email","email"},{"Last Change","changedate"},{"Last Gift Date","lastgivedt"},{"Last Gift","lastamt"},{"Total Gifts","alltime"},{"Year-to-date","yeartodt"},{"First Gift","firstgift"},{"Largest Gift","largest"}};
	static public Donor[] donors = null;

	private TableViewer tableViewer;
	
	private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			Donor donor = (Donor) element;
			if (columns[columnIndex][1].matches("(yeartodt|lastamt|largest|alltime)")) {
				return LibreFundraiser.toMoney(donor.getData(columns[columnIndex][1]));
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
		donors = LibreFundraiser.getLocalDB().getDonors();
		this.setLayout(new FillLayout(SWT.HORIZONTAL));
		final CTabFolder tabFolder = new CTabFolder(this, SWT.BORDER);
		tabFolder.setSelectionBackground(new Color[]{SWTResourceManager.getColor(SWT.COLOR_WHITE), SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND)}, new int[]{100}, true);

		
		CTabItem tbtmDonors = new CTabItem(tabFolder, SWT.NONE);
		tbtmDonors.setText("Donors");
		
		tableViewer = new TableViewer(tabFolder, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				TableItem selectedItem = table.getSelection()[0];
				int id = Integer.parseInt(selectedItem.getText(columnSearch("account")));
				new DonorTab(id,tabFolder);
			}
		});
		table.setHeaderVisible(true);
		tbtmDonors.setControl(table);
		
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
		new TableColumnSorter(tableViewer);
		for (TableColumn tc : table.getColumns()) {
	        tc.pack();
		}
	}

	public void refresh() {
		donors = LibreFundraiser.getLocalDB().getDonors();
		tableViewer.refresh();
	}
	
	private int columnSearch(String columnName) {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i][1].equals(columnName)) return i;
		}
		return -1;
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
