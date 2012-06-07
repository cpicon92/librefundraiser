package net.sf.librefundraiser.gui;
import net.sf.librefundraiser.LibreFundraiser;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class TableColumnSorter extends ViewerComparator {
	public static final int ASC = 1;
	public static final int NONE = 0;
	public static final int DESC = -1;

	private int direction = 0;
	private TableColumn column = null;
	private int columnIndex = 0;
	final private TableViewer viewer;

	final private SelectionListener selectionHandler = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			TableColumnSorter sorter = (TableColumnSorter) TableColumnSorter.this.viewer.getComparator();
			Assert.isTrue(TableColumnSorter.this == sorter);
			TableColumn selectedColumn = (TableColumn) e.widget;
			Assert.isTrue(TableColumnSorter.this.viewer.getTable() == selectedColumn.getParent());
			TableColumnSorter.this.setColumn(selectedColumn);
		}
	};

	public TableColumnSorter(TableViewer viewer) {
		this.viewer = viewer;
		Assert.isTrue(this.viewer.getComparator() == null);
		viewer.setComparator(this);

		for (TableColumn tableColumn : viewer.getTable().getColumns()) {
			tableColumn.addSelectionListener(selectionHandler);
		}
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

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return direction * doCompare(viewer, e1, e2);
	}

	protected int doCompare(Viewer v, Object e1, Object e2) {
		Assert.isTrue(v == this.viewer);
		ITableLabelProvider labelProvider = (ITableLabelProvider) viewer.getLabelProvider();
		String column = DonorList.columns[columnIndex][0];
		String t1 = labelProvider.getColumnText(e1,columnIndex);
		String t2 = labelProvider.getColumnText(e2,columnIndex);
		if (column.toLowerCase().equals("address2")) {
			String addr1 = t1.replaceAll("^([0-9]*)(.*)","$2");
			String addr2 = t2.replaceAll("^([0-9]*)(.*)","$2");
			int num1 = 0;
			int num2 = 0;
			try {
				num1 = Integer.parseInt(t1.replaceAll("^([0-9]*)(.*)","$1"));
				num2 = Integer.parseInt(t2.replaceAll("^([0-9]*)(.*)","$1"));
			} catch (Exception e) {}
			t1 = addr1 + String.format(" %05d", num1);
			t2 = addr2 + String.format(" %05d", num2);
		}
		if (DonorList.columns[columnIndex][1].matches("(yeartodt|lastamt|largest|alltime)")) {
			Double d1 = LibreFundraiser.fromMoney(t1);
			Double d2 = LibreFundraiser.fromMoney(t2);
			return d1.compareTo(d2);
		}
		if (t1 == null) t1 = "";
		if (t2 == null) t2 = "";
		int result = t1.compareTo(t2);
		return result;
	}
}
