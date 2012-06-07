package net.sf.librefundraiser.gui;
import net.sf.librefundraiser.LibreFundraiser;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;


public class FundRaiserImportDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	private ProgressBar progressBar;
	private Composite compositeProgress;
	private ProgressBar progressBarIndeterminate;
	private Label lblImporting;
	private boolean canceled = true;
	private Button btnCancel;
	private Display display;
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public FundRaiserImportDialog(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 * @throws OperationCanceledException 
	 */
	public boolean open() {
		createContents();
		shell.open();
		shell.layout();
		display = getParent().getDisplay();
		Rectangle bounds = getParent().getBounds();
		shell.setLocation(bounds.x+(bounds.width/2)-160, bounds.y+(bounds.height/2)-65);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return canceled;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
		shell.setSize(320, 130);
		shell.setImages(LibreFundraiser.logo);
		shell.setText("Importing from FundRaiser Basic...");
		GridLayout gl_shlImportingFromFundraiser = new GridLayout(1, false);
		gl_shlImportingFromFundraiser.verticalSpacing = 10;
		shell.setLayout(gl_shlImportingFromFundraiser);

		lblImporting = new Label(shell, SWT.NONE);
		lblImporting.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		lblImporting.setText("Importing...");

		compositeProgress = new Composite(shell, SWT.NONE);
		compositeProgress.setLayout(new StackLayout());
		compositeProgress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		progressBarIndeterminate = new ProgressBar(compositeProgress, SWT.INDETERMINATE);

		progressBarIndeterminate.setVisible(true);
		((StackLayout)compositeProgress.getLayout()).topControl = progressBarIndeterminate;

		progressBar = new ProgressBar(compositeProgress, SWT.SMOOTH);
		progressBar.setMaximum(100);
		progressBar.setVisible(false);


		btnCancel = new Button(shell, SWT.NONE);
		btnCancel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		btnCancel.setText("Cancel");

	}

	public void setProgress(int i) {
		if (!progressBar.getVisible()) {
			progressBarIndeterminate.setVisible(false);
			progressBar.setVisible(true);
			((StackLayout)compositeProgress.getLayout()).topControl = progressBar;
		}
		progressBar.setSelection(i);
	}
	public void setProgressMaximum(int i) {
		progressBar.setMaximum(i);
	}
	public void setStatusText(String status) {
		lblImporting.setText(status);
	}
	public void dispose() {
		this.canceled = false;
		shell.dispose();
	}
	public void setCancelable(boolean cancelable) {
		btnCancel.setEnabled(cancelable);
	}
}
