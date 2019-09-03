package net.sf.librefundraiser.gui;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import net.sf.librefundraiser.Main;
import net.sf.librefundraiser.ResourceManager;
import net.sf.librefundraiser.Util;
import net.sf.librefundraiser.db.ODB;
import net.sf.librefundraiser.io.Donor;
import net.sf.librefundraiser.tabs.TabFolder;
import net.sf.librefundraiser.tabs.TabFolderEvent;
import net.sf.librefundraiser.tabs.TabFolderListener;


public class MainWindow {

	protected Shell shell;
	private ToolItem tltmSave;
	private DonorTabFolder donorTabFolder;
	private Display display;
	private Runnable saveCurrent;
	private DonorTable donorTable;

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
		refresh();
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
				if (!donorTabFolder.closeAllTabs()) event.doit = false;
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
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = Main.newDbFilePrompt(shell);
				if (path != null) {
					Main.addSetting("lastDB",path);
					Main.resetLocalDB();
					refreshTitle();
					refresh();
				}
			}
		});
		mntmNewDatabase.setText("New Local Database...");

		MenuItem mntmOpenDatabase = new MenuItem(menuFile, SWT.NONE);
		mntmOpenDatabase.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell,SWT.OPEN);
				fileDialog.setFilterExtensions(new String[]{"*.lfd","*.*"});
				fileDialog.setFilterNames(new String[]{"LibreFundraiser Database (*.lfd)","All Files"});
				String path = fileDialog.open();
				if (path != null) {
					Main.addSetting("lastDB",path);
					Main.resetLocalDB();
					refreshTitle();
					refresh();
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

		MenuItem mntmFromFBRW = new MenuItem(menuImport, SWT.NONE);
		mntmFromFBRW.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				importFRBW();
			}
		});
		mntmFromFBRW.setText("From FundRaiser Basic...");

		MenuItem mntmFromCsv = new MenuItem(menuImport, SWT.NONE);
		mntmFromCsv.setText("From CSV File...");
		mntmFromCsv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent ev) {
				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog.setFilterExtensions(new String[]{"*.csv","*.*"});
				fileDialog.setFilterNames(new String[]{"Comma Separated Values (*.csv)","All Files"});
				final String path = fileDialog.open();
				if (path == null) return;
				File f = new File(path);
				if (!f.exists()) return;
				//TODO check Excel compatibility
				try (CSVParser csvParser = Util.readCSV(f, CSVFormat.DEFAULT)) {
					Iterator<CSVRecord> iterRecord = csvParser.iterator();
					CSVRecord rawHeaders = iterRecord.next();
					List<String> headers = new ArrayList<>(rawHeaders.size());
					for (String h : rawHeaders) headers.add(h);
					ColumnMatcherDialog cmd = new ColumnMatcherDialog(shell, SWT.NONE);
					Map<String, Integer> columnMap = cmd.open(headers);
					if (columnMap == null) return;
					List<Donor> newDonors = new ArrayList<>();
					for (int acct = Main.getDonorDB().getMaxAccount() + 1; iterRecord.hasNext(); acct++) {
						CSVRecord record = iterRecord.next();
						Donor d = new Donor(acct);
						for (Entry<String, Integer> e : columnMap.entrySet()) {
							d.data.putData(e.getKey(), record.get(e.getValue()));
						}
						newDonors.add(d);
						System.out.println(d);
					}
					Main.getDonorDB().saveDonors(newDonors.toArray(new Donor[0]));
					refresh();
				} catch (IOException e) {
					throw new RuntimeException("Error importing CSV file", e);
				}

			}
		});;

		MenuItem mntmExport = new MenuItem(menuFile, SWT.CASCADE);
		mntmExport.setText("Export");

		Menu menuExport = new Menu(mntmExport);
		mntmExport.setMenu(menuExport);

		MenuItem mntmCsv = new MenuItem(menuExport, SWT.NONE);
		mntmCsv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setFilterExtensions(new String[]{"*.csv","*.*"});
				fileDialog.setFilterNames(new String[]{"Comma Separated Values (*.csv)","All Files"});
				final String path = fileDialog.open();
				if (path == null) return;
				File f = new File(path);
				Util.writeCSV(donorTable.donors, f);
			}
		});
		mntmCsv.setText("To CSV file...");

		MenuItem mntmOds = new MenuItem(menuExport, SWT.NONE);
		mntmOds.setEnabled(false);
		mntmOds.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setFilterExtensions(new String[]{"*.ods","*.*"});
				fileDialog.setFilterNames(new String[]{"Open Document Spreadsheet (*.ods)","All Files"});
				final String path = fileDialog.open();
				if (path == null) return;
				File f = new File(path);
				Util.writeODS(donorTable.donors, f, true);
			}
		});
		mntmOds.setText("To ODS (LibreOffice Spreadsheet) file...");

		MenuItem mntmOdb = new MenuItem(menuExport, SWT.NONE);
		mntmOdb.setEnabled(false);
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

		MenuItem mntmExit = new MenuItem(menuFile, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		mntmExit.setText("Exit");

		/*
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
		*/

		MenuItem mntmDatabase = new MenuItem(menu, SWT.CASCADE);
		mntmDatabase.setText("Database");

		Menu menuDatabase = new Menu(mntmDatabase);
		mntmDatabase.setMenu(menuDatabase);

		MenuItem mntmNewDonor = new MenuItem(menuDatabase, SWT.NONE);
		mntmNewDonor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				newDonor();
			}
		});
		mntmNewDonor.setText("New Donor");

		final MenuItem mntmSaveCurrentDonor = new MenuItem(menuDatabase, SWT.NONE);
		mntmSaveCurrentDonor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (saveCurrent == null) return;
				saveCurrent.run();
			}
		});
		menuDatabase.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				mntmSaveCurrentDonor.setEnabled(tltmSave.getEnabled());
			}
		});
		mntmSaveCurrentDonor.setText("Save Current Donor");

		MenuItem mntmSaveAllDonors = new MenuItem(menuDatabase, SWT.NONE);
		mntmSaveAllDonors.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((DonorTabFolder)donorTabFolder).saveAll();
			}
		});
		mntmSaveAllDonors.setText("Save All Donors");
		
		new MenuItem(menuDatabase, SWT.SEPARATOR);
		
		MenuItem mntmDbProperties = new MenuItem(menuDatabase, SWT.NONE);
		mntmDbProperties.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new DatabasePropertiesDialog(shell, SWT.DIALOG_TRIM).open();
			}
		});
		mntmDbProperties.setText("Properties...");

		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("Help");

		Menu menuHelp = new Menu(mntmHelp);
		mntmHelp.setMenu(menuHelp);

		MenuItem mntmAbout = new MenuItem(menuHelp, SWT.NONE);
		mntmAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new AboutDialog(shell, SWT.DIALOG_TRIM).open();
			}
		});
		mntmAbout.setText("About...");

//		Composite compositeToolbar = new Composite(shell, SWT.NONE);
//		compositeToolbar.setLayout(new GridLayout(3, false));
//		compositeToolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		ToolBar toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		toolBar.setBounds(0, 0, 80, 21);

		ToolItem tltmNewDonor = new ToolItem(toolBar, SWT.NONE);
		tltmNewDonor.addSelectionListener(new SelectionAdapter() {
			@Override
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
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (saveCurrent == null) return;
				saveCurrent.run();
			}
		});
		new ToolItem(toolBar, SWT.SEPARATOR);

		ToolItem tltmDbProperties = new ToolItem(toolBar, SWT.NONE);
		tltmDbProperties.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new DatabasePropertiesDialog(shell, SWT.DIALOG_TRIM).open();
			}
		});
		tltmDbProperties.setText("Database Properties");
		tltmDbProperties.setImage(ResourceManager.getIcon("db-properties.png"));

		//TODO add advanced search with SQL queries
		
		final SashForm mainPanel = new SashForm(shell, SWT.SMOOTH);
		mainPanel.setSashWidth(6);
		mainPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		
		donorTable = new DonorTable(mainPanel, SWT.NONE);

		donorTabFolder = new DonorTabFolder(mainPanel, SWT.NONE);
		FillLayout fillLayout = (FillLayout) donorTabFolder.getLayout();
		fillLayout.marginWidth = 1;
		donorTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		mainPanel.setBackground(new Color(display, 0, 0, 0));
		final Color divider = TabFolder.changeColorBrightness(display, display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), -50);
		donorTabFolder.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle size = donorTabFolder.getClientArea();
				e.gc.setForeground(divider);
				e.gc.drawLine(size.x, size.y, size.x, size.y + size.height);
			}
		});
		
		final TabFolder tabFolder = ((DonorTabFolder) donorTabFolder).tabFolder;
		donorTable.setTabFolder(tabFolder);
		tabFolder.addTabFolderListener(new TabFolderListener() {
			@Override
			public void close(TabFolderEvent e) {
				if (tabFolder.getItems().length <= 1) mainPanel.setWeights(new int[] {1, 0});
			}
			@Override
			public void open(TabFolderEvent e) {
				if (tabFolder.getItems().length >= 1) {
					int[] weights = mainPanel.getWeights();
					for (int w : weights) {
						if (w == 0) {
							mainPanel.setWeights(new int[] {1, 3});
							return;
						}
					}
				}
			}
		});
		mainPanel.setWeights(new int[] {1, 0});

	}
	public ToolItem getSaveButton() {
		return tltmSave;
	}
	public void setSaveAction(Runnable r) {
		saveCurrent = r;
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
	public DonorTabFolder getCompositeDonorList() {
		return (DonorTabFolder) donorTabFolder;
	}


	public void refresh() {
		donorTable.refresh();
	}

	public void newDonor() {
		((DonorTabFolder)donorTabFolder).newDonor();
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

	public Display getDisplay() {
		return display;
	}
	
	public void setCursor(int cursor) {
		shell.setCursor(new Cursor(display, cursor));
	}

	public DonorTable getDonorTable() {
		return donorTable;
	}
}
