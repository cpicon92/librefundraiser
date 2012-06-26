package net.sf.librefundraiser.gui;

import java.io.File;

import net.sf.librefundraiser.Main;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class DatabasePropertiesDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text txtDatabaseName;
	private Text txtFileName;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DatabasePropertiesDialog(Shell parent, int style) {
		super(parent, style);
		String filename = null;
		try {
			filename = new File(Main.getSetting("lastDB")).getName();
		} catch (Exception e) {}
		if (filename != null) filename = filename + " ";
		setText(filename+"Properties");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
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
		
		Label lblDatabaseName = new Label(grpGeneral, SWT.NONE);
		lblDatabaseName.setBounds(0, 0, 49, 13);
		lblDatabaseName.setText("Database Name");
		
		txtDatabaseName = new Text(grpGeneral, SWT.BORDER);
		txtDatabaseName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDatabaseName.setBounds(0, 0, 76, 19);
		
		Group grpCustomFields = new Group(shell, SWT.NONE);
		grpCustomFields.setLayout(new GridLayout(1, false));
		grpCustomFields.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		grpCustomFields.setText("Custom Fields");

	}
}
