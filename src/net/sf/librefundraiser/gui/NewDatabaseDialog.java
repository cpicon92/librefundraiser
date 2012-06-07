package net.sf.librefundraiser.gui;
import net.sf.librefundraiser.LibreFundraiser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;


public class NewDatabaseDialog {

	protected String result;
	protected Shell shell;
	private Text txtFilename;
	private Button btnLocalDatabase;
	private Button btnExistingLocalDatabase;
	private Button btnRemoteDatabase;
	private Button btnBrowse;
	private boolean canceled = true;
	private Display display;
	private Button btnNext;
	private Object currentSelection;
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public String open() {
		createContents();
		shell.open();
		shell.layout();
		display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 * @wbp.parser.entryPoint
	 */
	private void createContents() {
		shell = new Shell(SWT.DIALOG_TRIM);
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (canceled) System.exit(0);
			}
		});
		shell.setSize(450, 370);
		shell.setImages(LibreFundraiser.logo);
		shell.setText("LibreFundraiser");
		GridLayout gl_shlLibreFundraiser = new GridLayout(1, false);
		gl_shlLibreFundraiser.marginWidth = 0;
		gl_shlLibreFundraiser.horizontalSpacing = 0;
		gl_shlLibreFundraiser.marginHeight = 0;
		gl_shlLibreFundraiser.verticalSpacing = 0;
		shell.setLayout(gl_shlLibreFundraiser);
		
		Composite compositeBanner = new Composite(shell, SWT.NONE);
		compositeBanner.setBackgroundMode(SWT.INHERIT_DEFAULT);
		compositeBanner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeBanner.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		compositeBanner.setBounds(0, 0, 64, 64);
		compositeBanner.setLayout(new GridLayout(2, false));
		
		Label lblFirstStart = new Label(compositeBanner, SWT.SHADOW_NONE);
		FontData[] fD = lblFirstStart.getFont().getFontData();
		fD[0].setHeight(14);
		lblFirstStart.setFont(new Font(display,fD[0]));
		GridData gd_lblFirstStart = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_lblFirstStart.horizontalIndent = 10;
		lblFirstStart.setLayoutData(gd_lblFirstStart);
		lblFirstStart.setText("Welcome to LibreFundraiser! ");
		
		Label lblLogo = new Label(compositeBanner, SWT.NONE);
		lblLogo.setImage(SWTResourceManager.getImage(NewDatabaseDialog.class, "/logo/balloon48.png"));
		lblLogo.setBounds(0, 0, 49, 13);
		
		new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite compositeMain = new Composite(shell, SWT.NONE);
		GridLayout gl_compositeMain = new GridLayout(1, false);
		gl_compositeMain.verticalSpacing = 10;
		gl_compositeMain.horizontalSpacing = 20;
		gl_compositeMain.marginWidth = 20;
		gl_compositeMain.marginHeight = 20;
		compositeMain.setLayout(gl_compositeMain);
		compositeMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblThisAppearsTo = new Label(compositeMain, SWT.WRAP);
		lblThisAppearsTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblThisAppearsTo.setBounds(0, 0, 49, 13);
		lblThisAppearsTo.setText("This appears to be the first time you are running LibreFundraiser. Before you begin, we have to make a database to store your donors. \r\n\r\nWould you like to use a local database (saved in a file), or a remote one (requires extra setup)? ");
		
		SelectionAdapter dbType = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				currentSelection = e.getSource();
				boolean browse = currentSelection.equals(btnExistingLocalDatabase);
				txtFilename.setEnabled(browse);
				btnBrowse.setEnabled(browse);
				if (currentSelection.equals(btnExistingLocalDatabase)||currentSelection.equals(btnLocalDatabase)) {
					btnNext.setText("Finish >");
				} else {
					btnNext.setText("Next >");
				}
			}
		};
		
		btnLocalDatabase = new Button(compositeMain, SWT.RADIO);
		btnLocalDatabase.addSelectionListener(dbType);
		btnLocalDatabase.setSelection(true);
		btnLocalDatabase.setBounds(0, 0, 83, 16);
		btnLocalDatabase.setText("Create new local database");
		
		btnExistingLocalDatabase = new Button(compositeMain, SWT.RADIO);
		btnExistingLocalDatabase.addSelectionListener(dbType);
		btnExistingLocalDatabase.setBounds(0, 0, 134, 16);
		btnExistingLocalDatabase.setText("Use existing local database");
		
		Composite compositeBrowse = new Composite(compositeMain, SWT.NONE);
		compositeBrowse.setBounds(0, 0, 64, 64);
		GridLayout gl_compositeBrowse = new GridLayout(2, false);
		gl_compositeBrowse.marginLeft = 15;
		gl_compositeBrowse.marginHeight = 0;
		compositeBrowse.setLayout(gl_compositeBrowse);
		
		txtFilename = new Text(compositeBrowse, SWT.BORDER);
		txtFilename.setEnabled(false);
		GridData gd_txtFilename = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtFilename.widthHint = 200;
		txtFilename.setLayoutData(gd_txtFilename);
		txtFilename.setBounds(0, 0, 76, 19);
		
		btnBrowse = new Button(compositeBrowse, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell,SWT.OPEN);
				fileDialog.setFilterExtensions(new String[]{"*.ldb","*.*"});
				fileDialog.setFilterNames(new String[]{"LibreFundraiser Database (*.ldb)","All Files"});
				String path = fileDialog.open();
				if (path != null) txtFilename.setText(path);
			}
		});
		btnBrowse.setEnabled(false);
		btnBrowse.setText("Browse...");
		
		btnRemoteDatabase = new Button(compositeMain, SWT.RADIO);
		btnRemoteDatabase.setEnabled(false);
		btnRemoteDatabase.addSelectionListener(dbType);
		btnRemoteDatabase.setBounds(0, 0, 83, 16);
		btnRemoteDatabase.setText("Connect to remote database");
		
		new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite compositeButtons = new Composite(shell, SWT.NONE);
		compositeButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		RowLayout rl_compositeButtons = new RowLayout(SWT.HORIZONTAL);
		rl_compositeButtons.marginBottom = 5;
		rl_compositeButtons.marginLeft = 5;
		rl_compositeButtons.marginRight = 5;
		rl_compositeButtons.marginTop = 5;
		rl_compositeButtons.spacing = 5;
		compositeButtons.setLayout(rl_compositeButtons);
		
		btnNext = new Button(compositeButtons, SWT.NONE);
		btnNext.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentSelection == null) return;
				if (currentSelection.equals(btnLocalDatabase)) {
					FileDialog fileDialog = new FileDialog(shell,SWT.SAVE);
					fileDialog.setFilterExtensions(new String[]{"*.ldb","*.*"});
					fileDialog.setFilterNames(new String[]{"LibreFundraiser Database (*.ldb)","All Files"});
					result = fileDialog.open();
				} else if (currentSelection.equals(btnExistingLocalDatabase)) {
					result = txtFilename.getText();
				}
				canceled = false;
				shell.dispose();
			}
		});
		btnNext.setLayoutData(new RowData(70, SWT.DEFAULT));
		btnNext.setBounds(0, 0, 68, 23);
		btnNext.setText("Finish >");
		
		Button btnCancel = new Button(compositeButtons, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		btnCancel.setLayoutData(new RowData(70, SWT.DEFAULT));
		btnCancel.setText("Cancel");
		currentSelection = btnLocalDatabase;
	}
}
