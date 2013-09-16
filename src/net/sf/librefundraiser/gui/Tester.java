package net.sf.librefundraiser.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;

public class Tester extends Composite {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public Tester(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		DateTime dateTime = new DateTime(this, SWT.BORDER | SWT.CALENDAR);
		dateTime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
