package net.sf.librefundraiser.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.db.CustomField;

public class DatabasePropertiesDialog extends Dialog {

	protected Shell shell;
	private Text txtDatabaseName;
	private Text txtFileName;
	private boolean changeEffected = false;
	private Button btnApply;
	private ArrayList<CustomField> customFields = new ArrayList<CustomField>(Arrays.asList(Main.getDonorDB().getCustomFields()));
	private Table tblFields;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DatabasePropertiesDialog(Shell parent, int style) {
		super(parent, style);
		String filename = null;
		try {
			filename = new File(Main.getDonorDB().getDbPath()).getName();
		} catch (Exception e) {}
		if (filename != null) filename = filename + " ";
		setText(filename+"Properties");
	}

	/**
	 * Open the dialog.
	 */
	public void open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setSize(391, 458);
		shell.setText(getText());
		shell.setLayout(new GridLayout(1, false));
		
		Group grpGeneral = new Group(shell, SWT.NONE);
		grpGeneral.setLayout(new GridLayout(1, false));
		grpGeneral.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpGeneral.setText("General");
		
		Label lblFileName = new Label(grpGeneral, SWT.NONE);
		lblFileName.setBounds(0, 0, 49, 13);
		lblFileName.setText("File Name");
		
		txtFileName = new Text(grpGeneral, SWT.BORDER | SWT.READ_ONLY);
		txtFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtFileName.setBounds(0, 0, 76, 19);
		txtFileName.setText(Main.getDonorDB().getDbPath());
		
		Label lblDatabaseName = new Label(grpGeneral, SWT.NONE);
		lblDatabaseName.setBounds(0, 0, 49, 13);
		lblDatabaseName.setText("Database Name");
		
		txtDatabaseName = new Text(grpGeneral, SWT.BORDER);
		txtDatabaseName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				changeEffected();
			}
		});
		txtDatabaseName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDatabaseName.setBounds(0, 0, 76, 19);
		txtDatabaseName.setText(Main.getDonorDB().getDbName());
		
		Group grpCustomFields = new Group(shell, SWT.NONE);
		grpCustomFields.setLayout(new GridLayout(1, false));
		grpCustomFields.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpCustomFields.setText("Custom Fields");
		
		Label lblCustomFieldExplanation = new Label(grpCustomFields, SWT.WRAP);
		lblCustomFieldExplanation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblCustomFieldExplanation.setText("LibreFundraiser allows an unlimited number of custom fields for your donors. When you add a field here, it will show up on the \"Custom\" tab of the donor entry. ");
		
		tblFields = new Table(grpCustomFields, SWT.BORDER);
		tblFields.setHeaderVisible(true);
		tblFields.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableColumn tblclmnName = new TableColumn(tblFields, SWT.NONE);
		tblclmnName.setWidth(100);
		tblclmnName.setText("Name");
		
		TableColumn tblclmnType = new TableColumn(tblFields, SWT.NONE);
		tblclmnType.setWidth(100);
		tblclmnType.setText("Type");
		
		refreshFieldTable();
		
		Composite compositeCustomFieldButtons = new Composite(grpCustomFields, SWT.NONE);
		compositeCustomFieldButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_compositeCustomFieldButtons = new GridLayout(3, false);
		gl_compositeCustomFieldButtons.marginWidth = 0;
		gl_compositeCustomFieldButtons.marginHeight = 0;
		compositeCustomFieldButtons.setLayout(gl_compositeCustomFieldButtons);
		
		Button btnDeleteField = new Button(compositeCustomFieldButtons, SWT.NONE);
		btnDeleteField.setSize(126, 27);
		btnDeleteField.setText("Delete selected");
		btnDeleteField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tblFields.getSelectionIndex() < 0) return;
				customFields.remove(tblFields.getSelectionIndex());
				refreshFieldTable();
				changeEffected();
			}
		});
		
		Button btnAddAField = new Button(compositeCustomFieldButtons, SWT.NONE);
		btnAddAField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddAField.setText("Add new");	
		btnAddAField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CustomField newField = new CustomFieldEditDialog(shell, SWT.DIALOG_TRIM).open();
				if (newField != null) {
					customFields.add(newField);
					refreshFieldTable();
					changeEffected();
				}
			}
		});
		
		Button btnEdit = new Button(compositeCustomFieldButtons, SWT.NONE);
		btnEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnEdit.setText("Edit");
		btnEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int sel = tblFields.getSelectionIndex();
				if (sel < 0) return;
				CustomField editing = customFields.get(sel);
				CustomField edited = new CustomFieldEditDialog(shell, SWT.DIALOG_TRIM, editing).open();
				if (edited != null) {
					customFields.set(sel, edited);
					refreshFieldTable();
					changeEffected();
				}
			}
		});
		
		Composite compositeButtons = new Composite(shell, SWT.NONE);
		compositeButtons.setLayout(new RowLayout(SWT.HORIZONTAL));
		compositeButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		Button btnOk = new Button(compositeButtons, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (changeEffected) saveChanges();
				shell.close();
			}
		});
		btnOk.setText("OK");
		
		Button btnCancel = new Button(compositeButtons, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		btnCancel.setText("Cancel");
		
		btnApply = new Button(compositeButtons, SWT.NONE);
		btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveChanges();
			}
		});
		btnApply.setText("Apply");
		btnApply.setEnabled(false);
	}

	private void changeEffected() {
		if (btnApply != null) {
			btnApply.setEnabled(true);
			changeEffected = true;
		}
	}
	
	private void saveChanges() {
		Main.getDonorDB().setCustomFields(customFields);
		Main.getDonorDB().setDbName(txtDatabaseName.getText());
		Main.getWindow().refreshTitle();
		btnApply.setEnabled(false);
		changeEffected = false;
	}
	
	private void refreshFieldTable() {
		tblFields.removeAll();
		for (CustomField item : customFields) {
			TableItem tableItem = new TableItem(tblFields, SWT.NONE);
			tableItem.setText(new String[] {item.getName(), item.getType().getName()});
		}
	}
}
