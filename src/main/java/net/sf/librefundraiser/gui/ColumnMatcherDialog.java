package net.sf.librefundraiser.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.db.CustomField;
import net.sf.librefundraiser.db.FileLFD;

public class ColumnMatcherDialog extends Dialog {

	protected Map<String, Integer> result;
	protected Shell shell;
	private Table tblMap;
	private Table tblAvailable;
	private TableColumn tblclmnAvailable;
	private Composite cmpButtons;
	private Button btnImport;
	private Button btnCancel;

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
		
		for (String itm : importColumns) {
			TableItem tableItem = new TableItem(tblMap, SWT.NONE);
			tableItem.setText(new String[]{itm, null});
		}
		//drop target for map
		DropTarget tgtMap = new DropTarget(tblMap, DND.DROP_MOVE);
		tgtMap.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		tgtMap.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (event.item == null) return;
				TableItem item = (TableItem) event.item;
				item.setText(1, (String) event.data); 
			}
			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT;
				drag(event);
			}
			@Override
			public void dragEnter(DropTargetEvent event) {
				drag(event);
			}
			@Override
			public void dragLeave(DropTargetEvent event) {
				event.detail = DND.DROP_DEFAULT;
			}
			private void drag(DropTargetEvent event) {
				if (event.item == null) return;
				TableItem item = (TableItem) event.item;
				event.detail = !item.getText(1).isEmpty() ? DND.DROP_NONE : DND.DROP_MOVE;
			}
		});
		//drag source for map
		DragSource srcMap = new DragSource(tblMap, DND.DROP_MOVE);
		srcMap.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		srcMap.addDragListener(new DragSourceListener() {
			TableItem item;
			@Override
			public void dragStart(DragSourceEvent event) {
				item = (TableItem) tblMap.getItem(tblMap.getSelectionIndex());
				if (item.getText(1).isEmpty()) event.doit = false;
			}
			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = item.getText(1);
			}
			@Override
			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE) item.setText(1, "");
			}
		});
		
		tblAvailable = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		tblAvailable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tblAvailable.setHeaderVisible(true);
		tblAvailable.setLinesVisible(true);
		
		tblclmnAvailable = new TableColumn(tblAvailable, SWT.NONE);
		tblclmnAvailable.setWidth(100);
		tblclmnAvailable.setText("Available");
		
		for (String itm : FileLFD.donorFields) {
			TableItem tableItem = new TableItem(tblAvailable, SWT.NONE);
			tableItem.setText(new String[]{itm});
		}
		for (CustomField itm : Main.getDonorDB().getCustomFields()) {
			if (itm.getType() != CustomField.Type.TEXT && itm.getType() != CustomField.Type.CHOICE) continue;
			TableItem tableItem = new TableItem(tblAvailable, SWT.NONE);
			tableItem.setText(new String[]{itm.getName()});
		}
		//drag source for available fields
		DragSource srcAvailable = new DragSource(tblAvailable, DND.DROP_MOVE);
		srcAvailable.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		srcAvailable.addDragListener(new DragSourceListener() {
			int sel;
			@Override
			public void dragStart(DragSourceEvent event) {
				sel = tblAvailable.getSelectionIndex();
				if (sel < 0) event.doit = false;
			}
			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = tblAvailable.getItem(sel).getText();
			}
			@Override
			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE) tblAvailable.remove(sel);
			}
		});
		//drop target for available fields
		DropTarget tgtAvailable = new DropTarget(tblAvailable, DND.DROP_MOVE);
		tgtAvailable.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		tgtAvailable.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				int index = tblAvailable.indexOf((TableItem) event.item);
				TableItem item = new TableItem(tblAvailable, SWT.NONE, index);
				item.setText(0, (String) event.data);
			}
			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_INSERT_BEFORE;
			}
		});
		
		cmpButtons = new Composite(shell, SWT.NONE);
		cmpButtons.setLayout(new RowLayout(SWT.HORIZONTAL));
		cmpButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		
		btnImport = new Button(cmpButtons, SWT.NONE);
		btnImport.setText("Import");
		btnImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = new HashMap<>();
				TableItem[] items = tblMap.getItems();
				for (int i = 0; i < items.length; i++) {
					String field = items[i].getText(1);
					if (!field.isEmpty()) result.put(field, i);
				}
				shell.close();
			}
		});
		
		btnCancel = new Button(cmpButtons, SWT.NONE);
		btnCancel.setText("Cancel");
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});

	}

}
