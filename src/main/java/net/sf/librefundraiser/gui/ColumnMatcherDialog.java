package net.sf.librefundraiser.gui;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ColumnMatcherDialog extends Dialog {

	protected Map<String, Integer> result;
	protected Shell shell;
	private Table tblMap;
	private Table tblAvailable;
	private TableColumn tblclmnAvailable;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ColumnMatcherDialog(Shell parent, int style) {
		super(parent, style);
		setText("Match Columns");
	}

	/**
	 * Open the dialog.
	 * @return a map of donor field name to column index
	 */
	public Map<String, Integer> open(List<String> importColumns) {
		createContents(importColumns);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 * @param importColumns 
	 */
	private void createContents(List<String> importColumns) {
		shell = new Shell(getParent(), getStyle() | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setSize(500, 300);
		shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));
		
		tblMap = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		tblMap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tblMap.setHeaderVisible(true);
		tblMap.setLinesVisible(true);
		
		TableColumn tblclmnImportColumn = new TableColumn(tblMap, SWT.NONE);
		tblclmnImportColumn.setWidth(150);
		tblclmnImportColumn.setText("Import Column");
		
		
		TableColumn tblclmnDonorField = new TableColumn(tblMap, SWT.NONE);
		tblclmnDonorField.setWidth(100);
		tblclmnDonorField.setText("Donor Field");
		
		tblAvailable = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		tblAvailable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tblAvailable.setHeaderVisible(true);
		tblAvailable.setLinesVisible(true);
		
		tblclmnAvailable = new TableColumn(tblAvailable, SWT.NONE);
		tblclmnAvailable.setWidth(100);
		tblclmnAvailable.setText("Available");

	}

}
