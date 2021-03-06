package net.sf.librefundraiser.gui;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import net.sf.librefundraiser.db.CustomField;

public class CustomFieldEditDialog extends Dialog {

	protected CustomField editing, result;
	protected Shell shell;
	private Combo comboFieldType;
	private Text txtFieldName;
	private boolean changeEffected = false;
	private Button btnApply;
	private Composite cmpChoices;
	private Table tblChoices;
	private Button btnOk;

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
//		shell.setSize(390, editing.getType() == CustomField.Type.CHOICE ? 458 : 220);
		shell.setText(getText());
		shell.setLayout(new GridLayout(1, false));
		
		Label lblFieldName = new Label(shell, SWT.NONE);

		lblFieldName.setText("Field Name");
		
		//TODO warn if name exists
		txtFieldName = new Text(shell, SWT.BORDER);
		GridData gd_txtFieldName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtFieldName.widthHint = 390;
		txtFieldName.setLayoutData(gd_txtFieldName);

		txtFieldName.setText(editing.getName());
		txtFieldName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				changed();
				editing.setName(txtFieldName.getText());
			}
		});
		
		Label lblFieldType = new Label(shell, SWT.NONE);

		lblFieldType.setText("Type");
		
		comboFieldType = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboFieldType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		comboFieldType.add("");
		for (CustomField.Type t : CustomField.Type.values()) {
			comboFieldType.add(t.getName());
			if (t == editing.getType()) {
				if (comboFieldType.getSelectionIndex() != 0 && comboFieldType.getItem(0).isEmpty()) comboFieldType.remove(0);
				comboFieldType.select(comboFieldType.getItemCount() - 1);
			}
		}
		comboFieldType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (comboFieldType.getSelectionIndex() != 0 && comboFieldType.getItem(0).isEmpty()) comboFieldType.remove(0);
				try {
					editing.setType(CustomField.Type.valueOf(comboFieldType.getText().toUpperCase()));
				} catch (IllegalArgumentException ex) {
				}
				cmpChoices.setVisible(editing.getType() == CustomField.Type.CHOICE);
				((GridData) cmpChoices.getLayoutData()).exclude = editing.getType() != CustomField.Type.CHOICE;
				shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				refreshTable();
				changed();
			}
		});
		
		cmpChoices = new Composite(shell, SWT.NONE);
		GridLayout lyCmpChoices = new GridLayout(1, false);
		lyCmpChoices.marginHeight = 0;
		lyCmpChoices.marginWidth = 0;
		cmpChoices.setLayout(lyCmpChoices );
		GridData gdCmpChoices = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gdCmpChoices.exclude = editing.getType() != CustomField.Type.CHOICE;
		cmpChoices.setLayoutData(gdCmpChoices);
		cmpChoices.setVisible(editing.getType() == CustomField.Type.CHOICE);

		tblChoices = new Table(cmpChoices, SWT.BORDER);
		tblChoices.setHeaderVisible(true);
		GridData gd_tblChoices = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tblChoices.heightHint = 142;
		tblChoices.setLayoutData(gd_tblChoices);
		
		TableColumn tblclmnName = new TableColumn(tblChoices, SWT.NONE);
		tblclmnName.setWidth(300);
		tblclmnName.setText("Choices");
		
		refreshTable();
		
		Composite compositeCustomFieldButtons = new Composite(cmpChoices, SWT.NONE);
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
				if (tblChoices.getSelectionIndex() < 0) return;
				editing.getChoices().remove(tblChoices.getSelectionIndex());
				refreshTable();
				changed();
			}
		});
		
		Button btnAddAField = new Button(compositeCustomFieldButtons, SWT.NONE);
		btnAddAField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddAField.setText("Add new");	
		btnAddAField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog input = new InputDialog(shell, "New choice", "Enter value for new choice", null, null);
				if (input.open() == 0 && !input.getValue().trim().isEmpty()) {
					editing.getChoices().add(input.getValue());
					refreshTable();
					changed();
				}
			}
		});
		
		Button btnEdit = new Button(compositeCustomFieldButtons, SWT.NONE);
		btnEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnEdit.setText("Edit");
		btnEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int sel = tblChoices.getSelectionIndex();
				if (sel < 0) return;
				String prev = editing.getChoices().get(sel);
				InputDialog input = new InputDialog(shell, "Edit choice", "Enter new value for choice", prev, null);
				if (input.open() == 0 && !input.getValue().trim().isEmpty()) {
					editing.getChoices().set(sel, input.getValue());
					refreshTable();
					changed();
				}
			}
		});
		
		Composite compositeButtons = new Composite(shell, SWT.NONE);
		RowLayout rl_compositeButtons = new RowLayout(SWT.HORIZONTAL);
		rl_compositeButtons.pack = false;
		compositeButtons.setLayout(rl_compositeButtons);
		compositeButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		btnOk = new Button(compositeButtons, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (changeEffected) saveChanges();
				shell.close();
			}
		});
		btnOk.setText("OK");
		btnOk.setEnabled(editing.getType() != null);
		
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
		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void refreshTable() {
		tblChoices.removeAll();
		if (editing.getType() == CustomField.Type.CHOICE) for (String choice : editing.getChoices()) {
			TableItem tableItem = new TableItem(tblChoices, SWT.NONE);
			tableItem.setText(new String[] {choice});
		}
	}

	private void changed() {
		//form validation
		boolean valid = true;
		if (editing.getType() == null) valid = false;
		else if (editing.getType() == CustomField.Type.CHOICE && editing.getChoices().isEmpty()) valid = false;
		else if (editing.getName() == null || editing.getName().trim().isEmpty()) valid = false;
		btnApply.setEnabled(valid);
		changeEffected = true;
		btnOk.setEnabled(valid);
	}
	
	private void saveChanges() {
		this.result = this.editing.copy();
		btnApply.setEnabled(false);
		changeEffected = false;
	}
}
