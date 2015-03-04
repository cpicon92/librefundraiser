package net.sf.librefundraiser.gui;
import net.sf.librefundraiser.gui.DonorTable.DonorLabelProvider;
import net.sf.librefundraiser.io.Donor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class DonorListSorter extends ViewerComparator {
	public static final int ASC = 1;
	public static final int NONE = 0;
	public static final int DESC = -1;

	private int direction = 0;
	private TableColumn column = null;
	private int columnIndex = 0;
	final private TableViewer viewer;

	final private SelectionListener selectionHandler = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			DonorListSorter sorter = (DonorListSorter) DonorListSorter.this.viewer.getComparator();
			Assert.isTrue(DonorListSorter.this == sorter);
			TableColumn selectedColumn = (TableColumn) e.widget;
			Assert.isTrue(DonorListSorter.this.viewer.getTable() == selectedColumn.getParent());
			DonorListSorter.this.setColumn(selectedColumn);
		}
	};

	public DonorListSorter(TableViewer viewer) {
		this.viewer = viewer;
		Assert.isTrue(this.viewer.getComparator() == null);
		viewer.setComparator(this);

		for (TableColumn tableColumn : viewer.getTable().getColumns()) {
			tableColumn.addSelectionListener(selectionHandler);
		}
		try {
			TableColumn selectedColumn = viewer.getTable().getColumns()[0];
			Assert.isTrue(DonorListSorter.this.viewer.getTable() == selectedColumn.getParent());
			DonorListSorter.this.setColumn(selectedColumn);
		} catch (Exception e) {}
	}

	public void setColumn(TableColumn selectedColumn) {
		if (column == selectedColumn) {
			switch (direction) {
			case ASC:
				direction = DESC;
				break;
			case DESC:
				direction = ASC;
				break;
			default:
				direction = ASC;
				break;
			}
		} else {
			this.column = selectedColumn;
			this.direction = ASC;
		}

		Table table = viewer.getTable();
		switch (direction) {
		case ASC:
			table.setSortColumn(selectedColumn);
			table.setSortDirection(SWT.UP);
			break;
		case DESC:
			table.setSortColumn(selectedColumn);
			table.setSortDirection(SWT.DOWN);
			break;
		default:
			table.setSortColumn(null);
			table.setSortDirection(SWT.NONE);
			break;
		}

		TableColumn[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++) {
			TableColumn theColumn = columns[i];
			if (theColumn == this.column){
				columnIndex = i;
				break;
			}
		}
		viewer.refresh();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compare(Viewer v, Object c1, Object c2) {
		Donor d1 = (Donor) c1, d2 = (Donor) c2;
		Assert.isTrue(v == this.viewer);
		DonorLabelProvider labelProvider = (DonorLabelProvider) viewer.getLabelProvider();
		String column = DonorTable.columns[columnIndex][0];
		Object t1 = labelProvider.getColumnData(d1, columnIndex),
		t2 = labelProvider.getColumnData(d2, columnIndex);
		int output = 0;
		//compare address with street name first
		if (column.toLowerCase().equals("address2")) {
			//TODO add Address type and use real comparator
			String addr1 = String.valueOf(t1).replaceAll("^([0-9]*)(.*)","$2");
			String addr2 = String.valueOf(t2).replaceAll("^([0-9]*)(.*)","$2");
			int num1 = 0;
			int num2 = 0;
			try {
				num1 = Integer.parseInt(String.valueOf(t1).replaceAll("^([0-9]*)(.*)","$1"));
				num2 = Integer.parseInt(String.valueOf(t2).replaceAll("^([0-9]*)(.*)","$1"));
			} catch (Exception e) {}
			t1 = addr1 + String.format(" %05d", num1);
			t2 = addr2 + String.format(" %05d", num2);
		}
		if (t1 instanceof Comparable && t2 instanceof Comparable) {
			output = ((Comparable) t1).compareTo((Comparable) t2);
		}
		return direction * output;
	}
}
