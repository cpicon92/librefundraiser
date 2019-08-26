package net.sf.librefundraiser.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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

public class CustomFieldEditDialog extends Dialog {

	protected CustomField editing, result;
	protected Shell shell;
	private Combo comboFieldType;
	private Text txtFieldName;
	private boolean changeEffected = false;
	private Button btnApply;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CustomFieldEditDialog(Shell parent, int style, CustomField toEdit) {
		super(parent, style);
		setText("Edit Custom Field");
		this.editing = toEdit == null ? new CustomField() : toEdit.copy();
	}
	
	public CustomFieldEditDialog(Shell parent, int style) {
		this(parent, style, null);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public CustomField open() {
		createContents();
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
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setSize(391, 458);
		shell.setText(getText());
		shell.setLayout(new GridLayout(1, false));
		
		Label lblFieldName = new Label(shell, SWT.NONE);
		lblFieldName.setBounds(0, 0, 49, 13);
		lblFieldName.setText("Field Name");
		
		txtFieldName = new Text(shell, SWT.BORDER);
		txtFieldName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtFieldName.setBounds(0, 0, 76, 19);
		txtFieldName.setText(editing.name);
		txtFieldName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				changeEffected();
				editing.name = txtFieldName.getText();
			}
		});
		
		Label lblFieldType = new Label(shell, SWT.NONE);
		lblFieldType.setBounds(0, 0, 49, 13);
		lblFieldType.setText("Type");
		
		comboFieldType = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboFieldType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				changeEffected();
				try {
					editing.type = CustomField.Type.valueOf(comboFieldType.getText().toUpperCase());
				} catch (IllegalArgumentException ex) {
				}
			}
		});
		comboFieldType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboFieldType.setBounds(0, 0, 76, 19);
		comboFieldType.add("");
		int selectedType = 0, i = 1;
		for (CustomField.Type t : CustomField.Type.values()) {
			comboFieldType.add(t.getName());
			if (t == editing.type) selectedType = i;
			i++;
		}
		comboFieldType.setSelection(new Point(selectedType, selectedType));
		
		Group grpChoices = new Group(shell, SWT.NONE);
		grpChoices.setLayout(new GridLayout(1, false));
		grpChoices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpChoices.setText("Choices");

		Table list = new Table(grpChoices, SWT.BORDER);
		list.setHeaderVisible(true);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
//		TableColumn tblclmnName = new TableColumn(list, SWT.NONE);
//		tblclmnName.setWidth(100);
//		tblclmnName.setText("Name");
//		
//		TableColumn tblclmnType = new TableColumn(list, SWT.NONE);
//		tblclmnType.setWidth(100);
//		tblclmnType.setText("Type");
		
		CustomField[] items = Main.getDonorDB().getCustomFields();
		for (CustomField item : items) {
			TableItem tableItem = new TableItem(list, SWT.NONE);
			tableItem.setText(new String[] {item.name, item.type.getName()});
		}
		
		Composite compositeCustomFieldButtons = new Composite(grpChoices, SWT.NONE);
		compositeCustomFieldButtons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_compositeCustomFieldButtons = new GridLayout(3, false);
		gl_compositeCustomFieldButtons.marginWidth = 0;
		gl_compositeCustomFieldButtons.marginHeight = 0;
		compositeCustomFieldButtons.setLayout(gl_compositeCustomFieldButtons);
		
		Button btnDeleteField = new Button(compositeCustomFieldButtons, SWT.NONE);
		btnDeleteField.setSize(126, 27);
		btnDeleteField.setText("Delete selected");
		
		Button btnAddAField = new Button(compositeCustomFieldButtons, SWT.NONE);
		btnAddAField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddAField.setText("Add new");	
		btnAddAField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
			}
		});
		
		Button btnEdit = new Button(compositeCustomFieldButtons, SWT.NONE);
		btnEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnEdit.setText("Edit");
		
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

	public void changeEffected() {
		if (btnApply != null) {
			btnApply.setEnabled(true);
			changeEffected = true;
		}
	}
	
	public void saveChanges() {
		this.result = this.editing.copy();
		btnApply.setEnabled(false);
		changeEffected = false;
	}
}
