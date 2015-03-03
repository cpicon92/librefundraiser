package net.sf.librefundraiser.gui;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.io.Gift;
import net.sf.librefundraiser.Main;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


public class GiftTable extends Composite {
	private Table tableGifts;
	private Donor donor;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	//TODO:Add sorting functionality
	public GiftTable(Composite parent, int style, Donor donor) {
		super(parent, style);
		this.donor = donor;
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		tableGifts = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableGifts.setHeaderVisible(true);
		
		TableColumn tblclmnAmount = new TableColumn(tableGifts, SWT.NONE);
		tblclmnAmount.setWidth(100);
		tblclmnAmount.setText("Amount");
		
		TableColumn tblclmnDateGiven = new TableColumn(tableGifts, SWT.NONE);
		tblclmnDateGiven.setWidth(100);
		tblclmnDateGiven.setText("Date Given");
		
		TableColumn tblclmnLetter = new TableColumn(tableGifts, SWT.NONE);
		tblclmnLetter.setWidth(100);
		tblclmnLetter.setText("Letter");
		
		TableColumn tblclmnEntryDate = new TableColumn(tableGifts, SWT.NONE);
		tblclmnEntryDate.setWidth(100);
		tblclmnEntryDate.setText("Entry Date");
		
		TableColumn tblclmnGiftSource = new TableColumn(tableGifts, SWT.NONE);
		tblclmnGiftSource.setWidth(100);
		tblclmnGiftSource.setText("Gift Source");
		
		TableColumn tblclmnNote = new TableColumn(tableGifts, SWT.NONE);
		tblclmnNote.setWidth(100);
		tblclmnNote.setText("Note");
		
		TableColumn tblclmnRecordNumber = new TableColumn(tableGifts, SWT.NONE);
		tblclmnRecordNumber.setWidth(100);
		tblclmnRecordNumber.setText("Record Number");
		
		fillTable();
	}
	
	protected void fillTable() {
		List<Gift> gifts = new ArrayList<>(donor.getGifts().values());
		Collections.sort(gifts);
		for (Gift gift : gifts) {
			TableItem tableItem = new TableItem(tableGifts, SWT.NONE);
			String[] itemData = new String[] { Main.toMoney(gift.getIc("amount")),
					gift.getIc("dategiven"), gift.getIc("letter"),
					gift.getIc("dt_entry"), gift.getIc("source"),
					gift.getIc("note"), gift.getIc("recnum") };
			tableItem.setText(itemData);
		}
		for (TableColumn c : tableGifts.getColumns()) {
			c.pack();
		}
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	@Override
	public void setEnabled(boolean e) {
		tableGifts.setEnabled(e);
	}

	public Table getTable() {
		return tableGifts;
	}
	
	public void refresh() {
		tableGifts.removeAll();
		fillTable();
	}

}
