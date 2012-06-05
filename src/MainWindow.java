import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
		shell.setSize(883, 670);
		shell.setText("LibreFundraiser");
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
			@Override
			public void widgetSelected(SelectionEvent e) {
				final FundRaiserImportDialog dialog = new FundRaiserImportDialog(shell,SWT.NONE);
				new Thread(new Runnable() {
					public void run() {
						FileDBASE db = new FileDBASE("C:\\FRBW");
						db.loadTable("Master.dbf","donors");
						display.asyncExec(new Runnable() {
							public void run() {
								dialog.setProgress(50);
							}
						});
						db.loadTable("Gifts.dbf","gifts");
						display.asyncExec(new Runnable() {
							public void run() {
								dialog.setProgress(80);
							}
						});
						((DonorList)compositeDonorList).donors = LibreFundraiser.getLocalDB().getDonors();
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
}
