package net.sf.librefundraiser.gui;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ResourceManager;


public class NewDatabaseWizard {

	protected String result;
	protected Shell shell;
	private boolean canceled = true;
	private String frbwImportFile = null;
	private Display display;
	private final int width = 450;
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
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (canceled) System.exit(0);
			}
		});
		shell.setSize(shell.computeSize(width, SWT.DEFAULT));
		shell.setImages(ResourceManager.getLogo());
		shell.setText("LibreFundraiser");
		final StackLayout sl_shlLibreFundraiser = new StackLayout();
		shell.setLayout(sl_shlLibreFundraiser);
		final Composite compositeFirstPage = new Composite(shell, SWT.NONE);
		final Composite compositeLocalDbPage = new Composite(shell, SWT.NONE);
		sl_shlLibreFundraiser.topControl = compositeFirstPage;
		{
			GridLayout gl_firstPage = new GridLayout(1, false);
			gl_firstPage.marginWidth = 0;
			gl_firstPage.horizontalSpacing = 0;
			gl_firstPage.marginHeight = 0;
			gl_firstPage.verticalSpacing = 0;

			compositeFirstPage.setLayout(gl_firstPage);


			Composite compositeBanner = new Composite(compositeFirstPage, SWT.NONE);
			compositeBanner.setBackgroundMode(SWT.INHERIT_DEFAULT);
			compositeBanner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			compositeBanner.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
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
			lblLogo.setImage(ResourceManager.getLogo(48));
			lblLogo.setBounds(0, 0, 49, 13);

			new Label(compositeFirstPage, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Composite compositeMain = new Composite(compositeFirstPage, SWT.NONE);
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

			final Button btnLocalDatabase = new Button(compositeMain, SWT.RADIO);
			btnLocalDatabase.setSelection(true);
			btnLocalDatabase.setBounds(0, 0, 83, 16);
			btnLocalDatabase.setText("Create new local database, or import from Fundraiser Basic");
			final Object[] currentSelection = {btnLocalDatabase};

			final Button btnExistingLocalDatabase = new Button(compositeMain, SWT.RADIO);
			btnExistingLocalDatabase.setBounds(0, 0, 134, 16);
			btnExistingLocalDatabase.setText("Use existing local database");
			

			Composite compositeBrowse = new Composite(compositeMain, SWT.NONE);
			compositeBrowse.setBounds(0, 0, 64, 64);
			GridLayout gl_compositeBrowse = new GridLayout(2, false);
			gl_compositeBrowse.marginLeft = 15;
			gl_compositeBrowse.marginHeight = 0;
			compositeBrowse.setLayout(gl_compositeBrowse);

			final Text txtFilename = new Text(compositeBrowse, SWT.BORDER);
			txtFilename.setEnabled(false);
			GridData gd_txtFilename = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_txtFilename.widthHint = 200;
			txtFilename.setLayoutData(gd_txtFilename);
			txtFilename.setBounds(0, 0, 76, 19);

			final Button btnBrowse = new Button(compositeBrowse, SWT.NONE);
			btnBrowse.setEnabled(false);
			btnBrowse.setText("Browse...");

			final Button btnRemoteDatabase = new Button(compositeMain, SWT.RADIO);
			btnRemoteDatabase.setEnabled(false);
			btnRemoteDatabase.setBounds(0, 0, 83, 16);
			btnRemoteDatabase.setText("Connect to remote database");

			new Label(compositeFirstPage, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Composite compositeButtons = new Composite(compositeFirstPage, SWT.NONE);
			compositeButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			RowLayout rl_compositeButtons = new RowLayout(SWT.HORIZONTAL);
			rl_compositeButtons.pack = false;
			rl_compositeButtons.marginBottom = 5;
			rl_compositeButtons.marginLeft = 5;
			rl_compositeButtons.marginRight = 5;
			rl_compositeButtons.marginTop = 5;
			rl_compositeButtons.spacing = 5;
			compositeButtons.setLayout(rl_compositeButtons);

			final Button btnNext = new Button(compositeButtons, SWT.NONE);
			btnNext.setLayoutData(new RowData(80, SWT.DEFAULT));
			btnNext.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (currentSelection[0] == null) return;
					if (currentSelection[0].equals(btnLocalDatabase)) {
						compositeFirstPage.setVisible(false);
						compositeLocalDbPage.setVisible(true);
						sl_shlLibreFundraiser.topControl = compositeLocalDbPage;
					} else if (currentSelection[0].equals(btnExistingLocalDatabase)) {
						if (Main.fileExists(txtFilename.getText())) {
							result = txtFilename.getText();
							canceled = false;
							shell.dispose();
						} else {
							btnNext.setEnabled(false);
							btnBrowse.setFocus();
						}
					}
				}
			});
			btnNext.setBounds(0, 0, 68, 23);
			btnNext.setText("Next >");
			
			btnBrowse.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog fileDialog = new FileDialog(shell,SWT.OPEN);
					fileDialog.setFilterExtensions(new String[]{"*.lfd","*.*"});
					fileDialog.setFilterNames(new String[]{"LibreFundraiser Database (*.lfd)","All Files"});
					String path = fileDialog.open();
					if (Main.fileExists(path)) {
						txtFilename.setText(path);
						btnNext.setEnabled(true);
					}
				}
			});

			Button btnCancel = new Button(compositeButtons, SWT.NONE);
			btnCancel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					shell.dispose();
				}
			});
			btnCancel.setText("Cancel");
			SelectionAdapter dbType = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					currentSelection[0] = e.getSource();
					boolean browse = currentSelection[0].equals(btnExistingLocalDatabase);
					txtFilename.setEnabled(browse);
					btnBrowse.setEnabled(browse);
					if (currentSelection[0].equals(btnExistingLocalDatabase)) {
						btnNext.setText("Finish");
						btnNext.setEnabled(Main.fileExists(txtFilename.getText()));
					} else {
						btnNext.setEnabled(true);
						btnNext.setText("Next >");
					}
				}
			};
			btnLocalDatabase.addSelectionListener(dbType);
			btnExistingLocalDatabase.addSelectionListener(dbType);
			btnRemoteDatabase.addSelectionListener(dbType);
		}
		{
			GridLayout gl_localDbPage = new GridLayout(1, false);
			gl_localDbPage.marginWidth = 0;
			gl_localDbPage.horizontalSpacing = 0;
			gl_localDbPage.marginHeight = 0;
			gl_localDbPage.verticalSpacing = 0;

			compositeLocalDbPage.setLayout(gl_localDbPage);


			Composite compositeBanner = new Composite(compositeLocalDbPage, SWT.NONE);
			compositeBanner.setBackgroundMode(SWT.INHERIT_DEFAULT);
			compositeBanner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			compositeBanner.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			compositeBanner.setBounds(0, 0, 64, 64);
			compositeBanner.setLayout(new GridLayout(2, false));

			Label lblFirstStart = new Label(compositeBanner, SWT.SHADOW_NONE);
			FontData[] fD = lblFirstStart.getFont().getFontData();
			fD[0].setHeight(14);
			lblFirstStart.setFont(new Font(display,fD[0]));
			GridData gd_lblFirstStart = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_lblFirstStart.horizontalIndent = 10;
			lblFirstStart.setLayoutData(gd_lblFirstStart);
			lblFirstStart.setText("Creating a new local database");

			Label lblLogo = new Label(compositeBanner, SWT.NONE);
			lblLogo.setImage(ResourceManager.getLogo(48));
			lblLogo.setBounds(0, 0, 49, 13);

			new Label(compositeLocalDbPage, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Composite compositeMain = new Composite(compositeLocalDbPage, SWT.NONE);
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
			lblThisAppearsTo.setText("Please select the location for your new database");

			Composite compositeBrowse = new Composite(compositeMain, SWT.NONE);
			compositeBrowse.setBounds(0, 0, 64, 64);
			GridLayout gl_compositeBrowse = new GridLayout(2, false);
			gl_compositeBrowse.marginLeft = 15;
			gl_compositeBrowse.marginHeight = 0;
			compositeBrowse.setLayout(gl_compositeBrowse);

			final Text txtFilename = new Text(compositeBrowse, SWT.BORDER);
			GridData gd_txtFilename = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_txtFilename.widthHint = 200;
			txtFilename.setLayoutData(gd_txtFilename);
			txtFilename.setBounds(0, 0, 76, 19);

			final Button btnBrowse = new Button(compositeBrowse, SWT.NONE);
			btnBrowse.setText("Browse...");
			
			Label lblWouldYouLike = new Label(compositeMain, SWT.WRAP);
			lblWouldYouLike.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			lblWouldYouLike.setText("Would you like this database to be blank, or would you like to import data from Fundraiser Basic?");

			final Button btnBlankDb = new Button(compositeMain, SWT.RADIO);
			btnBlankDb.setSelection(true);
			btnBlankDb.setBounds(0, 0, 83, 16);
			btnBlankDb.setText("Create a blank database");
			final Object[] currentSelection = {btnBlankDb};
			
			final Button btnImportedDb = new Button(compositeMain, SWT.RADIO);
			btnImportedDb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			btnImportedDb.setText("Create a database with imported data");
			
			Composite compositeBrowseImport = new Composite(compositeMain, SWT.NONE);
			GridData gd_compositeBrowseImport = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
			gd_compositeBrowseImport.minimumHeight = 55;
			gd_compositeBrowseImport.verticalIndent = -10;
			compositeBrowseImport.setLayoutData(gd_compositeBrowseImport);
			compositeBrowseImport.setBounds(0, 0, 64, 64);
			GridLayout gl_compositeBrowseImport = new GridLayout(2, false);
			gl_compositeBrowseImport.marginLeft = 15;
			gl_compositeBrowseImport.marginHeight = 0;
			compositeBrowseImport.setLayout(gl_compositeBrowseImport);
			final Label frbwNote = new Label(compositeBrowseImport, SWT.NONE);
			frbwNote.setEnabled(false);
			frbwNote.setText("(please indicate the location of the Fundraiser Basic installation)");
			frbwNote.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			final Text txtFilenameImport = new Text(compositeBrowseImport, SWT.BORDER);
			txtFilenameImport.setEnabled(false);
			GridData gd_txtFilenameImport = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_txtFilenameImport.widthHint = 200;
			txtFilenameImport.setLayoutData(gd_txtFilenameImport);
			txtFilenameImport.setBounds(0, 0, 76, 19);

			final Button btnBrowseImport = new Button(compositeBrowseImport, SWT.NONE);
			btnBrowseImport.setEnabled(false);
			btnBrowseImport.setText("Browse...");
			

			new Label(compositeLocalDbPage, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			Composite compositeButtons = new Composite(compositeLocalDbPage, SWT.NONE);
			compositeButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			RowLayout rl_compositeButtons = new RowLayout(SWT.HORIZONTAL);
			rl_compositeButtons.pack = false;
			rl_compositeButtons.marginBottom = 5;
			rl_compositeButtons.marginLeft = 5;
			rl_compositeButtons.marginRight = 5;
			rl_compositeButtons.marginTop = 5;
			rl_compositeButtons.spacing = 5;
			compositeButtons.setLayout(rl_compositeButtons);
			
			Button btnBack = new Button(compositeButtons, SWT.NONE);
			btnBack.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					compositeFirstPage.setVisible(true);
					compositeLocalDbPage.setVisible(false);
					sl_shlLibreFundraiser.topControl = compositeFirstPage;
				}
			});
			btnBack.setLayoutData(new RowData(80, SWT.DEFAULT));
			btnBack.setText("< Back");

			final String[] pickedPath = {""};
			
			final Button btnFinish = new Button(compositeButtons, SWT.NONE);
			btnFinish.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (currentSelection[0] == null) return;
					result = txtFilename.getText();
					if (!result.equals(pickedPath[0]) && !Main.pathWritable(result)) result = Main.newDbFilePrompt(shell);
					if (currentSelection[0].equals(btnImportedDb)) {
						frbwImportFile = txtFilenameImport.getText();
					}
					canceled = false;
					shell.dispose();
				}
			});
			
			btnBrowse.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String path = Main.newDbFilePrompt(shell);
					if (path != null) {
						txtFilename.setText(path);
						pickedPath[0] = path;
					}
					if (path != null && currentSelection[0] == btnBlankDb) btnFinish.setEnabled(true);
				}
			});			
			btnBrowseImport.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					DirectoryDialog fileDialog = new DirectoryDialog(shell);
					fileDialog.setMessage("Please indicate the FundRaiser Basic installation folder");
					String systemDrive = System.getenv("SystemDrive");
					fileDialog.setFilterPath(systemDrive+"\\FRBW");
					final String path = fileDialog.open();
					if (path != null) {
						txtFilenameImport.setText(path);
						btnFinish.setEnabled(true);
					}
				}
			});
			btnFinish.setBounds(0, 0, 68, 23);
			btnFinish.setText("Finish");
			btnFinish.setEnabled(false);
			
			SelectionAdapter dbContents = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					currentSelection[0] = e.getSource();
					boolean importEnabled = false;
					if (currentSelection[0].equals(btnImportedDb)) {
						importEnabled = true;
						if (txtFilename.getText().equals("") || txtFilenameImport.getText().equals("")) {
							btnFinish.setEnabled(false);
						} else {
							btnFinish.setEnabled(true);
						}
					} else {
						if (txtFilename.getText().equals("")) {
							btnFinish.setEnabled(false);
						} else {
							btnFinish.setEnabled(true);
						}
					}
					frbwNote.setEnabled(importEnabled);
					btnBrowseImport.setEnabled(importEnabled);
					txtFilenameImport.setEnabled(importEnabled);
				}
			};
			btnBlankDb.addSelectionListener(dbContents);
			btnImportedDb.addSelectionListener(dbContents);

			Button btnCancel = new Button(compositeButtons, SWT.NONE);
			btnCancel.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					shell.dispose();
				}
			});
			btnCancel.setText("Cancel");
		}
		shell.setSize(shell.computeSize(width, SWT.DEFAULT));
	}

	public String getFrbwImportFile() {
		return frbwImportFile;
	}
}
