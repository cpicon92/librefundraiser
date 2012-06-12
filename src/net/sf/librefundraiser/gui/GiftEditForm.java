package net.sf.librefundraiser.gui;

import net.sf.librefundraiser.Donor.Gift;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class GiftEditForm extends Composite {
	private Text txtAmount;
	private DateTime dtDateGiven;
	private Text txtNote;
	private final Gift gift;
	private final Object[][] fields;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public GiftEditForm(Composite parent, int style, Gift gift) {
		super(parent, SWT.BORDER);
		setBackgroundMode(SWT.INHERIT_DEFAULT);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		this.gift = gift;
		setLayout(new GridLayout(2, false));

		Label lblAmount = new Label(this, SWT.NONE);
		lblAmount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAmount.setSize(37, 13);
		lblAmount.setText("Amount");

		Composite compositeTop = new Composite(this, SWT.NONE);
		compositeTop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_compositeTop = new GridLayout(5, false);
		gl_compositeTop.marginWidth = 0;
		gl_compositeTop.marginHeight = 0;
		compositeTop.setLayout(gl_compositeTop);

		txtAmount = new Text(compositeTop, SWT.BORDER);
		txtAmount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtAmount.setSize(58, 19);

		Label lblDateGiven = new Label(compositeTop, SWT.NONE);
		lblDateGiven.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDateGiven.setSize(53, 13);
		lblDateGiven.setText("Date Given");

		dtDateGiven = new DateTime(compositeTop, SWT.BORDER | SWT.DROP_DOWN);
		dtDateGiven.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		dtDateGiven.setSize(58, 19);

		Label lblSource = new Label(compositeTop, SWT.NONE);
		lblSource.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSource.setSize(33, 13);
		lblSource.setText("Source");

		Combo comboSource = new Combo(compositeTop, SWT.NONE);
		comboSource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboSource.setSize(73, 21);

		Label lblNote = new Label(this, SWT.NONE);
		lblNote.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNote.setText("Note");

		Composite compositeBottom = new Composite(this, SWT.None);
		compositeBottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_compositeBottom = new GridLayout(5, false);
		gl_compositeBottom.marginHeight = 0;
		gl_compositeBottom.marginWidth = 0;
		compositeBottom.setLayout(gl_compositeBottom);

		txtNote = new Text(compositeBottom, SWT.BORDER);
		txtNote.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Button chkLetter = new Button(compositeBottom, SWT.CHECK);
		GridData gd_chkLetter = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_chkLetter.verticalIndent = 1;
		chkLetter.setLayoutData(gd_chkLetter);
		chkLetter.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		
		Label lblLetter = new Label(compositeBottom, SWT.NONE);
		lblLetter.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				chkLetter.setSelection(!chkLetter.getSelection());
			}
		});
		lblLetter.setText("Send a thank you letter or receipt");

		Button btnCancel = new Button(compositeBottom, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dispose();
			}
		});
		btnCancel.setText("Cancel");

		Button btnSave = new Button(compositeBottom, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dispose();
			}
		});
		btnSave.setSize(36, 23);
		btnSave.setText("Save");

		Object[][] fields = { { txtAmount, "amount" },
				{ dtDateGiven, "dategiven" }, { chkLetter, "letter" },
				{ comboSource, "source" }, { txtNote, "note" } };
		this.fields = fields;
		
		setForeground(SWTResourceManager.getColor(SWT.COLOR_INFO_FOREGROUND));
		inheritForeground(this);
		fillForm();
	}

	private void inheritForeground(Control control) {
		System.out.println(control.getClass().getName());
		try {
			for (Control c : ((Composite)control).getChildren()) {
				c.setForeground(control.getForeground());
				inheritForeground(c);
			}
		} catch (Exception e) {}
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	private void fillForm() {
		for (Object field[] : fields) {
			fillField((Control)field[0],(String)field[1]);
		}
	}
	
	private void fillField(Control field, String key) {
		if (field.getClass().getName().equals("org.eclipse.swt.widgets.Text")) {
			((Text)field).setText(gift.getIc(key)!=null?gift.getIc(key):"");
		} else if (field.getClass().getName().equals("org.eclipse.swt.widgets.Combo")) {
			((Combo)field).setText(gift.getIc(key)!=null?gift.getIc(key):"");
		} else {
			System.err.println("The field for \""+key+"\" cannot contain text.");
		}
	}
}
