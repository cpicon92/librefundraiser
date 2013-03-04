package net.sf.librefundraiser.gui;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.sf.librefundraiser.Donor;
import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ResourceManager;
import net.sf.librefundraiser.db.ODB;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;


public class MainWindow {

	protected Shell shell;
	private Text txtSearch;
	private ToolItem tltmSave;
	private Composite compositeDonorList;
	private Display display;
	private List listSearch;
	private Shell shellSearch;
	private long popupTimer = System.currentTimeMillis();
	private Runnable saveCurrent;

	/**
	 * Open the window.
	 */
	public void open(String importDb) {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		if (importDb != null) {
			Main.importFromFRBW(Display.getDefault(), shell, this, importDb);
		}
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
		shell.setMinimumSize(new Point(800, 552));
		shell.setSize(880, 670);
		shell.setImages(ResourceManager.getLogo());
		shell.setMaximized(true);
		shell.addListener(SWT.Close, new Listener() {	
			@Override
			public void handleEvent(Event event) {
				if (!((DonorList)compositeDonorList).closeAllTabs()) event.doit = false;
			}
		});
		refreshTitle();
		GridLayout gl_shell = new GridLayout(1, false);
		gl_shell.verticalSpacing = 0;
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
		mntmNewDatabase.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell,SWT.SAVE);
				fileDialog.setFilterExtensions(new String[]{"*.lfd","*.*"});
				fileDialog.setFilterNames(new String[]{"LibreFundraiser Database (*.lfd)","All Files"});
				String path = fileDialog.open();
				if (path != null) {
					Main.addSetting("lastDB",path);
					Main.resetLocalDB();
					Main.refresh();
				}
			}
		});
		mntmNewDatabase.setText("New Local Database...");

		MenuItem mntmOpenDatabase = new MenuItem(menuFile, SWT.NONE);
		mntmOpenDatabase.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell,SWT.OPEN);
				fileDialog.setFilterExtensions(new String[]{"*.lfd","*.*"});
				fileDialog.setFilterNames(new String[]{"LibreFundraiser Database (*.lfd)","All Files"});
				String path = fileDialog.open();
				if (path != null) {
					Main.addSetting("lastDB",path);
					Main.resetLocalDB();
					Main.refresh();
				}
			}
		});
		mntmOpenDatabase.setText("Open Local Database...");

		MenuItem mntmConnectToRemote = new MenuItem(menuFile, SWT.NONE);
		mntmConnectToRemote.setEnabled(false);
		mntmConnectToRemote.setText("Connect to Remote Database...");

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
		mntmFromCsvFile.setEnabled(false);
		mntmFromCsvFile.setText("From CSV File...");

		MenuItem mntmExport = new MenuItem(menuFile, SWT.CASCADE);
		mntmExport.setText("Export");

		Menu menu_1 = new Menu(mntmExport);
		mntmExport.setMenu(menu_1);

		MenuItem mntmCsv = new MenuItem(menu_1, SWT.NONE);
		mntmCsv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setFilterExtensions(new String[]{"*.csv","*.*"});
				fileDialog.setFilterNames(new String[]{"Comma Separated Values (*.csv)","All Files"});
				final String path = fileDialog.open();
				if (path == null) return;
				File f = new File(path);
				((DonorList)compositeDonorList).writeCSV(f);
			}
		});
		mntmCsv.setText("To CSV file...");
		
		MenuItem mntmOds = new MenuItem(menu_1, SWT.NONE);
		mntmOds.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setFilterExtensions(new String[]{"*.ods","*.*"});
				fileDialog.setFilterNames(new String[]{"Open Document Spreadsheet (*.ods)","All Files"});
				final String path = fileDialog.open();
				if (path == null) return;
				File f = new File(path);
				((DonorList)compositeDonorList).writeODS(f, true);
			}
		});
		mntmOds.setText("To ODS (LibreOffice Spreadsheet) file...");
		
		MenuItem mntmOdb = new MenuItem(menu_1, SWT.NONE);
		mntmOdb.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setFilterExtensions(new String[]{"*.odb","*.*"});
				fileDialog.setFilterNames(new String[]{"Open Document Database (*.odb)","All Files"});
				final String path = fileDialog.open();
				if (path == null) return;
				File f = new File(path);
				MessageBox registrationWarning = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
				registrationWarning.setText("LibreFundraiser");
				registrationWarning.setMessage("Would you like LibreFundraiser to register the database with LibreOffice Base?" +
						"This way you may create labels and other mail-merge documents with it. \n\n" +
						"WARNING: Please save and close all open LibreOffice documents, LibreOffice will quit if you choose 'yes'.");
				boolean saidYes = registrationWarning.open() == SWT.YES;
				ODB.exportToODB(f, saidYes);
			}
		});
		mntmOdb.setText("To ODB (LibreOffice Database) file...");

		new MenuItem(menuFile, SWT.SEPARATOR);
		
		MenuItem mntmUpdateStats = new MenuItem(menuFile, SWT.NONE);
		mntmUpdateStats.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateAllDonorStats();
			}
		});
		mntmUpdateStats.setText("Update donor statistics");
		
		new MenuItem(menuFile, SWT.SEPARATOR);

		MenuItem mntmExit = new MenuItem(menuFile, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		mntmExit.setText("Exit");

		MenuItem mntmEdit = new MenuItem(menu, SWT.CASCADE);
		mntmEdit.setText("Edit");

		Menu menuEdit = new Menu(mntmEdit);
		mntmEdit.setMenu(menuEdit);

		MenuItem mntmCut = new MenuItem(menuEdit, SWT.NONE);
		mntmCut.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control focused = getFocusControl();
				if (focused.getClass().equals(Text.class)) {
					((Text) focused).cut();
				} else if (focused.getClass().equals(Combo.class)) {
					((Combo) focused).cut();
				}
			}
		});
		mntmCut.setText("Cut\tCtrl+X");

		MenuItem mntmCopy = new MenuItem(menuEdit, SWT.NONE);
		mntmCopy.setText("Copy\tCtrl+C");
		mntmCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control focused = getFocusControl();
				if (focused.getClass().equals(Text.class)) {
					((Text) focused).copy();
				} else if (focused.getClass().equals(Combo.class)) {
					((Combo) focused).copy();
				}
			}
		});

		MenuItem mntmPaste = new MenuItem(menuEdit, SWT.NONE);
		mntmPaste.setText("Paste\tCtrl+V");
		mntmPaste.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control focused = getFocusControl();
				if (focused.getClass().equals(Text.class)) {
					((Text) focused).paste();
				} else if (focused.getClass().equals(Combo.class)) {
					((Combo) focused).paste();
				}
			}
		});

		MenuItem mntmDonor = new MenuItem(menu, SWT.CASCADE);
		mntmDonor.setText("Donor");

		Menu menuDonor = new Menu(mntmDonor);
		mntmDonor.setMenu(menuDonor);

		MenuItem mntmNewDonor = new MenuItem(menuDonor, SWT.NONE);
		mntmNewDonor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newDonor();
			}
		});
		mntmNewDonor.setText("New Donor");

		final MenuItem mntmSaveCurrentDonor = new MenuItem(menuDonor, SWT.NONE);
		mntmSaveCurrentDonor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (saveCurrent == null) return;
				saveCurrent.run();
			}
		});
		menuDonor.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				mntmSaveCurrentDonor.setEnabled(tltmSave.getEnabled());
			}
		});
		mntmSaveCurrentDonor.setText("Save Current Donor");

		MenuItem mntmSaveAllDonors = new MenuItem(menuDonor, SWT.NONE);
		mntmSaveAllDonors.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				((DonorList)compositeDonorList).saveAll();
			}
		});
		mntmSaveAllDonors.setText("Save All Donors");

		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("Help");

		Menu menuHelp = new Menu(mntmHelp);
		mntmHelp.setMenu(menuHelp);

		MenuItem mntmAbout = new MenuItem(menuHelp, SWT.NONE);
		mntmAbout.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new AboutDialog(shell, SWT.DIALOG_TRIM).open();
			}
		});
		mntmAbout.setText("About...");

		Composite compositeToolbar = new Composite(shell, SWT.NONE);
		compositeToolbar.setLayout(new GridLayout(2, false));

		ToolBar toolBar = new ToolBar(compositeToolbar, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		toolBar.setBounds(0, 0, 80, 21);

		ToolItem tltmNewDonor = new ToolItem(toolBar, SWT.NONE);
		tltmNewDonor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newDonor();
			}
		});
		tltmNewDonor.setImage(ResourceManager.getIcon("new-donor.png"));
		tltmNewDonor.setText("New Donor");

		tltmSave = new ToolItem(toolBar, SWT.NONE);
		tltmSave.setToolTipText("Save");
		tltmSave.setEnabled(false);
		tltmSave.setImage(ResourceManager.getIcon("save.png"));
		tltmSave.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (saveCurrent == null) return;
				saveCurrent.run();
			}
		});
		new ToolItem(toolBar, SWT.SEPARATOR);

		ToolItem tltmDbProperties = new ToolItem(toolBar, SWT.NONE);
		tltmDbProperties.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new DatabasePropertiesDialog(shell, SWT.DIALOG_TRIM).open();
			}
		});
		tltmDbProperties.setText("Database Properties");
		tltmDbProperties.setImage(ResourceManager.getIcon("db-properties.png"));

		//TODO add advanced search with SQL queries

		new ToolItem(toolBar, SWT.SEPARATOR);

		shellSearch = new Shell(shell, SWT.NONE);
		listSearch = new List(shellSearch, SWT.SINGLE);
		shellSearch.setLayout(new FillLayout());
		shellSearch.addShellListener(new ShellListener() {
			public void shellActivated(ShellEvent e) {
				txtSearch.setFocus();
				Rectangle bounds = txtSearch.getBounds();
				Point location = txtSearch.toDisplay(-2, bounds.height-2);
				shellSearch.setLocation(location);
				shellSearch.pack();
			}
			public void shellClosed(ShellEvent e) {
			}
			public void shellDeactivated(ShellEvent e) {
			}
			public void shellDeiconified(ShellEvent e) {
			}
			public void shellIconified(ShellEvent e) {
			}
		});

		txtSearch = new Text(compositeToolbar, SWT.BORDER | SWT.H_SCROLL | SWT.SEARCH | SWT.CANCEL);
		txtSearch.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				display.timerExec(100, new Runnable() {
					public void run() {
						try {
							if (!display.getFocusControl().equals(txtSearch) && !display.getFocusControl().equals(listSearch)) {
								shellSearch.setVisible(false);
							}
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
		GridData gd_txtSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtSearch.widthHint = 150;
		txtSearch.setLayoutData(gd_txtSearch);
		txtSearch.setBounds(0, 0, 76, 19);
		txtSearch.setMessage("Quick Find");

		listSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				quickSearchOpen();
			}
		});

		txtSearch.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.ARROW_DOWN: 
					listSearch.select(listSearch.getSelectionIndex()+1);
					break;
				case SWT.ARROW_UP:
					listSearch.select(listSearch.getSelectionIndex()-1);
					break;
				case SWT.ESC:
					shellSearch.setVisible(false);
					break;
				}
			}
		});

		txtSearch.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP) {
					display.timerExec(1, new Runnable() {
						public void run() {
							txtSearch.setSelection(txtSearch.getCharCount()+1);
						}
					});
				}
				if (e.detail == SWT.TRAVERSE_RETURN) {
					quickSearchOpen();
				}
			}
		});
		txtSearch.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				popupTimer = System.currentTimeMillis();
				display.timerExec(500, new Runnable() {
					long time = System.currentTimeMillis();
					public void run() {
						if (time != popupTimer) return;
						shellSearch.setVisible(false);
						listSearch.setItems(new String[]{});
						if (txtSearch.getCharCount() > 1) {
							HashMap<String,String> results = Main.getDonorDB().quickSearch(txtSearch.getText());
							ArrayList<String> keys = new ArrayList<String>();
							int maxItems = 10;
							int items = 0;
							for (Entry<String, String> entry : results.entrySet()) {
								items++;
								if (items >= maxItems) break;
								String key = entry.getKey();
								String value = entry.getValue();
								keys.add(key);
								listSearch.add(value);
							}
							if (items > 0) {
								listSearch.setData(keys);
								Rectangle bounds = txtSearch.getBounds();
								Point location = txtSearch.toDisplay(-2, bounds.height-2);
								shellSearch.setLocation(location);
								shellSearch.setMinimumSize(bounds.width, 0);
								shellSearch.pack();
								shellSearch.setVisible(true);
								listSearch.select(0);
							}
						}
					}
				});
			}
		});

		shell.addControlListener(new ControlAdapter() {
			public void controlMoved(ControlEvent e) {
				carryResults();
			}
			public void controlResized(ControlEvent e) {
				carryResults();
			}
			private void carryResults() {
				Rectangle bounds = txtSearch.getBounds();
				Point location = txtSearch.toDisplay(-2, bounds.height-2);
				shellSearch.setLocation(location);
			}
		});

		compositeDonorList = new DonorList(shell, SWT.NONE);
		compositeDonorList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

	}
	public ToolItem getSaveButton() {
		return tltmSave;
	}
	public void setSaveAction(Runnable r) {
		saveCurrent = r;
	}
	//TODO reconcile this with the method in Main
	public String newDBfile() {
		FileDialog fileDialog = new FileDialog(shell,SWT.SAVE);
		fileDialog.setFilterExtensions(new String[]{"*.lfd","*.*"});
		fileDialog.setFilterNames(new String[]{"LibreFundraiser Database (*.lfd)","All Files"});
		return fileDialog.open();
	}
	public void importFRBW() {
		MessageBox warning = new MessageBox(shell,SWT.ICON_WARNING|SWT.YES|SWT.NO);
		warning.setText("LibreFundraiser Warning");
		warning.setMessage("The imported data will overwrite anything you currently have in your database. Do you want to continue?");
		if (warning.open() == SWT.NO) return;
		DirectoryDialog fileDialog = new DirectoryDialog(shell);
		fileDialog.setMessage("Please indicate the FundRaiser Basic installation folder");
		String systemDrive = System.getenv("SystemDrive");
		fileDialog.setFilterPath(systemDrive+"\\FRBW");
		final String path = fileDialog.open();
		Main.importFromFRBW(display, shell, this, path);
	}
	public DonorList getCompositeDonorList() {
		return (DonorList) compositeDonorList;
	}

	public void refresh() {
		refresh(true);
	}

	public void refresh(boolean reload) {
		if (reload) reload();
		compositeDonorList.setVisible(false);
		((DonorList)compositeDonorList).refresh();
		compositeDonorList.setVisible(true);
	}

	public void reload() {
		((DonorList)compositeDonorList).donors = Main.getDonorDB().getDonors();
	}
	
	public void updateAllDonorStats() {
		//TODO: make a progress window and optimize this... 
		Donor[] donors = ((DonorList)compositeDonorList).donors;
		int i = 1;
		int percent = 0;
		for (Donor d : donors) {
			int newPercent = (int) Math.round(100*((double)i/(double)donors.length));
			if (newPercent != percent) {
				percent = newPercent;
				System.out.print(percent + (percent==100?"":", "));
			}
			d.updateStats();
			i++;
		}
		System.out.println();
		System.out.println("Saving to disk...");
		Main.getDonorDB().saveDonors(donors);
		System.out.println("Done");
		refresh(false);
	}

	private void quickSearchOpen() {
		@SuppressWarnings("unchecked")
		ArrayList<String> keys = (ArrayList<String>)listSearch.getData();
		try {
			if (keys != null && !keys.isEmpty()) {
				String key = keys.get(listSearch.getSelectionIndex());
				int id = Integer.parseInt(key);
				DonorTab newTab = new DonorTab(id,((DonorList)compositeDonorList).tabFolder);
				shellSearch.setVisible(false);
				listSearch.setItems(new String[]{});
				txtSearch.setText("");
				((DonorList)compositeDonorList).tabFolder.setSelection(newTab);
				((DonorList)compositeDonorList).tabFolder.setFocus();
			}
		} catch (Exception e1) {}
	}

	public void newDonor() {
		((DonorList)compositeDonorList).newDonor();
	}
	
	public void refreshTitle() {
		String filename = null;
		try {
			filename = Main.getDonorDB().getDbName();
		} catch (Exception e) {}
		if (filename.equals("")) {
			filename = Main.getDonorDB().getDbPath();
		}
		if (!filename.equals("")) filename = " - " + filename;
		shell.setText("LibreFundraiser"+filename);
	}
	
	public Control getFocusControl() {
		return this.shell.getDisplay().getFocusControl();
	}
}
