package net.sf.librefundraiser.gui;

import java.text.ParseException;
import java.util.Date;

import net.sf.librefundraiser.io.Gift;
import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ResourceManager;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GiftEditForm extends Composite {
	private Combo txtAmount;
	private DatePicker dtDateGiven;
	private Text txtNote;
	private final Gift gift;
	private final Object[][] fields;
	public boolean canceled = true;

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
		txtAmount.setItems(new String[] {"15", "25", "50", "100", "150", "200", "250", "500"});
		GridData gd_txtAmount = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtAmount.widthHint = 30;
		txtAmount.setLayoutData(gd_txtAmount);

		Label lblDateGiven = new Label(compositeTop, SWT.NONE);
		lblDateGiven.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDateGiven.setText("Date Given");

		dtDateGiven = new DatePicker(Main.getDateFormat(), compositeTop, SWT.NONE);
		dtDateGiven.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		dtDateGiven.setDate(new Date());

		Label lblSource = new Label(compositeTop, SWT.NONE);
		lblSource.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSource.setText("Source");

		Combo comboSource = new Combo(compositeTop, SWT.NONE);
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

		final Button chkLetter = new Button(compositeBottom, SWT.CHECK);
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
		btnSave.setText("Add");

		Object[][] fields = { { txtAmount, "amount" },
				{ dtDateGiven, "dategiven" }, { chkLetter, "letter" },
				{ comboSource, "source" }, { txtNote, "note" } };
		this.fields = fields;
		fillForm();
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
	
	private void saveForm() {
		for (Object field[] : fields) {
			saveField((Control)field[0],(String)field[1]);
		}
		gift.putIc("dt_entry",Main.getDateFormat().format(new Date()));
		gift.putIc("recnum", String.format("%d",gift.recnum));
	}
	
	private void fillField(Control field, String key) {
		String value = gift.getIc(key)!=null?gift.getIc(key):"";
		if (field.getClass().equals(Text.class)) {
			((Text)field).setText(value);
		} else if (field.getClass().equals(Combo.class)) {
			((Combo)field).setText(value);
		} else if (field.getClass().equals(DatePicker.class)) {
			try {
				((DatePicker)field).setDate(Main.getDateFormat().parse(value));
			} catch (ParseException e) {}
		} else if (field.getClass().equals(Button.class)) {
			Button b = ((Button)field);
			if (SWT.CHECK == (b.getStyle() & SWT.CHECK)) {
				b.setSelection(value.toLowerCase().equals("true"));
				System.out.println(value);
			}
		} else {
			System.err.println("The field for \""+key+"\" cannot contain text.");
		}
	}
	
	private void saveField(Control field, String key) {
		if (field.getClass().equals(Text.class)) {
			String value = ((Text)field).getText();
			if (field.equals(txtAmount)) {
				value = value.replace(",", "");
			}
			gift.putIc(key, value);
		} else if (field.getClass().equals(Combo.class)) {
			gift.putIc(key,((Combo)field).getText());
		} else if (field.getClass().equals(DatePicker.class)) {
			Date date = ((DatePicker)field).getDate();
			gift.putIc(key, Main.getDateFormat().format(date));
		} else if (field.getClass().equals(Button.class)) {
			Button b = ((Button)field);
			if (SWT.CHECK == (b.getStyle() & SWT.CHECK)) {
				gift.putIc(key, ""+b.getSelection());
			}
		} else {
			System.err.println("The field for \""+key+"\" cannot contain text.");
		}
	}

	public Gift getGift() {
		return gift;
	}
	public void initializePointer() {
		txtAmount.setFocus();
	}
}
