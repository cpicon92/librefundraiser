package net.sf.librefundraiser.gui;

import java.util.Date;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ResourceManager;
import net.sf.librefundraiser.io.Gift;
import net.sf.librefundraiser.io.Money;

import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GiftEditForm extends Composite {
	private Combo txtAmount;
	private DatePicker dtDateGiven;
	private Text txtNote;
	private final Gift gift;
	public boolean canceled = true;
	private Button chkLetter;
	private Combo comboSource;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public GiftEditForm(Composite parent, int style, Gift gift) {
		super(parent, SWT.BORDER);
		if (System.getProperty("os.name").indexOf("nux") >= 0) {
			setBackground(ResourceManager.getColor(0xd3d3d3));
			setBackgroundMode(SWT.INHERIT_FORCE);
		}
		this.gift = gift;
		setLayout(new GridLayout(2, false));

		Label lblAmount = new Label(this, SWT.NONE);
		lblAmount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAmount.setText("Amount");

		Composite compositeTop = new Composite(this, SWT.NONE);
		compositeTop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_compositeTop = new GridLayout(5, false);
		gl_compositeTop.marginWidth = 0;
		gl_compositeTop.marginHeight = 0;
		compositeTop.setLayout(gl_compositeTop);

		txtAmount = new Combo(compositeTop, SWT.BORDER);
		txtAmount.setItems(new String[] {"15", "25", "40", "50", "100", "150", "200", "250", "500"});
		GridData gd_txtAmount = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtAmount.widthHint = 120;
		txtAmount.setLayoutData(gd_txtAmount);

		Label lblDateGiven = new Label(compositeTop, SWT.NONE);
		lblDateGiven.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDateGiven.setText("Date Given");

		dtDateGiven = new DatePicker(Main.getDateFormat(), compositeTop, SWT.NONE);
		GridData gd_dtDateGiven = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_dtDateGiven.widthHint = 160;
		dtDateGiven.setLayoutData(gd_dtDateGiven);
		dtDateGiven.setDate(new Date());

		Label lblSource = new Label(compositeTop, SWT.NONE);
		lblSource.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSource.setText("Source");

		comboSource = new Combo(compositeTop, SWT.NONE);
		comboSource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		String[] previousSources = Main.getDonorDB().getPreviousGiftValues("source").toArray(new String[0]);
		comboSource.setItems(previousSources);
		new AutoCompleteField(comboSource, new ComboContentAdapter(), previousSources);

		Label lblNote = new Label(this, SWT.NONE);
		lblNote.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNote.setText("Note");

		Composite compositeBottom = new Composite(this, SWT.None);
		compositeBottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_compositeBottom = new GridLayout(4, false);
		gl_compositeBottom.horizontalSpacing = 4;
		gl_compositeBottom.marginHeight = 0;
		gl_compositeBottom.marginWidth = 0;
		compositeBottom.setLayout(gl_compositeBottom);

		txtNote = new Text(compositeBottom, SWT.BORDER);
		txtNote.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		chkLetter = new Button(compositeBottom, SWT.CHECK);
		chkLetter.setText("Send a thank you letter or receipt");
		GridData gd_chkLetter = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_chkLetter.verticalIndent = 1;
		chkLetter.setLayoutData(gd_chkLetter);

		Button btnCancel = new Button(compositeBottom, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dispose();
			}
		});
		btnCancel.setText("Cancel");

		Button btnSave = new Button(compositeBottom, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				canceled = false;
				saveForm();
				dispose();
			}
		});
		btnSave.setSize(36, 23);
		btnSave.setText("Confirm");

		fillForm();
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	private void fillForm() {
		txtAmount.setText(String.valueOf(gift.getAmount()));
		dtDateGiven.setDate(gift.getDategiven());
		chkLetter.setSelection(gift.isLetter());
		comboSource.setText(gift.getSource());
		txtNote.setText(gift.getNote());
	}
	
	private void saveForm() {
		gift.setAmount(new Money(txtAmount.getText()));
		gift.setDategiven(dtDateGiven.getDate());
		gift.setLetter(chkLetter.getSelection());
		gift.setSource(comboSource.getText());
		gift.setNote(txtNote.getText());
		gift.setDt_entry(new Date());
	}

	public Gift getGift() {
		return gift;
	}
	public void initializePointer() {
		txtAmount.setFocus();
	}
}