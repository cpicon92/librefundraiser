package net.sf.librefundraiser.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.text.SimpleDateFormat;

public class DatePickerTest extends Shell {

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			DatePickerTest shell = new DatePickerTest(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * @param display
	 */
	public DatePickerTest(Display display) {
		super(display, SWT.SHELL_TRIM);
		setLayout(new GridLayout(2, false));
		new DatePicker(new SimpleDateFormat("yyyy-MM-dd"), this, SWT.NONE);
		new DateTime(this, SWT.BORDER | SWT.DROP_DOWN);
		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("SWT Application");
		setSize(450, 300);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
