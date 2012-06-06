import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;


public class MainWindow {

	protected Shell shell;
	private Text txtSearch;
	private ToolItem tltmSave;
	private Composite compositeDonorList;
	private Display display;

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(880, 670);
		shell.setImages(LibreFundraiser.logo);
		String filename = null;
		try {
			filename = new File(LibreFundraiser.getSetting("lastDB")).getName();
		} catch (Exception e) {}
		if (filename != null) filename = " - " + filename;
		shell.setText("LibreFundraiser"+filename);
		GridLayout gl_shell = new GridLayout(1, false);
		gl_shell.marginWidth = 0;
		gl_shell.marginHeight = 0;
		shell.setLayout(gl_shell);

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");

		Menu menuFile = new Menu(mntmFile);
		mntmFile.setMenu(menuFile);

		MenuItem mntmNewDatabase = new MenuItem(menuFile, SWT.NONE);
		mntmNewDatabase.setText("New Database...");

		MenuItem mntmOpenDatabase = new MenuItem(menuFile, SWT.NONE);
		mntmOpenDatabase.setText("Open Database...");

		new MenuItem(menuFile, SWT.SEPARATOR);

		MenuItem mntmImport = new MenuItem(menuFile, SWT.CASCADE);
		mntmImport.setText("Import");

		Menu menuImport = new Menu(mntmImport);
		mntmImport.setMenu(menuImport);

		MenuItem mntmFromFundraiserBasic = new MenuItem(menuImport, SWT.NONE);
		mntmFromFundraiserBasic.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				importFRBW();
			}
		});
		mntmFromFundraiserBasic.setText("From FundRaiser Basic...");

		MenuItem mntmFromCsvFile = new MenuItem(menuImport, SWT.NONE);
		mntmFromCsvFile.setText("From CSV File...");

		new MenuItem(menuFile, SWT.SEPARATOR);

		MenuItem mntmExit = new MenuItem(menuFile, SWT.NONE);
		mntmExit.setText("Exit");

		MenuItem mntmDonor = new MenuItem(menu, SWT.CASCADE);
		mntmDonor.setText("Donor");

		Menu menuDonor = new Menu(mntmDonor);
		mntmDonor.setMenu(menuDonor);

		MenuItem mntmNewDonor = new MenuItem(menuDonor, SWT.NONE);
		mntmNewDonor.setText("New Donor");

		MenuItem mntmSaveCurrentDonor = new MenuItem(menuDonor, SWT.NONE);
		mntmSaveCurrentDonor.setText("Save Current Donor");

		MenuItem mntmSaveAllDonors = new MenuItem(menuDonor, SWT.NONE);
		mntmSaveAllDonors.setText("Save All Donors");
		
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("Help");
		
		Menu menuHelp = new Menu(mntmHelp);
		mntmHelp.setMenu(menuHelp);
		
		MenuItem mntmAbout = new MenuItem(menuHelp, SWT.NONE);
		mntmAbout.setText("About...");

		Composite compositeToolbar = new Composite(shell, SWT.NONE);
		compositeToolbar.setLayout(new GridLayout(3, false));

		ToolBar toolBar = new ToolBar(compositeToolbar, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toolBar.setBounds(0, 0, 80, 21);

		ToolItem tltmNewDonor = new ToolItem(toolBar, SWT.NONE);
		tltmNewDonor.setImage(SWTResourceManager.getImage(MainWindow.class, "/icons/new-donor.png"));
		tltmNewDonor.setText("New Donor");

		tltmSave = new ToolItem(toolBar, SWT.NONE);
		tltmSave.setToolTipText("Save");
		tltmSave.setEnabled(false);
		tltmSave.setImage(SWTResourceManager.getImage(MainWindow.class, "/icons/save.png"));

		ToolItem tltmSep = new ToolItem(toolBar, SWT.SEPARATOR);
		tltmSep.setText("sep");

		Label lblSearch = new Label(compositeToolbar, SWT.NONE);
		lblSearch.setBounds(0, 0, 49, 13);
		lblSearch.setText("Search");

		txtSearch = new Text(compositeToolbar, SWT.BORDER | SWT.SEARCH);
		GridData gd_txtSearch = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_txtSearch.widthHint = 150;
		txtSearch.setLayoutData(gd_txtSearch);
		txtSearch.setBounds(0, 0, 76, 19);

		compositeDonorList = new DonorList(shell, SWT.NONE);
		compositeDonorList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

	}
	public ToolItem getSaveButton() {
		return tltmSave;
	}
	public String newDBfile() {
		FileDialog fileDialog = new FileDialog(shell,SWT.SAVE);
		fileDialog.setFilterExtensions(new String[]{"*.ldb","*.*"});
		fileDialog.setFilterNames(new String[]{"LibreFundraiser Database (*.ldb)","All Files"});
		return fileDialog.open();
	}
	public void importFRBW() {
		MessageBox warning = new MessageBox(shell,SWT.ICON_WARNING|SWT.YES|SWT.NO);
		warning.setText("LibreFundraiser Warning");
		warning.setMessage("The imported data will overwrite anything you currently have in your database. Do you want to continue?");
		if (warning.open() == SWT.NO) return;
		final FundRaiserImportDialog dialog = new FundRaiserImportDialog(shell,SWT.NONE);
		DirectoryDialog fileDialog = new DirectoryDialog(shell);
		fileDialog.setMessage("Please indicate the FundRaiser Basic installation folder");
		String systemDrive = System.getenv("SystemDrive");
		fileDialog.setFilterPath(systemDrive+"\\FRBW");
		final String result = fileDialog.open();
		if (result == null) return;
		new Thread(new Runnable() {
			public void run() {
				FileDBASE db = new FileDBASE(result);
				display.asyncExec(new Runnable() {
					public void run() {
						dialog.setCancelable(false);
						dialog.setStatusText("Importing donor list...");
					}
				});
				if (!db.loadTable("Master.dbf","donors")) {
					display.asyncExec(new Runnable() {
						public void run() {
							MessageBox error = new MessageBox(shell,SWT.ICON_ERROR);
							error.setText("LibreFundraiser Error");
							error.setMessage("Could not load donors. This probably isn't a FundRaiser basic installation folder...");
							dialog.dispose();
						}
					});
					return;
				}
				display.asyncExec(new Runnable() {
					public void run() {
						dialog.setProgress(25);
						dialog.setStatusText("Importing gifts...");
					}
				});
				db.loadTable("Gifts.dbf","gifts");
				display.asyncExec(new Runnable() {
					public void run() {
						dialog.setProgress(50);
						dialog.setStatusText("Consolidating donors and gifts...");
					}
				});
				((DonorList)compositeDonorList).donors = LibreFundraiser.getLocalDB().getDonors();
				display.asyncExec(new Runnable() {
					public void run() {
						dialog.setProgress(75);
						dialog.setStatusText("Refreshing donor list...");
					}
				});
				display.asyncExec(new Runnable() {
					public void run() {
						((DonorList)compositeDonorList).refresh();
						dialog.dispose();
					}
				});
			}
		}).start();
		dialog.open();
	}
}
