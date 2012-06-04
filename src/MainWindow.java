import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;

public class MainWindow extends ApplicationWindow {
	private Action actExit;
	private Action actionNewDonor;
	private Action actionSave;


	public MainWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	protected Control createContents(Composite parent) {
		setStatus("I'll find something to put here...");

		Composite container = new DonorList(parent, SWT.NONE);
		return container;
	}

	private void createActions() {

		actExit = new Action("Exit") {

		};

		actionNewDonor = new Action("New Donor") {

		};
		actionNewDonor.setImageDescriptor(ResourceManager.getImageDescriptor(MainWindow.class, "/icons/new-donor.png"));

		actionSave = new Action("Save") {

		};
		actionSave.setEnabled(false);
		actionSave.setImageDescriptor(ResourceManager.getImageDescriptor(MainWindow.class, "/icons/save.png"));

	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");

		MenuManager menuFile = new MenuManager("File");
		menuManager.add(menuFile);
		menuFile.add(actExit);
		return menuManager;
	}

	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
		toolBarManager.add(actionNewDonor);
		toolBarManager.add(actionSave);
		toolBarManager.add(new Separator());
		return toolBarManager;
	}

	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}



	/**
	 * Configure the shell.
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("LibreFundraiser");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(461, 212);
	}
}
